import sbt._

object Dependencies {
  object V {
    val catsEff = "3.1.1"
    val cats = "2.6.0"
    val pureConfig = "0.16.0"
    val refined = "0.9.23"
    val circe = "0.14.0"
    val http4s = "0.23.0"
    val Logback = "1.2.3"
    val circeDerivation = "0.13.0-M4"
    val betterMonadicFor = "0.3.1"
    val kindProjector = "0.13.0"
    val munit = "0.7.29"
    val scalacheckEffect = "1.0.2"
  }
  object Libraries {

    def http4sLib(artifact: String): ModuleID =
      "org.http4s" %% artifact % V.http4s
    def refinedLib(artifact: String): ModuleID =
      "eu.timepit" %% artifact % V.refined
    def circeLib(artifact: String): ModuleID = "io.circe" %% artifact % V.circe
    def mUnitLib(artifact: String): ModuleID =
      "org.scalameta" %% artifact % V.munit % Test
    def typeLevelLibTest(artifact: String, v: String): ModuleID =
      "org.typelevel" %% artifact % v % Test

    val catsEffect = "org.typelevel" %% "cats-effect" % V.catsEff
    val circe = circeLib("circe-generic")
    val circeDerivation = "io.circe" %% "circe-derivation" % V.circeDerivation
    val circeExtras = circeLib("circe-generic-extras")
    val circeFs2 = circeLib("circe-fs2")
    val circeParser = circeLib("circe-parser")
    val circeRefined = circeLib("circe-refined")
    val http4sCirce = http4sLib("http4s-circe")
    val http4sClient = http4sLib("http4s-blaze-client")
    val http4sDsl = http4sLib("http4s-dsl")
    val http4sServer = http4sLib("http4s-blaze-server")
    val logback = "ch.qos.logback" % "logback-classic" % V.Logback
    val pureConfig = "com.github.pureconfig" %% "pureconfig" % V.pureConfig
    val pureConfigCE =
      "com.github.pureconfig" %% "pureconfig-cats-effect" % V.pureConfig
    val pureConfigRefined = refinedLib("refined-pureconfig")
    val refined = refinedLib("refined")
    val slf4j = "org.typelevel" %% "log4cats-slf4j" % "2.1.1"
    val mUnitCE = typeLevelLibTest("munit-cats-effect-3", "1.0.0")
    val scalaCheckEffect =
      typeLevelLibTest("scalacheck-effect", V.scalacheckEffect)
    val scalaCheckEffectMunit =
      typeLevelLibTest("scalacheck-effect-munit", V.scalacheckEffect)

    // Compiler plugins
    val betterMonadicFor =
      "com.olegpy" %% "better-monadic-for" % V.betterMonadicFor
    val kindProjector =
      "org.typelevel" % "kind-projector" % V.kindProjector cross CrossVersion.full

    val mUnit = mUnitLib("munit")
    val mUnitScalacheck = mUnitLib("munit-scalacheck")
  }
}
