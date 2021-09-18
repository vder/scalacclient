package com.pfl.scalacclient

import org.scalacheck.Arbitrary
import com.pfl.scalacclient.generators._

object arbitraries {

  implicit def arbNonEmptyStringGen = Arbitrary(nonEmptyStringGen)
  implicit def arbRepoGen = Arbitrary(repoGen)
  implicit def arbOrganisationGen = Arbitrary(organisationGen)
  implicit def arbLoginGen = Arbitrary(loginGen)
  implicit def arbContributionsGen = Arbitrary(contributionsGen)
  implicit def arbUserGen = Arbitrary(userGen)
  implicit def arbGithubRepoMap = Arbitrary(gitHubRepoGen)
  implicit def arbGithubOrgMap = Arbitrary(gitHubOrgGen)
  implicit def arbGithubMap = Arbitrary(gitHubGen)

}
