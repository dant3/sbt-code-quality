package codequality

import sbt._

object CheckStyleReportFormat extends Enumeration {
    val plain, xml = Value
}

case class CheckStyleResult(violations: Int, outputFile: Option[File])
