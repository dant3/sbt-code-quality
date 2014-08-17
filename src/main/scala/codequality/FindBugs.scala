package codequality

import sbt.Keys._
import sbt._

import scala.xml.XML

object FindBugs {
    object Effort extends Enumeration {
        val min, less, default, more, max = Value
    }
    object ConsoleOutput extends Enumeration {
        val none, short, detailed = Value
    }


    val effort = SettingKey[Effort.Value]("FindBugs.effort", "effort made by FindBugs")
    val consoleOutput = SettingKey[ConsoleOutput.Value]("FindBugs.consoleOutput", "amount of output made by FindBugs task")
    val outputFile = SettingKey[File]("FindBugs.outputFile")
    val displayProgress = SettingKey[Boolean]("FindBugs.displayProgress")
    val failOnViolation = SettingKey[Boolean]("FindBugs.failOnViolation")

    val findbugs = TaskKey[Seq[Bug]]("findbugs", "run FindBugs")


    lazy val defaults: Seq[Def.Setting[_]] = Seq(
        task,
        effort := Effort.default,
        consoleOutput := ConsoleOutput.short,
        outputFile <<= target { _ / "findbugs.xml" },
        displayProgress := true,
        failOnViolation := false
    )


    private def task = findbugs <<= (streams, compile in Compile, classDirectory in Compile, target in Compile,
                                    effort, consoleOutput, outputFile, displayProgress, failOnViolation) map {
        (streams, compile, classesDirectory, target,
         effort, consoleOutput, outputFile, displayProgress, failOnViolation) =>
            import edu.umd.cs.findbugs.FindBugs2

            val classes = (classesDirectory ** "*.class").getPaths

            outputFile.getParentFile.mkdirs()
            outputFile.delete()

            var args = List(/*"-help",*/
                s"-effort:$effort",
                "-xml:withMessages",
                "-output", outputFile.absolutePath
            ) ++ classes

            if (displayProgress) {
                args = args :+ "-progress"
            }

            FindBugs2.main(args.toArray)

            def bugs = parseFindbugsOutputXml(outputFile, streams.log)
            for (bug <- bugs) {
                consoleOutput match {
                    case ConsoleOutput.detailed => streams.log.info(bug.detailedDescription)
                    case ConsoleOutput.short => streams.log.info(bug.shortDescription)
                    case ConsoleOutput.none =>
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
        Bug.parseBugs(XML.loadFile(file))
    } else {
        log.warn("No XML report, nothing to parse")
        Seq.empty
    }


    class Bug(val sourceXml: xml.Node) {
        lazy val `type` = Bug.getFirstAttribute(sourceXml, "type").get.text
        lazy val category = Bug.getFirstAttribute(sourceXml, "category").get.text
        lazy val label = (sourceXml \ "ShortMessage").text
        lazy val at = (sourceXml \ "SourceLine" \ "Message").text

        lazy val shortDescription = at + " - " + label

        lazy val detailedDescription = {
            val descriptionPieces = for {
                bugInfoPiece <- sourceXml.child
                bugInfoPieceChild <- bugInfoPiece.child
                if bugInfoPieceChild.label == "Message"
            } yield {
                Bug.getTagPrefix(bugInfoPiece) + bugInfoPieceChild.text
            }
            descriptionPieces reduce (_ + "\n" + _)
        }
    }


    object Bug {
        def parseBugs(findbugsXml: xml.Node) = for {bug <- findbugsXml \\ "BugInstance"} yield {
            Bug(bug)
        }
        
        def apply(sourceXml: xml.Node): Bug = new Bug(sourceXml)

        private def getTagPrefix(bugInfoPiece: xml.Node): String = bugInfoPiece.label match {
            case "Class"  if isPrimary(bugInfoPiece) => "| "
            case "Method" if isPrimary(bugInfoPiece) => "| "
            case "SourceLine"                        => "+-> "
            case label                               => "|    "//leftPad(label, 8)
        }

        private def getFirstAttribute(xmlNode: xml.Node, attrName: String):Option[xml.Node] = {
            for {
                attribute <- xmlNode.attribute(attrName)
                categoryHead <- attribute.headOption
            } yield categoryHead
        }

        private def isPrimary(bugInfo: xml.Node): Boolean = bugInfo.attribute("primary") match {
            case Some(nodes) if nodes.headOption.isDefined && nodes.head.text == true.toString => true
            case _ => false
        }

        private def leftPad(str: String, len: Int) = if (str.length > len) {
            str.substring(0, len)
        } else {
            str + " " * (len - str.length)
        }
    }
}
