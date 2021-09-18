package com.pfl.scalacclient

import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric._
import eu.timepit.refined.types.string

object model {
  final case class Organisation(value: string.NonEmptyString)
  final case class Repo(value: string.NonEmptyString)
  final case class Login(value: string.NonEmptyString)
  final case class User(login: Login, contributions: Int Refined Positive)
}
