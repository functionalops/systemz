import sbt._
import Keys._

package systemz {
  object Parent {
    def version       = "0.1.0"
    def organization  = "functionalops"
    def name          = "systemz"
    def scalaVersion  = "2.11.6"
    def scalacOptions = Seq(
      "-feature",
      "-unchecked",
      "-deprecation",
      "-Xfatal-warnings",
      "-Xlint",
      "-encoding",
      "utf8"
    )
    def license       = ("BSD", url("https://github.com/functionalops/systemz/blob/master/LICENSE"))
  }

  object Versions {
    def scalaz        = "7.1.1"
    def parboiled     = "1.1.6"
    def scalatest     = "2.2.1"
    def scalazStream  = "0.7a"
    def scodecCore    = "1.6.0"
  }

  object Build extends Build {
    /* default options at parent level */
    lazy val defaultSettings =
      Defaults.defaultSettings ++
        Seq(
          version       := Parent.version,
          organization  := Parent.organization,
          scalaVersion  := Parent.scalaVersion,
          scalacOptions := Parent.scalacOptions,
          licenses      += Parent.license,
          publishTo     := Some("Systemz Bintray Repo" at "https://dl.bintray.com/functionalops/systemz"),
          resolvers     := Seq("Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases")
        )

    /* aggregate/subproject spec */
    lazy val parent = Project("systemz",
      file("."),
      settings = defaultSettings
    )
    .aggregate(core, cloud, management, examples)

    lazy val core = Project("systemz-core",
      file("core"),
      settings = defaultSettings)

    lazy val cloud = Project("systemz-cloud",
      file("cloud"),
      settings = defaultSettings).dependsOn(core)

    lazy val management = Project("systemz-management",
      file("management"),
      settings = defaultSettings).dependsOn(core)

    lazy val examples = Project("systemz-examples",
      file("examples"),
      settings = defaultSettings).dependsOn(core)
  }
}
