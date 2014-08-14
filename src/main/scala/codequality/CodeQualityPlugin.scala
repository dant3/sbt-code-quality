package codequality

import sbt._

object CodeQualityPlugin extends Plugin {
    def all: Seq[Def.Setting[_]] = List(
        CheckStyle.all,
        PMD.defaults
    ).flatten


    def trappingExits[T](task: => T): Option[T] = {
        case class NoExitsException() extends SecurityException

        val originalSecManager = System.getSecurityManager

        System setSecurityManager new SecurityManager() {
            import java.security.Permission

            override def checkPermission(perm: Permission) {
                if (perm.getName startsWith "exitVM") throw NoExitsException()
            }
        }

        try {
            Some(task)
        } catch {
            case _: NoExitsException => None
            case e : Throwable => throw e
        } finally {
            System setSecurityManager originalSecManager
        }
    }
}
