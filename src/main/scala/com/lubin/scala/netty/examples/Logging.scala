package com.lubin.scala.netty.examples

import org.slf4j.{LoggerFactory, Logger}

trait Logging {
  val logger: Logger  = LoggerFactory.getLogger(getClass);
}
