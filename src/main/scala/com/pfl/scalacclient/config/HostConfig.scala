package com.pfl.scalacclient.config

import eu.timepit.refined.types.net.UserPortNumber
import eu.timepit.refined.types.string.NonEmptyString

import ciris._
import ciris.refined._
import cats.implicits._
import cats.effect.kernel.Async
import eu.timepit.refined.auto._
import cats.Show

final case class ServiceConfig(
    port: UserPortNumber,
    token: Secret[NonEmptyString],
    user: NonEmptyString
)

object ServiceConfig {

  // implicit val configReader: ConfigReader[ServiceConfig] =
  //   deriveReader[ServiceConfig]

  implicit val s: Show[NonEmptyString] = Show[String].contramap(_.value)

  def config[F[_]: Async]: F[ServiceConfig] =
    (
      env("SCALAC_PORT").as[UserPortNumber],
      env("SCALAC_TOKEN").as[NonEmptyString].secret,
      env("SCALAC_USER").as[NonEmptyString]
    ).parMapN(ServiceConfig.apply).load[F]

}
