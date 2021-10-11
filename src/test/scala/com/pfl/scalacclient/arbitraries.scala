package com.pfl.scalacclient

import org.scalacheck.Arbitrary
import com.pfl.scalacclient.generators._
import com.pfl.scalacclient.model._

object arbitraries {

  given Arbitrary[String] = Arbitrary(nonEmptyStringGen)
  given Arbitrary[Repo] = Arbitrary(repoGen)
  given Arbitrary[Organisation] = Arbitrary(organisationGen)
  given Arbitrary[Login] = Arbitrary(loginGen)
  given Arbitrary[Contributions] = Arbitrary(contributionsGen)
  given Arbitrary[User] = Arbitrary(userGen)
  given Arbitrary[Map[Repo, List[User]]] = Arbitrary(gitHubRepoGen)
  given arbGithubOrgMap: Arbitrary[Map[Organisation, List[User]]] =
    Arbitrary(gitHubOrgGen)
  given arbGithubMap: Arbitrary[Map[Organisation, Map[Repo, List[User]]]] =
    Arbitrary(gitHubGen)

}
