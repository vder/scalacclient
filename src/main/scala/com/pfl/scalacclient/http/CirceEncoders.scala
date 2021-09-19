package com.pfl.scalacclient.http

import com.pfl.scalacclient.model._
import io.circe.Encoder
import io.circe.generic.semiauto._

trait CirceEncoders {
  implicit val userEncoder: Encoder[User] =
    deriveEncoder[User]
  implicit val loginEncoder: Encoder[Login] =
    Encoder[String].contramap(_.value.value)

  implicit val contribEncoder: Encoder[Contributions] =
    Encoder[Int].contramap(_.value.value)
}
