

sbtPlugin := true

name := "sbt-code-quality"

organization := "de.corux"

version := "0.2.3-SNAPSHOT"

scalaVersion := "2.10.4"

crossScalaVersions := Seq("2.10.4")

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

publishTo <<= version { v: String =>
val nexus = "http://tomcat.corux.de/nexus/"
if (v.trim.endsWith("SNAPSHOT"))
  Some("snapshots" at nexus + "content/repositories/snapshots")
else                            
  Some("releases" at nexus + "content/repositories/releases")
}

pomExtra := <url>https://github.com/corux/sbt-code-quality</url>
    <licenses>
        <license>
            <name>Apache 2.0</name>
            <url>http://www.apache.org/licenses/LICENSE-2.0.html</url>
            <distribution>repo</distribution>
        </license>
    </licenses>
    <scm>
        <url>scm:git:git@github.com:corux/sbt-code-quality.git</url>
        <connection>scm:git:git@github.com:corux/sbt-code-quality.git</connection>
    </scm>
    <developers>
        <developer>
            <id>corux</id>
            <name>Tobias Wallura</name>
            <url>http://www.corux.de/</url>
        </developer>
    </developers>



// scripted-test settings
scriptedSettings

scriptedLaunchOpts ++= Seq("-Xmx1024m", "-XX:PermSize=512m")

//scriptedBufferLog := false

//sbtTestDirectory <<= baseDirectory (_ / "main/sbt-test")

// TODO reorganize tests better
// group by test config type
scriptedDependencies <<= ( sbtTestDirectory
    , streams
    , organization
    , name
    , version
    , sbtVersion) map {
    (dir,s, org, n, v, sbtv) =>
        val tests = for {
            testGroup   <- dir.listFiles(DirectoryFilter)
            testCaseDir <- testGroup.listFiles(DirectoryFilter)
            if (testCaseDir ** "*.sbt").get.size > 0 || (testCaseDir / "project").isDirectory
        } yield testCaseDir
        tests foreach { test =>
            val project = test / "project"
            project.mkdirs()
            val pluginsFile = project / "auto_plugins.sbt"
            val propertiesFile = project / "build.properties"
            pluginsFile.delete()
            propertiesFile.delete()
            IO.write(pluginsFile,
                """addSbtPlugin("%s" %% "%s" %% "%s")""" format (org, n, v))
            IO.write(propertiesFile, """sbt.version=%s""" format sbtv)
        }
}

scriptedDependencies <<= scriptedDependencies dependsOn publishLocal