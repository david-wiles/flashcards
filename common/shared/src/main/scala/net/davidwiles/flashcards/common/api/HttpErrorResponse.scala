package net.davidwiles.flashcards.common.api

sealed trait HttpErrorResponse

object HttpErrorResponse:

  case class NotFound(message: String) extends HttpErrorResponse

  case class BadRequest(message: String) extends HttpErrorResponse

  case class Unauthorized() extends HttpErrorResponse

  case class Conflict(message: String) extends HttpErrorResponse

  case class InternalServerError() extends HttpErrorResponse
