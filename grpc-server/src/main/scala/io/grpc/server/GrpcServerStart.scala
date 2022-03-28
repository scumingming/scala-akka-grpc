package io.grpc.server

import akka.Done
import akka.actor.typed.ActorSystem
import akka.actor.{ActorSystem => ClassicActorSystem}
import akka.actor.typed.scaladsl.Behaviors
import akka.grpc.scaladsl.{ServerReflection, ServiceHandler}
import ch.qos.logback.classic.{Level, LoggerContext}
import com.google.inject.{Guice, Inject}
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import io.grpc.common.util.LoggerUtil
import io.grpc.server.module.ActorModule
import io.grpc.server.service.impl.{AuthServiceImpl, GreeterServiceImpl}
import io.schemas.{AuthService, AuthServiceHandler, GreeterService, GreeterServiceHandler}
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object GrpcServerStart extends App with LoggerUtil {

  val env        = System.getProperty("env", "dev")
  val config     = ConfigFactory.load(s"$env/application.conf")
  val serverName = config.getString("app-name")
  val port       = 8080

  System.setProperty("logback.configurationFile", s"$env/logback.xml")

  val loggerContext = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
  loggerContext.getLogger("org.apache.kafka").setLevel(Level.toLevel("ERROR"))
  if (config.hasPath("log-level")) {
    val logLevel = config.getString("log-level").trim.toUpperCase()
    loggerContext.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).setLevel(Level.toLevel(logLevel))
  }

  ActorSystem[Done](
    Behaviors.setup[Done] { implicit ctx =>
      implicit val classicSystem           = ctx.system.classicSystem
      implicit val ec                      = ctx.executionContext
      implicit val injector: ScalaInjector = Guice.createInjector(ActorModule(ctx))
      GrpcServerStart().runMultiService() onComplete {
        case Success(_) => logger.info(s"Binding success!")
        case Failure(e) => logger.error(s"Binding failed, exception=$e")
      }
      Behaviors.receiveMessage { case Done => Behaviors.stopped }
    },
    serverName,
    config
  )
}

case class GrpcServerStart @Inject() (implicit
    system: ClassicActorSystem,
    ec: ExecutionContext,
    injector: ScalaInjector
) extends LoggerUtil {
  import GrpcServerStart._

  def runSingleService(): Future[Http.ServerBinding] = {
    val greeterService: HttpRequest => Future[HttpResponse] = GreeterServiceHandler(
      injector.instance[GreeterServiceImpl]
    )

    val bound: Future[Http.ServerBinding] = Http(system)
      .newServerAt(interface = "0.0.0.0", port = port)
      .bind(greeterService)

    bound
  }

  def runMultiService(): Future[Http.ServerBinding] = {

    val greeterService: PartialFunction[HttpRequest, Future[HttpResponse]] =
      GreeterServiceHandler.partial(injector.instance[GreeterServiceImpl])

    val authService: PartialFunction[HttpRequest, Future[HttpResponse]] =
      AuthServiceHandler.partial(injector.instance[AuthServiceImpl])

    //作用是什么?
//    val reflectionService = ServerReflection.partial(List(GreeterService, AuthService))

    val serviceHandler = ServiceHandler.concatOrNotFound(greeterService, authService)

    val bound: Future[Http.ServerBinding] = Http(system)
      .newServerAt(interface = "0.0.0.0", port = port)
      .bind(serviceHandler)

    bound

  }

}
