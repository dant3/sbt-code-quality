package codequality

import sbt._

case class PMDResult(violations: Int, outputFile: Option[File])


object PMDReportFormat extends Enumeration {
    val text, textcolor, xml, csv, emacs, html, summaryhtml, none = Value
}
