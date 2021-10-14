import Dependencies.Libraries._

ThisBuild / scalaVersion := "3.1.0-RC3"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.pfl"
ThisBuild / organizationName := "pfl"

//Test / parallelExecution := false

lazy val root = (project in file("."))
  .settings(
    name := "scalacclient",
    scalaVersion := "3.1.0-RC3",
    libraryDependencies ++= Seq(
      catsEffect,
      circe,
      circeFs2,
      circeParser,
      circeRefined,
      ciris,
      cirisRefined,
      http4sCirce,
      http4sDsl,
      http4sClient,
      http4sServer,
      logback,
      mUnit,
      mUnitCE,
      mUnitScalacheck,
      refined,
      scalaCheckEffect,
      scalaCheckEffectMunit,
      slf4j
    ),
    scalacOptions ++= Seq(
    //  "-language:postfixOps",
      "-feature",
      "-Xfatal-warnings",
      "-Ykind-projector"
    )
  )
