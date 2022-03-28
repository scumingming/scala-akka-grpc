package io.grpc.server.module

import akka.actor.typed.ActorSystem
import akka.actor.{ActorSystem => ClassicActorSystem}
import akka.actor.typed.scaladsl.ActorContext
import com.google.inject.{AbstractModule, Provides, Singleton}
import io.grpc.common.util.LoggerUtil

import scala.concurrent.ExecutionContext

case class ActorModule(context: ActorContext[_]) extends AbstractModule with LoggerUtil {

  @Singleton
  @Provides
  def executionContext():ExecutionContext = context.executionContext

  @Singleton
  @Provides
  def typedActorSystem():ActorSystem[_] = context.system

  @Singleton
  @Provides
  def classicActorSystem(): ClassicActorSystem = context.system.classicSystem

}
