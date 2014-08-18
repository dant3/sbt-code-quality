package codequality

import sbt._

object CodeQualityPlugin extends AutoPlugin {
    override lazy val projectSettings: Seq[Def.Setting[_]] = List(
        CodeQuality.defaults
    ).flatten


    private[codequality] def trappingExits(task: => Unit): Option[Int] = {
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
