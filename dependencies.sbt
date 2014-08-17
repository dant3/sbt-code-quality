resolvers ++= Seq(/*"snapshots", */"releases").map(Resolver.sonatypeRepo)

libraryDependencies ++= Seq(
    "com.puppycrawl.tools" % "checkstyle" % "5.7",
    "net.sourceforge.pmd" % "pmd" % "5.1.2",
    "com.google.code.findbugs" % "findbugs" % "3.0.0" withSources(),
    // --- tests --- //
    "org.specs2" %% "specs2" % "2.4" % "test"
)
