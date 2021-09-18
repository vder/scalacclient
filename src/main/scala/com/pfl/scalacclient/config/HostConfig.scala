package com.pfl.scalacclient.config

import eu.timepit.refined.auto._
import eu.timepit.refined.pureconfig._
import eu.timepit.refined.types.net.UserPortNumber
import eu.timepit.refined.types.string.NonEmptyString
import pureconfig.ConfigReader
import pureconfig.generic.semiauto._

final case class ServiceConfig(
    port: UserPortNumber,
    token: NonEmptyString,
    user: NonEmptyString
)

object ServiceConfig {

  implicit val configReader: ConfigReader[ServiceConfig] =
    deriveReader[ServiceConfig]

}
