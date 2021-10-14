package com.pfl.scalacclient

import org.scalacheck.Gen
import com.pfl.scalacclient.model._
import eu.timepit.refined.types.string
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Positive

object generators {

  val nonEmptyStringGen: Gen[String] =
    Gen
      .chooseNum(21, 40)
      .flatMap { n =>
        Gen.buildableOfN[String, Char](n, Gen.alphaChar)
      }

  val repoGen: Gen[Repo] =
    nonEmptyStringGen
      .map[string.NonEmptyString](Refined.unsafeApply)
      .map(Repo.apply)

  val organisationGen: Gen[Organisation] =
    nonEmptyStringGen
      .map[string.NonEmptyString](Refined.unsafeApply)
      .map(Organisation.apply)

  val loginGen: Gen[Login] =
    nonEmptyStringGen
      .map[string.NonEmptyString](Refined.unsafeApply)
      .map(Login.apply)

  val contributionsGen: Gen[Contributions] =
    Gen
      .choose(1, 10000)
      .map(Refined.unsafeApply[Int, Positive])
      .map(Contributions.apply)

  val userGen: Gen[User] =
    for {
      login <- loginGen
      contributions <- contributionsGen
    } yield User(login, contributions)

  val gitHubOrgGen: Gen[Map[Organisation, List[User]]] =
    for {
      organisation <- nonEmptyGen(organisationGen)
      usersList <- nonEmptyGen(nonEmptyGen(userGen))
      map = (organisation zip usersList).toMap
    } yield map

  val gitHubRepoGen: Gen[Map[Repo, List[User]]] =
    for {
      repo <- nonEmptyGen(repoGen)
      usersList <- nonEmptyGen(nonEmptyGen(userGen))
      map = (repo zip usersList).toMap
      if map.size > 0
    } yield map

  val gitHubGen: Gen[Map[Organisation, Map[Repo, List[User]]]] =
    for {
      organisation <- nonEmptyGen(organisationGen)
      reposMap <- nonEmptyGen(gitHubRepoGen)
    } yield (organisation zip reposMap).toMap

  def nonEmptyGen[T](gen: Gen[T]): Gen[List[T]] =
    for {
      n <- Gen.choose(1, 20)
      list <- Gen.containerOfN[List, T](n, gen)
    } yield list
}
