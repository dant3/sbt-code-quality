package codequality

import sbt.Keys._
import sbt._

object PMD {
    object ReportFormat extends Enumeration {
        val text, textcolor, xml, csv, none = Value
    }

    val format = SettingKey[ReportFormat.Value]("pmdOutputFormat", "Output format used by PMD")
    val outputFile = SettingKey[Option[File]]("pmdOutputFile", "File to print output of PMD")
    val ruleSets = SettingKey[Seq[String]]("pmdRuleSets", "File paths to PMD rulesets")

    val pmd = TaskKey[Boolean]("pmd", "run PMD")

    val defaults: Seq[Def.Setting[_]] = Seq(
        task,
        // setup meaningful defaults
        format := ReportFormat.text,
        outputFile := None,
        ruleSets := Seq("basic.xml", "unusedcode.xml", "imports.xml").map((filename) => s"rulesets/java/$filename")
    )


    def task = pmd <<= (streams, baseDirectory, sourceDirectory in Compile, target, format, outputFile, ruleSets) map {
        (streams, base, src, target, format, outputFile, ruleSets) =>
            import net.sourceforge.pmd.PMD.{run => runPMD}
            val OK = 0

            import streams.log

            val reportFileOption = outputFile match {
                case Some(reportFile) => "-reportfile" :: reportFile.getAbsolutePath :: Nil
                case None => Nil
            }

            val args = List(
                "-dir", src.getAbsolutePath,
                "-format", format.toString,
                "-rulesets", ruleSets.reduce(_ + "," + _)
            ) ++ reportFileOption

            log info ("using pmd args " + args)
            OK == runPMD(args.toArray)
    }
}
