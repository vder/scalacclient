package com.pfl.scalacclient

import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric._
import eu.timepit.refined.types.string

object model {
  final case class Organisation(value: string.NonEmptyString)
  final case class Repo(value: string.NonEmptyString)
  final case class Login(value: string.NonEmptyString)
  final case class Contributions(value: Int Refined Positive)
  final case class User(login: Login, contributions: Contributions)

  object User {
    given Ordering[User] with {
      def compare(x: User, y: User) = {
        val contributionCompare =
          (-x.contributions.value.value).compareTo(-y.contributions.value.value)
        if (contributionCompare == 0)
          x.login.value.value.compareTo(y.login.value.value)
        else
          contributionCompare
      }
    }
  }

  object Contributions {
    def unsafeApply(i: Int) = Contributions(
      Refined.unsafeApply[Int, Positive](i)
    )
  }
}
