package codequality

import sbt.File

import scala.xml.XML


object FindBugsEffort extends Enumeration {
    val min, less, default, more, max = Value
}
object FindBugsConsoleOutput extends Enumeration {
    val none, short, detailed = Value
}


class FindBugsBug(val sourceXml: xml.Node) {
    lazy val `type` = FindBugsBug.getFirstAttribute(sourceXml, "type").get.text
    lazy val category = FindBugsBug.getFirstAttribute(sourceXml, "category").get.text
    lazy val label = (sourceXml \ "ShortMessage").text
    lazy val at = (sourceXml \ "SourceLine" \ "Message").text

    lazy val shortDescription = at + " - " + label

    lazy val detailedDescription = {
        val descriptionPieces = for {
            bugInfoPiece <- sourceXml.child
            bugInfoPieceChild <- bugInfoPiece.child
            if bugInfoPieceChild.label == "Message"
        } yield {
            FindBugsBug.getTagPrefix(bugInfoPiece) + bugInfoPieceChild.text
        }
        descriptionPieces reduce (_ + "\n" + _)
    }
}


object FindBugsBug {
    def parseBugs(file: File):Seq[FindBugsBug] = parseBugs(XML.loadFile(file))

    def parseBugs(findbugsXml: xml.Node):Seq[FindBugsBug] = for {bug <- findbugsXml \\ "BugInstance"} yield {
        FindBugsBug(bug)
    }

    def apply(sourceXml: xml.Node): FindBugsBug = new FindBugsBug(sourceXml)

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
