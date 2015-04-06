scalacOptions ++= Seq(
  "-feature",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-encoding",
  "utf8"
)

libraryDependencies ++= {
  Seq(
    "org.scalaz"        %%  "scalaz-core"                 % systemz.Versions.scalaz,
    "org.scalaz"        %%  "scalaz-effect"               % systemz.Versions.scalaz,
    "org.scalaz"        %%  "scalaz-concurrent"           % systemz.Versions.scalaz,
    "org.scalaz.stream" %%  "scalaz-stream"               % systemz.Versions.scalazStream,
    "org.scalaz"        %%  "scalaz-scalacheck-binding"   % systemz.Versions.scalaz % "test"
  )
}
