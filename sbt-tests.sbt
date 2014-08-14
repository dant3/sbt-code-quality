// scripted-test settings
scriptedSettings

scriptedLaunchOpts ++= Seq("-Xmx1024m", "-XX:PermSize=512m")

//scriptedBufferLog := false

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