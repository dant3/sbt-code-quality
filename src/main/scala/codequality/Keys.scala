package codequality

import sbt._

object Keys {
  val check = TaskKey[Unit]("check", "run all code-quality checks")

  val checkStyle = TaskKey[CheckStyleResult]("check-style", "run CheckStyle")
  val findbugs = TaskKey[Seq[FindBugsBug]]("findbugs", "run FindBugs")
  val pmd = TaskKey[PMDResult]("pmd", "run PMD")

  val failOnViolations = SettingKey[Boolean]("failOnViolations", "Wherever to mark check tasks as failed on violations")


  object CheckStyle {
    val format = SettingKey[CheckStyleReportFormat.Value]("CheckStyle.format", "Output format used by CheckStyle")
    val outputFile = SettingKey[Option[File]]("CheckStyle.outputFile", "File to print output of CheckStyle")
    val configFile = SettingKey[File]("CheckStyle.configFile", "CheckStyle configuration file")
  }

  object FindBugs {
    val effort = SettingKey[FindBugsEffort.Value]("FindBugs.effort", "effort made by FindBugs")
    val consoleOutput = SettingKey[FindBugsConsoleOutput.Value]("FindBugs.consoleOutput", "amount of output made by FindBugs task")
    val outputFile = SettingKey[File]("FindBugs.outputFile")
    val displayProgress = SettingKey[Boolean]("FindBugs.displayProgress")
    val failOnViolation = SettingKey[Boolean]("FindBugs.failOnViolation")
  }

  object PMD {
    val format = SettingKey[PMDReportFormat.Value]("PMD.format", "Output format used by PMD")
    val outputFile = SettingKey[Option[File]]("PMD.outputFile", "File to print output of PMD")
    val ruleSets = SettingKey[Seq[String]]("PMD.ruleSets", "File paths to PMD rulesets")
  }
}
