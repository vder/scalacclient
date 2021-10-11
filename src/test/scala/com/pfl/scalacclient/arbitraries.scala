package com.pfl.scalacclient

import org.scalacheck.Arbitrary
import com.pfl.scalacclient.generators._
import eu.timepit.refined.types.string.NonEmptyString
import org.scalacheck.Gen
import com.pfl.scalacclient.model._

object arbitraries {

  implicit def arbNonEmptyStringGen: Arbitrary[String] = Arbitrary(
    nonEmptyStringGen
  )
  implicit def arbRepoGen: Arbitrary[Repo] = Arbitrary(repoGen)
  implicit def arbOrganisationGen: Arbitrary[Organisation] = Arbitrary(
    organisationGen
  )
  implicit def arbLoginGen: Arbitrary[Login] = Arbitrary(loginGen)
  implicit def arbContributionsGen: Arbitrary[Contributions] = Arbitrary(
    contributionsGen
  )
  implicit def arbUserGen: Arbitrary[User] = Arbitrary(userGen)
  implicit def arbGithubRepoMap: Arbitrary[Map[Repo, List[User]]] =
    Arbitrary(gitHubRepoGen)
  implicit def arbGithubOrgMap: Arbitrary[Map[Organisation, List[User]]] =
    Arbitrary(gitHubOrgGen)
  implicit def arbGithubMap
      : Arbitrary[Map[Organisation, Map[Repo, List[User]]]] = Arbitrary(
    gitHubGen
  )

}
