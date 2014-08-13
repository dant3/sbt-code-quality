package codequality

import sbt._
import Keys._

object CheckStyle {
    val checkStyle = TaskKey[Unit]("checkstyle", "run CheckStyle")
    val checkStyleTask = checkStyle <<=
        (streams, baseDirectory, sourceDirectory in Compile, target) map {
            (streams, base, src, target) =>
                import com.puppycrawl.tools.checkstyle.Main.{ main => CsMain }
                import streams.log

                val args = List(
                    "-c", (base / "checkstyle-config.xml").getAbsolutePath,
                    "-f", "xml",
                    "-r", src.getAbsolutePath,
                    "-o", (target / "checkstyle-result.xml").getAbsolutePath
                )
                log info ("using checkstyle args " + args)
                CodeQualityPlugin.trappingExits {
                    CsMain(args.toArray)
                }
        }

    val all = Seq(checkStyleTask)
    
    
//    def run(streams: TaskKey[TaskStreams], base: SettingKey[File], src: SettingKey[File], target: SettingKey[File])
}
