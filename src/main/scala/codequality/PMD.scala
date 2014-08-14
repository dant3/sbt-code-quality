package codequality

import sbt.Keys._
import sbt._

object PMD {
    case class Result(violations: Int, outputFile: Option[File])
    object ReportFormat extends Enumeration {
        val text, textcolor, xml, csv, none = Value
    }


    val format = SettingKey[ReportFormat.Value]("PMD.format", "Output format used by PMD")
    val outputFile = SettingKey[Option[File]]("PMD.outputFile", "File to print output of PMD")
    val ruleSets = SettingKey[Seq[String]]("PMD.ruleSets", "File paths to PMD rulesets")
    val failOnViolations = SettingKey[Boolean]("PMD.failOnViolations", "Wherever to mark task as failed on violations")


    val pmd = TaskKey[Result]("pmd", "run PMD")


    lazy val defaults: Seq[Def.Setting[_]] = Seq(
        task,
        // setup meaningful defaults
        format := ReportFormat.text,
        outputFile := None,
        failOnViolations := false,
        ruleSets := Seq("basic.xml", "unusedcode.xml", "imports.xml").map((filename) => s"rulesets/java/$filename")
    )


    private def task = pmd <<= (streams, baseDirectory, sourceDirectory in Compile, target, format, outputFile, failOnViolations, ruleSets) map {
        Task.run
    }


    private object Task {
        import net.sourceforge.pmd.cli.{PMDCommandLineInterface, PMDParameters}
        import net.sourceforge.pmd.renderers.AbstractRenderer
        import net.sourceforge.pmd.util.IOUtil
        import net.sourceforge.pmd.{RuleContext, RuleSet, RulesetsFactoryUtils, PMD => PMDUtility}

        def run(streams: Keys.TaskStreams, base: File, src: File, target: File,
                format: ReportFormat.Value, outputFile: Option[File], failOnViolations: Boolean, ruleSets: Seq[String]): Result = {
            import streams.log

            val violationsCount =try {

                val reportFileOption = outputFile match {
                    case Some(reportFile) => "-reportfile" :: reportFile.getAbsolutePath :: Nil
                    case None => Nil
                }

                val args = List(
                    "-dir", src.getAbsolutePath,
                    "-format", format.toString,
                    "-rulesets", ruleSets.reduce(_ + "," + _)
                ) ++ reportFileOption

                log debug ("using pmd args " + args)
                runPMD(args.toArray, log)
//                log info s"PMD finished with $result violations found"
            } catch {
                case e : Throwable =>
                    val errorMsg = s"Error while running PMD: ${getStackTrace(e)}"
                    log error errorMsg
                    throw e
            } finally {
                log.info("PMD finished")
            }
            if (failOnViolations && violationsCount > 0) {
                sys.error(s"PMD found $violationsCount rule violations in the code")
            } else {
                Result(violationsCount, outputFile)
            }
        }

        private def getStackTrace(e: Throwable): String = e.getStackTrace.foldLeft("")(_ + "\n" + _.toString)

        private def runPMD(args: Array[String], log: sbt.Logger): Int = {

            val params = PMDCommandLineInterface.extractParameters(new PMDParameters, args, "pmd")
            val configuration = PMDParameters.transformParametersIntoConfiguration(params)
            val ruleSetFactory = RulesetsFactoryUtils.getRulesetFactory(configuration)

            val ruleSets = RulesetsFactoryUtils.getRuleSets(configuration.getRuleSets, ruleSetFactory, System.nanoTime)
            if (ruleSets == null) {
                log info "No rulesets given - nothing to inspect"
                0
            } else {
                import scala.collection.JavaConversions._

                val languages = { // TODO: file issue in PMD since method PMD.getLanguages could be public
                    import net.sourceforge.pmd.Rule

                    val discoverer = configuration.getLanguageVersionDiscoverer
                    for {
                        rule:Rule <- ruleSets.getAllRules
                        if RuleSet.applies(rule, discoverer.getDefaultLanguageVersion(rule.getLanguage))
                    } yield rule.getLanguage
                }
                val files = PMDUtility.getApplicableFiles(configuration, languages)
                val renderer = configuration.createRenderer
                val sbtRenderer = new SbtRenderer
                renderer.setWriter(IOUtil.createWriter(configuration.getReportFile))

                log warn s"About to process ${files.size()} files..."
                renderer.start()
                PMDUtility.processFiles(configuration, ruleSetFactory, files, new RuleContext, List(sbtRenderer, renderer))
                log warn s"Processing done! Found ${sbtRenderer.issuesCount} issue(s)"
                renderer.end()
                renderer.flush()
                sbtRenderer.issuesCount
            }
        }

        private class SbtRenderer extends AbstractRenderer("sbt", "Sbt renderer") {
            import net.sourceforge.pmd.Report
            import net.sourceforge.pmd.util.datasource.DataSource

            var issuesCount = 0

            override def start() {}
            override def startFileAnalysis(dataSource: DataSource) {
                //log("Processing file " + dataSource.getNiceFileName(false, inputPaths), Project.MSG_VERBOSE)
            }
            override def renderFileReport(r: Report) {
                issuesCount += r.size()
            }

            override def end() {}
            override def defaultFileExtension: String = { null }
        }
    }
}
