package functionalops.systemz

import scalaz._
import Scalaz._
import scalaz.effect._

import javax.management._
import java.lang.instrument.Instrumentation

trait CoreTypes {
  type MBeanAction[A] = MBeanServerConnection => IO[A]
  type ListenerAction[A] = Notification => A => Unit
  type RegistrationAction[A] =
    ListenerAction[A] => ObjectName => MBeanServerConnection => A => IO[Unit]

  /**
    * A more functional interface to write javaagent code in Scala.
    *
    * Instead of implementing a +premain+ or an +agentmain+ static method in
    * a class you would override one (or one of each) of the following (sets):
    *
    * * +launch: (String, Option[Instrumentation]) => IO[Unit]+ - override
    *   this method when you care about either the arguments provided on the
    *   command line or you want to access the Instrumentation instance.
    * * +launchc: IO[Unit]+ - override if you don't care about the arguments
    *   or Instrumentation instance provided.
    * * +run: (String, Option[Instrumentation]) => IO[Unit]+ - override when you
    *   have premain and agentmain behavior to share in one implementation and
    *   need to access the arguments and/or Instrumentation instance provided.
    * * +runc: IO[Unit]+
    *
    * If you don't care about the inputs given you can just override the 'c'
    * postfix versions.
    *
    * The +launch+ methods will only be called if launched on the command line
    * like a premain class.
    */
  trait JvmAgent {
    def launch(args: String, instr: Option[Instrumentation]): IO[Unit] = launchc
    def launchc: IO[Unit] = IO.ioUnit

    def runa[A](a: A): IO[Unit] = runc
    def run(args: String, instr: Option[Instrumentation]): IO[Unit] = runc
    def runc: IO[Unit] = IO.ioUnit

    /**
      * Invoked when javaagent is provided on the command line as an option or
      * when the JAR manifest file contains a Premain-Class entry. This is
      * triggered at JVM launch.
      *
      * If this method were not available on JVM launch of the class provided
      * in javaagent CLI option or in Premain-Class manifest entry of JAR then
      * the JVM would abort launching the JVM.
      *
      * If you have overridden launch or launchc that will be called first and
      * then run or runc (whichever is provided by implementation) in that
      * order (whichever is overridden first).
      */
    @inline final def premain(args: String, instr: Instrumentation) {
      (launch(args, Option(instr)) >> run(args, Option(instr))).unsafePerformIO
    }

    /**
      * Invoked after the JVM has already launched as an agent assuming a JVM
      * implementation supports this behavior.
      */
    @inline final def agentmain(args: String, instr: Instrumentation) {
      run(args, Option(instr)).unsafePerformIO
    }
  }
}
