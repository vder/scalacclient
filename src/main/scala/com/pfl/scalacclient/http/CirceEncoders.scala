package com.pfl.scalacclient.http

import com.pfl.scalacclient.model._
import io.circe.Encoder
import io.circe.generic.semiauto._

trait CirceEncoders {
  implicit val UserDecoder: Encoder[User] =
    deriveEncoder[User]
  implicit val loginDecoder: Encoder[Login] =
    Encoder[String].contramap(_.value.value)

  implicit val contribDecoder: Encoder[Contributions] =
    Encoder[Int].contramap(_.value.value)
}
