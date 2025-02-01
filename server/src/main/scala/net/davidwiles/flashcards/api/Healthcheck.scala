package net.davidwiles.flashcards.api

import sttp.tapir.*
import sttp.tapir.server.ServerEndpoint

import scala.concurrent.Future

object Healthcheck:

  val endpointImpl: ServerEndpoint[Any, Future] =
    endpoint.get
      .in("healthz")
      .out(stringBody)
      .serverLogicPure { _ =>
        Right("ok")
      }
