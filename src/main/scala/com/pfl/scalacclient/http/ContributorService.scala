package com.pfl.scalacclient.http

import cats.effect.Sync
import com.pfl.scalacclient.github.GitHubProgram
import com.pfl.scalacclient.model._
import eu.timepit.refined.types.string.NonEmptyString

final class ContributorService[F[_]](githubApi: GitHubProgram[F]) {

  def listContributors(organisationName: NonEmptyString): F[List[User]] =
    githubApi.listContributors(Organisation(organisationName))
}

object ContributorService {
  def make[F[_]: Sync](githubApi: GitHubProgram[F]) =
    Sync[F].delay(
      new ContributorService[F](githubApi)
    )
}
