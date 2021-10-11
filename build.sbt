import Dependencies.Libraries._

ThisBuild / scalaVersion := "3.0.0"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.pfl"
ThisBuild / organizationName := "pfl"

//Test / parallelExecution := false

lazy val root = (project in file("."))
  .settings(
    name := "scalacclient",
    scalaVersion := "3.0.0",
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
      //   "-deprecation",
      //   //  "-encoding",
      //   "utf-8",
      //   "future",
      //   "-language:higherKinds",
      "-language:postfixOps"
      //   "-feature"
      //   //"-source:3.0-migration",
      //   // "-rewrite"
      //   //  "-Xfatal-warnings",
      //   // "-Ykind-projector",
      //   //"-Yrangepos"
    )
  )
