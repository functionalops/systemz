libraryDependencies ++= {
  val scalazV = systemz.Versions.scalaz
  val scalazStreamV = systemz.Versions.scalazStream
  Seq(
    "org.scalaz"          %%  "scalaz-core"                 % scalazV,
    "org.scalaz"          %%  "scalaz-effect"               % scalazV,
    "org.scalaz"          %%  "scalaz-scalacheck-binding"   % scalazV  % "test"
  )
}

resolvers ++= Seq(
  "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"
)

packageOptions in (Compile, packageBin) ++= Seq(
  Package.ManifestAttributes( "Premain-Class" -> "functionalops.systemz.core.LaunchAgent" )
)
