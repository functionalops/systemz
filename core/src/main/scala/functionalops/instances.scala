package functionalops.systemz

import scalaz._
import Scalaz._

import javax.management.ObjectName

trait CoreInstances extends CoreClasses {
  implicit val integerShow: Show[Integer] = Show.shows(_.toString)
  implicit val ObjectNameShow: Show[ObjectName] = Show.shows(_.toString)
  implicit def SetShow[A]: Show[scala.collection.mutable.Set[A]] = Show.shows(_.toString)
}
