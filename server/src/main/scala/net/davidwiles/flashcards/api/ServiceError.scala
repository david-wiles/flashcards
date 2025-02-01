package net.davidwiles.flashcards.api

abstract class ServiceError(val message: String, val cause: Option[Throwable] = None)

object ServiceError:

  case class UserNotFound(username: String) extends ServiceError(s"user $username not found")

  case class IncorrectPassword() extends ServiceError("password incorrect")

  case class BcryptError(override val message: String, override val cause: Option[Throwable] = None)
      extends ServiceError(message, cause)

  case class SQLExceptionError(override val message: String, ex: Throwable) extends ServiceError(message, Some(ex))

  case class UserNotCreatedError(override val message: String) extends ServiceError(message)

  case class JwtSignError(override val message: String, override val cause: Option[Throwable] = None)
      extends ServiceError(message, cause)

  case class JwtVerifyError(override val message: String, override val cause: Option[Throwable] = None)
      extends ServiceError(message, cause)

  case class JwtExpiredError(override val message: String, override val cause: Option[Throwable] = None)
      extends ServiceError(message, cause)
