package codequality

import Keys._
import sbt._
import sbt.{Keys => sbtkeys}

object rules {
  lazy val checkStyle = common ++ Seq(
    CheckStyle.format := CheckStyleReportFormat.plain,
    CheckStyle.outputFile <<= (sbtkeys.target, Keys.CheckStyle.format) { checkStyleDefaultOutputFile },
    CheckStyle.configFile <<= sbtkeys.baseDirectory(_ / "checkstyle-config.xml"),
    Keys.checkStyle <<= tasks.checkStyleTaskDef,
    check           <<= check dependsOn Keys.checkStyle
  )


  lazy val findbugs = common ++ Seq(
    FindBugs.effort          := FindBugsEffort.default,
    FindBugs.consoleOutput   := FindBugsConsoleOutput.short,
    FindBugs.outputFile      <<= sbtkeys.target { _ / "findbugs.xml" },
    FindBugs.displayProgress := true,
    Keys.findbugs            <<= tasks.findbugsTaskDef,
    check                    <<= check dependsOn Keys.findbugs
  )


  lazy val pmd = common ++ Seq(
    PMD.format     := PMDReportFormat.text,
    PMD.outputFile := None,
    PMD.ruleSets   := Seq("basic.xml", "unusedcode.xml", "imports.xml").map((filename) => s"rulesets/java/$filename"),
    Keys.pmd       <<= tasks.pmdTaskDef,
    check          <<= check dependsOn Keys.pmd
  )

  lazy val common = Seq(
    failOnViolations := false,
    check            := {}
  )

  lazy val all = (checkStyle ++ findbugs ++ pmd).distinct



  private def checkStyleDefaultOutputFile(target: File, format: CheckStyleReportFormat.Value) = format match {
    case CheckStyleReportFormat.plain => None
    case CheckStyleReportFormat.xml => Some(target / "checkstyle-result.xml")
  }
}
