scalacOptions ++= Seq(
  "-feature",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-encoding",
  "utf8"
)

libraryDependencies ++= {
  val scalazV = systemz.Versions.scalaz
  val scalazStreamV = systemz.Versions.scalazStream
  val scodecV = systemz.Versions.scodecCore
  Seq(
    "org.scalaz"        %%  "scalaz-core"                 % scalazV,
    "org.scalaz"        %%  "scalaz-effect"               % scalazV,
    //"org.scalaz"        %%  "scalaz-concurrent"           % scalazV,
    "org.scalaz.stream" %%  "scalaz-stream"               % scalazStreamV,
    "org.typelevel"     %% "scodec-core"                  % scodecV,
    "org.scalaz"  %%  "scalaz-scalacheck-binding"   % scalazV  % "test"
  )
}
