package functionalops.systemz

import scalaz._
import Scalaz._
import scalaz.effect._

import java.lang.management._
import javax.management._
import javax.management.remote._

import java.util.TreeSet

import functionalops.systemz.core._

trait MainDefaults {
  protected def host          = "localhost"
  // scalastyle:off magic.number
  protected def port          = 19200
  protected def numBrokers    = 4
  protected def defaultSleep  = 2000
  protected def bindPort      = 45678
  // scalastyle:on magic.number
  protected def objectName    = ObjectName.WILDCARD
  protected lazy val url      = createJMXUrl(host, port)


  implicit def disjunctionTUShow[E, A]: Show[E \/ A] =
    Show.shows(_.toString)
}

object JMXRunnerMain extends SafeApp with MainDefaults {
  private val concatedAction = (c: MBeanServerConnection) =>
    printDomainsAction(c) >> printDomainsAction(c)

  private def printDomainsAction(c: MBeanServerConnection) =
    IO.putStrLn("Domains: " + c.getDomains.mkString("|"))

  private def printMBeansCount(c: MBeanServerConnection) =
    IO.putStrLn("MBean count: " + c.getMBeanCount.toString)

  override def runc = for {
    _ <- runJMXAction(url)(concatedAction)
  } yield ()
}

object JMXPollerMain extends SafeApp with MainDefaults {
  import scala.collection.JavaConverters._

  private def ioDomainCount(c: MBeanServerConnection) =
    IO(c.getDomains.size)

  private def ioMBeanCount(c: MBeanServerConnection) =
    IO(c.getMBeanCount)

  private def ioObjectNames(c: MBeanServerConnection) =
    IO(c.queryNames(null, null).asScala)

  private def ioAttributes(c: MBeanServerConnection, ns: scala.collection.mutable.Set[ObjectName]) =
    IO {
      for (n <- ns)
      yield c.getMBeanInfo(n).getAttributes map { _.getName }
    }

  private def puts[A](label: String, io: IO[A])(implicit s: Show[A]): IO[Unit] =
    for {
      a <- io
      _ <- IO.putStrLn(s"${label}: ${s.shows(a)}")
    } yield ()

  private def putsAndYield[A](label: String, io: IO[A])(implicit s: Show[A]): IO[A] =
    for {
      a <- io
      _ <- IO.putStrLn(s"${label}: ${s.shows(a)}")
    } yield a

  private def action: MBeanAction[Unit] =
    (c: MBeanServerConnection) =>
      for {
        _ <- puts("Domain Count", ioDomainCount(c))
        _ <- puts("MBean Count", ioMBeanCount(c))
        names <- putsAndYield("Names", ioObjectNames(c))
        attrs <- putsAndYield("Attributes", ioAttributes(c, names))
        _ <- IO.putStrLn(s"Attribute Count: ${attrs.size}")
      } yield ()

  override def runc = for {
    _ <- runJMXAction(url)(action)
  } yield ()
}

object JMXNotificationsMain extends SafeApp with MainDefaults {
  private def callback[A]: Notification => A => Unit =
    (n) => a => Console.println(List(a, n).mkString(">> "))

  private lazy val threadMXBean = ManagementFactory.getThreadMXBean

  private def printThreadCount(tb: ThreadMXBean): IO[Unit] =
    IO.putStrLn(s"Thread Count: ${tb.getThreadCount}")

  private def toThreadCount(m: ThreadMXBean) = m.getThreadCount

  def action[A](seed: A)(c: MBeanServerConnection): IO[Unit] =
    for {
      _ <- printThreadCount(threadMXBean)
      _ <- registerJMXListener[A](callback)(objectName)(c)(seed)
      _ <- IO { Thread.sleep(defaultSleep) }
      _ <- printThreadCount(threadMXBean)
    } yield ()

  override def runc = for {
    r <- createJMXRunner(host, port)(action(0) _)
    _ <- IO.putStrLn(r.shows)
  } yield ()
}

object JMXThreadInfoMain extends SafeApp with MainDefaults {
  private lazy val threadMXBean = ManagementFactory.getThreadMXBean
  private lazy val threads: Array[ThreadInfo] = threadMXBean.dumpAllThreads(true, true)
  private def threadSummary(s: String)(t: ThreadInfo) =
    s"[${s}] ${t.getThreadId} - ${t.getThreadName} - ${t.getThreadState}: ${t.getBlockedCount}"

  private def deadlockedThreads: Array[Long] =
    threadMXBean.findDeadlockedThreads

  private def monitorDeadlockedThreads: Array[Long] =
    threadMXBean.findMonitorDeadlockedThreads

  private def toThreadInfos(tids: Array[Long]) =
    if (tids == null) { Array.empty[ThreadInfo] }
    else { threadMXBean.getThreadInfo(tids) }

  private def tcSummary(desc: String)(count: Long) =
    s">>> ${desc}: ${count}"

  private def monitorDeadlockedThreadSummaries: Array[String] =
    toThreadInfos(monitorDeadlockedThreads) map threadSummary("Monitor Deadlocked Threads")

  private def deadlockedThreadSummaries: Array[String] =
    toThreadInfos(deadlockedThreads) map threadSummary("Deadlocked Threads")

  private def daemonThreadCount: Int        = threadMXBean.getDaemonThreadCount
  private def allThreadCount: Int           = threadMXBean.getThreadCount
  private def peakThreadCount: Int          = threadMXBean.getPeakThreadCount
  private def totalStartedThreadCount: Long = threadMXBean.getTotalStartedThreadCount

  private def action(c: MBeanServerConnection): IO[Unit] = for {
    _ <- IO.putStrLn((threads map threadSummary("All Threads")).mkString("\n"))
    _ <- IO.putStrLn(deadlockedThreadSummaries.mkString("\n"))
    _ <- IO.putStrLn(monitorDeadlockedThreadSummaries.mkString("\n"))
    _ <- IO.putStrLn(tcSummary("All Threads")(allThreadCount))
    _ <- IO.putStrLn(tcSummary("Daemon Threads")(daemonThreadCount))
    _ <- IO.putStrLn(tcSummary("Peak Threads")(peakThreadCount))
    _ <- IO.putStrLn(tcSummary("Total Started Threads")(totalStartedThreadCount))
  } yield ()

  override def runc = for {
    _ <- runJMXAction(url)(action)
  } yield ()
}

object StreamMain extends SafeApp with MainDefaults {
  import java.net.InetSocketAddress
  import scalaz.concurrent.Task
  import scalaz.stream._
  import scalaz.stream.Process._
  import scodec.bits.ByteVector

  val address = new InetSocketAddress(bindPort)
  implicit val cg = nio.DefaultAsynchronousChannelGroup

  def handleClient(c: Process[Task, Exchange[ByteVector, ByteVector]]): Process[Task, Unit] =
    c flatMap { e =>
      val bla = e.read
      println(bla)
      bla.to(e.write).onFailure { _ => Process.empty }
    }

  override def runc: IO[Unit] = IO {
    val s = nio.server(address).map (handleClient _)
    merge.mergeN(s).runLog.run
  }
}
