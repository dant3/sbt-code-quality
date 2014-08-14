# sbt-code-quality: SBT plugin #

This is a fork of the sbt-code-quality.g8 project, built as a SBT plugin.

It adds support for [checkstyle](http://checkstyle.sourceforge.net/) and
[pmd](http://pmd.sourceforge.net/) to SBT.

## Setup ##

add following line to `project/plugins.sbt`

    addSbtPlugin("de.corux" %% "sbt-code-quality" % "0.2.0")
    resolvers += "corux-releases" at "http://tomcat.corux.de/nexus/content/repositories/releases/"
    resolvers += "corux-snapshots" at "http://tomcat.corux.de/nexus/content/repositories/snapshots/"

## Usage ##

### Checkstyle ###

You can check your code with [checkstyle](http://checkstyle.sourceforge.net/)
by typing `sbt checkstyle`. If xml format is selected, by default, the result file is `target/checkstyle-result.xml`. You can set any output file you wish by changing `codequality.CheckStyle.outputFile` setting.

You must have a checkstyle configuration file. By default the file must be called `checkstyle-config.xml` and should be under your projects root folder. However, you can supply any file by changing the setting key `codequality.CheckStyle.configFile`.

You can also select any output format you wish by changing `codequality.CheckStyle.format` setting, and fail build on violations by setting `codequality.CheckStyle.failOnViolations` to `true`.


### PMD ###

You can check your code with [pmd](http://pmd.sourceforge.net/) by typing
`sbt pmd`. The result file is specified using `codequality.PMD.outputFile` setting key.

You might want to have a custom pmd rule set. The file can be selected by using `codequality.PMD.ruleSets` setting key.

You can also select any output format you wish by changing `codequality.PMD.format` setting, and fail build on violations by setting `codequality.PMD.failOnViolations` to `true`.

### Putting it all together ###

You can run all checks of your code using `sbt check`. 
