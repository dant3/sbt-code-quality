package codequality

import sbt.Keys._
import sbt._

object CheckStyle {
    object ReportFormat extends Enumeration {
        val plain, xml = Value
    }

    val format = SettingKey[ReportFormat.Value]("CheckStyle.format", "Output format used by CheckStyle")
    val outputFile = SettingKey[Option[File]]("CheckStyle.outputFile", "File to print output of CheckStyle")
    val configFile = SettingKey[File]("CheckStyle.configFile", "CheckStyle configuration file")

    val checkStyle = TaskKey[Int]("checkstyle", "run CheckStyle")

    val defaults = Seq(
        format := ReportFormat.plain,
        configFile <<= baseDirectory(_ / "checkstyle-config.xml"),
        defaultOutputFile,
        checkStyleTask
    )

    private def checkStyleTask = checkStyle <<=
        (streams, sourceDirectory in Compile, format, outputFile, configFile) map {
            (streams, src, format, outputFile, configFile) =>
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
                CodeQualityPlugin.trappingExits {
                    runCheckStyle(args.toArray)
                }.get
        }

    private def defaultOutputFile = outputFile <<= (target, format) {
        (target, format) => format match {
            case ReportFormat.plain => None
            case ReportFormat.xml => Some(target / "checkstyle-result.xml")
        }
    }
}
