package net.davidwiles.flashcards.api

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import net.davidwiles.flashcards.api.auth.AuthManager
import net.davidwiles.flashcards.api.engine.JwtEngine
import net.davidwiles.flashcards.api.engine.UserEngine
import org.apache.pekko.Done
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.http.scaladsl.Http
import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import scalikejdbc.config.DBs
import scalikejdbc.config.EnvPrefix
import scalikejdbc.config.TypesafeConfig
import scalikejdbc.config.TypesafeConfigReader
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.pekkohttp.PekkoHttpServerInterpreter
import sttp.tapir.server.pekkohttp.PekkoHttpServerOptions
import sttp.tapir.swagger.bundle.SwaggerInterpreter

import scala.concurrent.Future

object RootBehavior:

  sealed trait Command

  case class Stop(replyTo: ActorRef[Done]) extends Command

  def apply(authManager: AuthManager): Behavior[Nothing] = Behaviors.setup { ctx =>
    import ctx.executionContext
    implicit val system: ActorSystem = ctx.system.classicSystem

    val authEndpoints = auth.Endpoints(authManager)

    val endpoints = authEndpoints.allEndpoints

    val openApiEndpoints = SwaggerInterpreter()
      .fromServerEndpoints(endpoints, "FLASHCARDS API", "1.0")

    val routes = PekkoHttpServerInterpreter(PekkoHttpServerOptions.default).toRoute(
      Healthcheck.endpointImpl :: endpoints ++ openApiEndpoints
    )

    val binding = Http()
      .newServerAt("0.0.0.0", 8080)
      .bindFlow(routes)

    Behaviors.receiveMessage { case Stop(replyTo) =>
      binding.foreach(_.unbind())
      replyTo ! Done
      Behaviors.stopped
    }
  }

case class HikariConnectionPool(config: Config) extends DBs with TypesafeConfigReader with TypesafeConfig with EnvPrefix

@main
def main(): Unit =
  val config = ServiceConfig()

  HikariConnectionPool(config.config).setupAll()

  val jwtEngine   = JwtEngine(config.publicKey(), config.privateKey())
  val userEngine  = UserEngine()
  val authManager = AuthManager(jwtEngine, userEngine)

  org.apache.pekko.actor.typed.ActorSystem(
    RootBehavior(authManager),
    "main"
  )
