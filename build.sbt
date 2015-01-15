name          := "systemz"

organization  := "functionalops"

version       := "0.1.0"

scalaVersion  := "2.11.4"

scalacOptions ++= Seq(
  "-feature",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-encoding",
  "utf8"
)

libraryDependencies ++= {
  val scalazV = "7.1.0"
  Seq(
    "org.scalaz"  %%  "scalaz-core"                 % scalazV,
    //"org.scalaz"  %%  "scalaz-effect"               % scalazV,
    //"org.scalaz"  %%  "scalaz-concurrent"           % scalazV,
    //"org.scalaz"  %%  "scalaz-stream"               % scalazV,
    "org.scalaz"  %%  "scalaz-scalacheck-binding"   % scalazV  % "test"
  )
}
