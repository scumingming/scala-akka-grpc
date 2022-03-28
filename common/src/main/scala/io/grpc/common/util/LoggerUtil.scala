package io.grpc.common.util

import org.slf4j.LoggerFactory

trait LoggerUtil {

  val logger = LoggerFactory.getLogger(this.getClass.getSimpleName)

}
