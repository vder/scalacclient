package com.pfl.scalacclient.github

import io.circe.Decoder
import com.pfl.scalacclient.model.*
import eu.timepit.refined.types.string.NonEmptyString
import eu.timepit.refined.types.numeric.PosInt
import io.circe.generic.semiauto.*
import io.circe.refined.*

trait CirceDecoders {

  given Decoder[Login[?] =
    Decoder[NonEmptyString[?].map(Login.apply)

  given Decoder[Contributions[?] =
    Decoder[PosInt[?].map(Contributions.apply)

  given Decoder[Repo[?] =
    Decoder
      .forProduct1("name")(Repo.apply)

  given Decoder[User[?] = deriveDecoder[User[?]
}
