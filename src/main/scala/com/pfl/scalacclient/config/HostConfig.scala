package com.pfl.scalacclient.config

import eu.timepit.refined.types.net.UserPortNumber
import eu.timepit.refined.types.string.NonEmptyString

import ciris.*
import ciris.refined.*
import cats.implicits.*
import cats.effect.kernel.Async
import eu.timepit.refined.auto.*
import cats.Show

final case class ServiceConfig(
    port: UserPortNumber,
    token: Secret[NonEmptyString[?],
    user: NonEmptyString
)

object ServiceConfig {

  given Show[NonEmptyString[?] = Show[String[?].contramap(_.value)

  def config[F[_[?]: Async[?]: F[ServiceConfig[?] =
    (
      env("SCALAC_PORT").as[UserPortNumber[?],
      env("SCALAC_TOKEN").as[NonEmptyString[?].secret,
      env("SCALAC_USER").as[NonEmptyString[?]
    ).parMapN(ServiceConfig.apply).load[F[?]

}
