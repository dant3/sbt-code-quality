package codequality

import sbt._
import sbt.Keys._


import scala.collection.mutable


private[codequality] object tasks {
  def checkStyleTaskDef = ( streams
                          , sourceDirectory in Compile
                          , Keys.CheckStyle.format
                          , Keys.CheckStyle.outputFile
                          , Keys.CheckStyle.configFile
                          , Keys.failOnViolations) map {
    (streams, src, format, outputFile, configFile, failOnViolations) =>
      import com.puppycrawl.tools.checkstyle.Main.{main => runCheckStyle}
      import streams.log

      if (!configFile.isFile) {
        sys.error(s"Config file at ${configFile.getAbsoluteFile} is missing")
      }

      val outputFileArg = outputFile match {
        case Some(file) => "-o" :: file.getAbsolutePath :: Nil
        case None => Nil
      }

      val args = List(
        "-c", configFile.getAbsolutePath,
        "-f", format.toString,
        "-r", src.getAbsolutePath
      ) ++ outputFileArg

      log debug ("using checkstyle args " + args)
      val violations = trappingExits {
        runCheckStyle(args.toArray)
      }.get

      if (failOnViolations && violations > 0) {
        sys.error(s"CheckStyle found $violations rule violations in the code")
      } else {
        log.info(s"CheckStyle found $violations rule violations in the code")
        CheckStyleResult(violations, outputFile)
      }
  }





  def findbugsTaskDef = ( streams
                        , compile in Compile
                        , classDirectory in Compile
                        , dependencyClasspath in Compile
                        , target in Compile
                        , Keys.FindBugs.effort
                        , Keys.FindBugs.consoleOutput
                        , Keys.FindBugs.outputFile
                        , Keys.FindBugs.displayProgress
                        , Keys.failOnViolations) map {
    (streams, compile, classesDirectory, classpath, target,
     effort, consoleOutput, outputFile, displayProgress, failOnViolation) =>
      import edu.umd.cs.findbugs.FindBugs2

      val log = streams.log
      val classes = (classesDirectory ** "*.class").getPaths

      outputFile.getParentFile.mkdirs()
      outputFile.delete()

      val auxClassPath = classpath.files.foldLeft(mutable.Buffer[String]()) { (buff, cpFile) =>
        buff += "-auxclasspath" += cpFile.getAbsolutePath
      }.toList

      var args = List(/*"-help",*/
        "-noClassOk",
        s"-effort:$effort",
        "-xml:withMessages",
        "-output", outputFile.absolutePath
      ) ++ auxClassPath ++ classes

      if (displayProgress) {
        args = "-progress" +: args
      }

      log.info(s"Running findbugs on ${classes.size} classes...")
      FindBugs2.main(args.toArray)

      def bugs = parseFindbugsOutputXml(outputFile, streams.log)
      for (bug <- bugs) {
        consoleOutput match {
          case FindBugsConsoleOutput.detailed => streams.log.info(bug.detailedDescription)
          case FindBugsConsoleOutput.short => streams.log.info(bug.shortDescription)
          case FindBugsConsoleOutput.none =>
        }
      }

      val bugsCount = bugs.size
      if (bugsCount > 0) {
        streams.log.error(s"FindBugs finished, $bugsCount bugs found, see $outputFile for details.")
        if (failOnViolation) {
          sys.error(s"FindBugs found $bugsCount violations")
        }
      } else {
        streams.log.success("FindBugs finished, no bugs were found!")
      }
      bugs
  }


  private def parseFindbugsOutputXml(file: File, log: Logger) = if (file.exists()) {
    FindBugsBug.parseBugs(file)
  } else {
    log.warn("No XML report, nothing to parse")
    Seq.empty
  }






  def pmdTaskDef = ( streams
                   , sourceDirectory in Compile
                   , Keys.PMD.format
                   , Keys.PMD.outputFile
                   , Keys.PMD.ruleSets
                   , Keys.failOnViolations) map { PMDTask.run }

  private object PMDTask {
    import net.sourceforge.pmd.cli.{PMDCommandLineInterface, PMDParameters}
    import net.sourceforge.pmd.renderers.AbstractRenderer
    import net.sourceforge.pmd.util.IOUtil
    import net.sourceforge.pmd.{RuleContext, RuleSet, RulesetsFactoryUtils, PMD => PMDUtility}

    def run(streams: sbt.Keys.TaskStreams, src: File, format: PMDReportFormat.Value,
            outputFile: Option[File], ruleSets: Seq[String], failOnViolations: Boolean): PMDResult = {
      import streams.log
      import java.lang.Runtime.{ getRuntime => JavaRuntime }

      val violationsCount = try {

        val reportFileOption = outputFile match {
          case Some(reportFile) => "-reportfile" :: reportFile.getAbsolutePath :: Nil
          case None => Nil
        }

        val args = Map(
          "-threads" -> JavaRuntime.availableProcessors().toString,
          "-dir" -> src.getAbsolutePath,
          "-format" -> format.toString,
          "-rulesets" -> ruleSets.reduce(_ + "," + _)
        ).toList.flatMap{
          case (key, value) => Seq(key, value.toString)
        } ++ reportFileOption

        log debug ("using pmd args " + args)
        runPMD(args.toArray, log)
        //  log info s"PMD finished with $result violations found"
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
        PMDResult(violationsCount, outputFile)
      }
    }


    private def getStackTrace(e: Throwable): String = e.getStackTrace.foldLeft("")(_ + "\n" + _.toString)


    private def runPMD(args: Array[String], log: sbt.Logger): Int = {
      val params = PMDCommandLineInterface.extractParameters(new PMDParameters, args, "pmd")
      val configuration = PMDParameters.transformParametersIntoConfiguration(params)
      val ruleSetFactory = RulesetsFactoryUtils.getRulesetFactory(configuration)
      val oldStdOut = System.out

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

        System.setOut(oldStdOut)
        sbtRenderer.issuesCount
      }
    }


    private class SbtRenderer extends AbstractRenderer("sbt", "Sbt renderer") {
      import net.sourceforge.pmd.Report
      import net.sourceforge.pmd.util.datasource.DataSource

      var issuesCount = 0

      override def start() {}
      override def startFileAnalysis(dataSource: DataSource) {
//        log("Processing file " + dataSource.getNiceFileName(false, inputPaths), Project.MSG_VERBOSE)
      }
      override def renderFileReport(r: Report) {
        issuesCount += r.size()
      }

      override def end() {}
      override def defaultFileExtension: String = { null }
    }
  }


  def trappingExits(task: => Unit): Option[Int] = {
    case class NoExitsException(exitCode: Int) extends SecurityException

    val originalSecManager = System.getSecurityManager

    System setSecurityManager new SecurityManager() {
      import java.security.Permission

      override def checkPermission(perm: Permission) {
        val exitPermName = "exitVM"

        val permName = perm.getName
        if (permName startsWith exitPermName) {
          val exitCodeString = permName.substring((exitPermName + ".").length)
          val exitCode = exitCodeString.trim.toInt
          throw NoExitsException(exitCode)
        }
      }
    }

    try {
      task
      None
    } catch {
      case NoExitsException(exitCode) => Some(exitCode)
      case e : Throwable => throw e
    } finally {
      System setSecurityManager originalSecManager
    }
  }
}
