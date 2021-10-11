package com.pfl.scalacclient

import org.scalacheck.Arbitrary
import com.pfl.scalacclient.generators._
// import eu.timepit.refined.types.string.NonEmptyString
// import org.scalacheck.Gen
import com.pfl.scalacclient.model._

object arbitraries {

  given arbNonEmptyStringGen: Arbitrary[String] = Arbitrary(
    nonEmptyStringGen
  )
  given arbRepoGen: Arbitrary[Repo] = Arbitrary(repoGen)
  given arbOrganisationGen: Arbitrary[Organisation] = Arbitrary(
    organisationGen
  )
  given arbLoginGen: Arbitrary[Login] = Arbitrary(loginGen)
  given arbContributionsGen: Arbitrary[Contributions] = Arbitrary(
    contributionsGen
  )
  given arbUserGen: Arbitrary[User] = Arbitrary(userGen)
  given arbGithubRepoMap: Arbitrary[Map[Repo, List[User]]] =
    Arbitrary(gitHubRepoGen)
  given arbGithubOrgMap: Arbitrary[Map[Organisation, List[User]]] =
    Arbitrary(gitHubOrgGen)
  given arbGithubMap
      : Arbitrary[Map[Organisation, Map[Repo, List[User]]]] = Arbitrary(
    gitHubGen
  )

}
