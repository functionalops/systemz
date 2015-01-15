package functionalops

import scalaz._
import Scalaz._
import scalaz.effect._

import java.lang.instrument.Instrumentation

trait Types {

  /**
    * A more functional interface to write javaagent code in Scala.
    *
    * Instead of implementing a +premain+ or an +agentmain+ static method in
    * a class you would override one (or one of each) of the following (sets):
    *
    * * +spawn: (String, Option[Instrumentation]) => IO[Unit]+
    * * +spawnc: IO[Unit]+
    * * +run: (String, Option[Instrumentation]) => IO[Unit]+
    * * +runc: IO[Unit]+
    *
    * If you don't care about the inputs given you can just override the 'c'
    * postfix versions.
    *
    * The +spawn+ methods will only be called if launched on the commond line
    * like a premain class.
    */
  trait JvmAgent {
    def spawn(args: String, instr: Option[Instrumentation]): IO[Unit] = spawnc
    def spawnc: IO[Unit] = IO.ioUnit

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
      */
    @inline final def premain(args: String, instr: Instrumentation) {
      (spawn(args, Option(instr)) >> run(args, Option(instr))).unsafePerformIO
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
