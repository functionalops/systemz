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
  Seq(
    "org.scalaz"  %%  "scalaz-core"                 % scalazV,
    "org.scalaz"  %%  "scalaz-scalacheck-binding"   % scalazV  % "test"
  )
}
