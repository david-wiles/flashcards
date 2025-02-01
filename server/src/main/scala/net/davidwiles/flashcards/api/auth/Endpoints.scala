package net.davidwiles.flashcards.api.auth

import sttp.tapir.*
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint
import io.circe.generic.auto.*
import net.davidwiles.flashcards.api.ServiceError
import net.davidwiles.flashcards.common.api.*
import sttp.model.StatusCode
import sttp.tapir.generic.auto.*

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class Endpoints(authManager: AuthManager)(implicit ec: ExecutionContext):

  private val signup =
    endpoint.post
      .in("signup")
      .in(jsonBody[SignupRequest])
      .out(jsonBody[SignupResponse])
      .errorOut(
        oneOf(
          oneOfVariant(StatusCode.BadRequest, jsonBody[HttpErrorResponse.BadRequest]),
          oneOfVariant(StatusCode.Conflict, jsonBody[HttpErrorResponse.Conflict]),
          oneOfVariant(StatusCode.InternalServerError, jsonBody[HttpErrorResponse.InternalServerError])
        )
      )
      .serverLogic { request =>
        authManager.signupHandler(request.username, request.email, request.password).map {
          case Right(token) => Right(SignupResponse(token))
          case Left(_) =>
            Left(HttpErrorResponse.InternalServerError())
        }
      }

  private val login =
    endpoint.post
      .in("login")
      .in(jsonBody[LoginRequest])
      .out(jsonBody[LoginResponse])
      .errorOut(
        oneOf(
          oneOfVariant(StatusCode.BadRequest, jsonBody[HttpErrorResponse.BadRequest]),
          oneOfVariant(StatusCode.InternalServerError, jsonBody[HttpErrorResponse.InternalServerError])
        )
      )
      .serverLogic { request =>
        authManager.loginHandler(request.username, request.password).map {
          case Right(token) => Right(LoginResponse(token))
          case Left(ServiceError.IncorrectPassword()) =>
            Left(HttpErrorResponse.BadRequest("incorrect password"))
          case Left(ServiceError.UserNotFound(_)) =>
            Left(HttpErrorResponse.BadRequest("user not found"))
          case Left(_) => Left(HttpErrorResponse.InternalServerError())
        }
      }

  private val keepalive =
    endpoint.get
      .in("keepalive")
      .out(jsonBody[KeepaliveResponse])
      .errorOut(
        oneOf(
          oneOfVariant(StatusCode.Unauthorized, jsonBody[HttpErrorResponse.Unauthorized]),
          oneOfVariant(StatusCode.BadRequest, jsonBody[HttpErrorResponse.BadRequest]),
          oneOfVariant(StatusCode.InternalServerError, jsonBody[HttpErrorResponse.InternalServerError])
        )
      )
      .securityIn(auth.bearer[String]())
      .serverSecurityLogicPure(Right(_)) // pass the token to the endpoint handler
      .serverLogic { token => _ =>
        authManager.keepaliveHandler(token).map {
          case Right(token) => Right(KeepaliveResponse(token))
          case Left(ServiceError.SQLExceptionError(_, _)) =>
            Left(HttpErrorResponse.InternalServerError())
          case Left(_) => Left(HttpErrorResponse.Unauthorized())
        }
      }

  val allEndpoints: List[ServerEndpoint[Any, Future]] =
    List(signup, login, keepalive)
