package net.davidwiles.flashcards.api.auth

import cats.data.EitherT
import net.davidwiles.flashcards.api.ServiceError
import net.davidwiles.flashcards.api.engine.JwtEngine
import net.davidwiles.flashcards.api.engine.UserEngine
import net.davidwiles.flashcards.common.models.User
import net.davidwiles.flashcards.api.ServiceError.*

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class AuthManager(jwtEngine: JwtEngine, userEngine: UserEngine):

  def getUserFromToken(token: String)(implicit ec: ExecutionContext): Future[Either[ServiceError, User]] =
    Future(
      for
        claims  <- jwtEngine.verify(token)
        username = claims.getSubject
        userOpt <- userEngine.get(username)
        user    <- userOpt.toRight(UserNotFound(username))
      yield user
    )

  def loginHandler(username: String, password: String)(implicit
      ec: ExecutionContext
  ): Future[Either[ServiceError, String]] =
    Future(
      for
        userOpt <- userEngine.get(username)
        user    <- userOpt.toRight(UserNotFound(username))
        result  <- userEngine.checkPass(password, user.passHash)
        _       <- if result then Right(()) else Left(IncorrectPassword())
        token   <- jwtEngine.sign(username)
      yield token
    )

  def signupHandler(username: String, email: String, password: String)(implicit
      ec: ExecutionContext
  ): Future[Either[ServiceError, String]] =
    Future(
      for
        passHash <- userEngine.hashPass(password)
        _        <- userEngine.create(username, email, passHash)
        token    <- jwtEngine.sign(username)
      yield token
    )

  def keepaliveHandler(token: String)(implicit ec: ExecutionContext): Future[Either[ServiceError, String]] =
    val result = for
      user  <- EitherT(getUserFromToken(token))
      token <- EitherT.fromEither(jwtEngine.sign(user.username))
    yield token
    result.value
