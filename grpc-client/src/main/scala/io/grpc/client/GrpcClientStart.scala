package io.grpc.client

import akka.Done
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.actor.{ActorSystem => ClassicActorSystem}
import akka.grpc.GrpcClientSettings
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.complete
import ch.qos.logback.classic.{Level, LoggerContext}
import com.google.inject.{Guice, Inject}
import com.typesafe.config.ConfigFactory
import io.grpc.client.module.{ActorModule, GrpcServiceModule}
import io.grpc.common.util.LoggerUtil
import io.schemas.{GreeterService, GreeterServiceClient, HelloRequest}
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import org.slf4j.LoggerFactory
import scala.concurrent.duration.DurationInt

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object GrpcClientStart extends App with LoggerUtil {

  val env        = System.getProperty("env", "dev")
  val config     = ConfigFactory.load(s"$env/application.conf")
  val serverName = config.getString("app-name")
  val port       = 8081

  System.setProperty("logback.configurationFile", s"$env/logback.xml")

  val loggerContext = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
  loggerContext.getLogger("org.apache.kafka").setLevel(Level.toLevel("ERROR"))
  if (config.hasPath("log-level")) {
    val logLevel = config.getString("log-level").trim.toUpperCase()
    loggerContext.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).setLevel(Level.toLevel(logLevel))
  }

  ActorSystem[Done](
    Behaviors.setup[Done] { implicit ctx =>
      implicit val classicSystem = ctx.system.classicSystem
      implicit val ec            = ctx.executionContext
      implicit val injector      = Guice.createInjector(ActorModule(ctx), GrpcServiceModule(ctx))
      val server                 = injector.instance[GrpcClientStart]
      server.run() onComplete {
        case Success(_) => logger.info(s"Binding success!")
        case Failure(e) => logger.error(s"Binding failed, exception=$e")
      }
      Behaviors.receiveMessage { case Done => Behaviors.stopped }
    },
    serverName,
    config
  )

}

case class GrpcClientStart @Inject() (
    implicit val injector: ScalaInjector,
    implicit val classicSystem: ClassicActorSystem,
    implicit val ec: ExecutionContext
) extends LoggerUtil {
  import GrpcClientStart._

  val api = injector.instance[ApiDependencyWiring]

//  val clientSettings = GrpcClientSettings.fromConfig(GreeterService.name)
//  val client         = GreeterServiceClient(clientSettings)
//  client.sayHello(HelloRequest("WM")) onComplete {
//    case Success(r) =>
//      logger.error(s"sayHello success:${r.message}")
//      complete(StatusCodes.OK, r.message)
//    case Failure(e) =>
//      logger.error(s"sayHello failed:${e.printStackTrace()}")
//      complete(StatusCodes.OK, "sayHello failed")
//  }

  def run(): Future[Http.ServerBinding] = {

    val binding = Http(classicSystem)
      .newServerAt(interface = "0.0.0.0", port = port)
      .bind(api.routes)
      .map(_.addToCoordinatedShutdown(hardTerminationDeadline = 10.seconds))

    binding.foreach{b => logger.info(s"grpc-client bound to ${b.localAddress}")}

    binding
  }
}
