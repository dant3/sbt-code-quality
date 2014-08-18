credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

publishTo <<= version { v: String =>
    val repository = if (v.trim.endsWith("SNAPSHOT")) "repo/snapshots" else "repo/releases"
    Some(Resolver.file("repository", file(repository))(Resolver.mavenStylePatterns))
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
        <developer>
            <id>dant3</id>
            <name>Viacheslav Blinov</name>
            <url>http://github.com/dant3</url>
        </developer>
    </developers>