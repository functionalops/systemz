package functionalops.systemz

import scalaz._
import Scalaz._
import scalaz.effect._

import javax.management._
import javax.management.remote._

trait CoreFunctions extends CoreInstances {

  /**
    * Creates a well formed JMX URL from given +host+ and +port+ arguments
    */
  def createJMXUrl(host: String, port: Int): JMXServiceURL =
    new JMXServiceURL(s"service:jmx:rmi:///jndi/rmi://${host}:${port}/jmxrmi")

  /**
    * Constructs a JMX +NotificationListener+ class implementation from given
    * function argument +f+ which takes a +Notifiation+ value and a _handback_
    * context value that is already of type +A+.
    */
  def createJMXListener[A](f: ListenerAction[A]): NotificationListener =
    new NotificationListener {
      override def handleNotification(n: Notification, o: Object): Unit =
        f(n)(o.asInstanceOf[A]) // This is ugly :( Why Java, why?
    }

   /**
    * Registers given function callback as a JMX notification listener after
    * wrapping it up appropriately in the correct interface and registering
    * it via the +MBeanServerConnection+ given.
    */
  def registerJMXListener[A]: RegistrationAction[A] =
    (f) => (o) => (c) => (hb) =>
      IO(c.addNotificationListener(o, createJMXListener(f), null, hb)) // ZOMG null!!!???

  /**
    * Run given +action+ against the given +jmxUrl+ cleaning up the connection
    * after itself. This is ideal for one off JMX tasks for polling with _quick_
    * in-and-out-needs.
    */
  def runJMXAction[A](jmxUrl: JMXServiceURL)(action: MBeanAction[A]): IO[Throwable \/ A] =
    IO(initJMX(jmxUrl)).bracket(closeJMX)(useMBeanServerConn(_)(action)).catchLeft

  /**
   * Create a JMX runner for the given +host+ and +port+.
   */
  def createJMXRunner[A](host: String, port: Int): MBeanAction[A] => IO[Throwable \/ A] =
    runJMXAction(createJMXUrl(host, port)) _

  /**
   * Initialize JMX connector given `JMXServiceURL`.
   */
  def initJMX(u: JMXServiceURL): JMXConnector =
    JMXConnectorFactory.connect(u)

  /**
    * Given `JMXConnector` initialized earlier, return a `IO[Unit]` that
    * will close the connector upon performing the action.
    */
  def closeJMX: JMXConnector => IO[Unit] =
    (c: JMXConnector) => IO(c.close)

  private def useMBeanServerConn[A]: JMXConnector => MBeanAction[A] => IO[A] =
    (c) => (action) => action(c.getMBeanServerConnection)
}
