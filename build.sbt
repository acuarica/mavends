
scalaVersion := "2.11.7"

//libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4"

lazy val lala = taskKey[Unit]("Downloads index")

lala := {
  "ls -lah" !
}

	<target name="fetchgzindex" depends="mkcachedir" description="Fetches the Maven Index (compressed) from a mirror using aria2.">
		<exec executable="aria2c">
			<arg value="--dir=/" />
			<arg value="--max-concurrent-downloads=16" />
			<arg value="--auto-file-renaming=false" />
			<arg value="--conditional-get=true" />
			<arg value="--file-allocation=falloc" />
			<arg value="--out=${gzindexpath}" />
			<arg value="${gzindexurl}" />
		</exec>
