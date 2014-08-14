package codequality

import sbt._

object CodeQualityPlugin extends AutoPlugin {
    override def projectSettings: Seq[Def.Setting[_]] = List(
        CheckStyle.defaults,
        PMD.defaults
    ).flatten


    private[codequality] def trappingExits(task: => Unit): Option[Int] = {
        case class NoExitsException(exitCode: Int) extends SecurityException

        val originalSecManager = System.getSecurityManager

        System setSecurityManager new SecurityManager() {
            import java.security.Permission

            override def checkPermission(perm: Permission) {
                if (perm.getName startsWith "exitVM") {
                    val nameParts = perm.getName.split(".")
                    val exitCode = if (nameParts.size > 1) {
                        nameParts(1).toInt
                    } else {
                        Integer.MIN_VALUE
                    }
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
