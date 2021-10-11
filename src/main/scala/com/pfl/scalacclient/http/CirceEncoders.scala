package com.pfl.scalacclient.http

import com.pfl.scalacclient.model._
import io.circe.Encoder
import io.circe.generic.semiauto._

trait CirceEncoders {
  given Encoder[User] =
    deriveEncoder[User]
  given Encoder[Login] =
    Encoder[String].contramap(_.value.value)

  given Encoder[Contributions] =
    Encoder[Int].contramap(_.value.value)
}
