
scalaVersion := "2.11.7"

//libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4"

lazy val lala = taskKey[Unit]("Downloads index")

lala := {
  "ls -lah" !
}
