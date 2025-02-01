package net.davidwiles.flashcards.api.user

import sttp.tapir.*
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint
import io.circe.generic.auto.*
import net.davidwiles.flashcards.common.api.*
import sttp.model.StatusCode
import sttp.tapir.generic.auto.*
import sttp.tapir.server.ServerEndpoint
import net.davidwiles.flashcards.api.auth.AuthManager

import scala.concurrent.Future

class Endpoints(userManager: UserManager, authManager: AuthManager)

//  val allEndpoints: List[ServerEndpoint[Any, Future]] =
//    List(viewProfile, editProfile, deleteAccount)
//
//  val viewProfile =
//    endpoint.get
//      .in("profile")
//      .out(jsonBody[UserProfile])
//      .errorOut(
//        oneOf(
//          oneOfVariant(StatusCode.Unauthorized, jsonBody[HttpErrorResponse.Unauthorized]),
//          oneOfVariant(StatusCode.InternalServerError, jsonBody[HttpErrorResponse.InternalServerError])
//        )
//      )
//      .securityIn(auth.bearer[String]())
//      .serverSecurityLogicPure(
//        authManager
//          .getUserFromToken
//          .andThen {
//            case Left(_) =>     Left(HttpErrorResponse.Unauthorized())
//            case Right(user) => Right(user)
//          }
//      )
//      .serverLogic { user => _ =>
//        Future.successful(Right(UserProfile(user.id, user.username, user.email, user.dateCreated)))
//      }
