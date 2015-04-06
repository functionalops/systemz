package functionalops.systemz

import scalaz._
import Scalaz._

import scodec.bits._
import scodec.codecs._

object kafka {
  type TopicName = String
  type Broker = Int
  sealed trait ApiKey
  object ApiKey {
    private case object MetadataRequest extends ApiKey
    private case object ProduceRequest extends ApiKey
    private case object FetchRequest extends ApiKey
    private case object OffsetRequest extends ApiKey
    private case object OffsetCommitRequest extends ApiKey
    private case object OffsetFetchRequest extends ApiKey

    @inline def metadata: ApiKey = MetadataRequest
    @inline def produce: ApiKey = ProduceRequest
    @inline def fetch: ApiKey = FetchRequest
    @inline def offset: ApiKey = OffsetRequest
    @inline def offsetCommit: ApiKey = OffsetCommitRequest
    @inline def offsetFetch: ApiKey = OffsetFetchRequest

    @inline def apply(i: Int): Option[ApiKey] = i match {
      case 0 => produce.some
      case 1 => fetch.some
      case 2 => offset.some
      case 3 => metadata.some
      case 8 => offsetCommit.some
      case 9 => offsetFetch.some
      case _ => none
    }
  }

  sealed trait ApiVersion
  object ApiVersion {
    private case object Version0 extends ApiVersion

    @inline def zero: ApiVersion = Version0

    @inline def apply(i: Int): Option[ApiVersion] = i match {
      case 0 => zero.some
      case _ => none
    }
  }

  sealed trait CorrelationId
  object CorrelationId {
    private case class RequestId(id: Int) extends CorrelationId
    @inline def apply(id: Int): CorrelationId = RequestId(id)
  }

  sealed trait ClientId
  object ClientId {
    private case class ApplicationId(id: String) extends ClientId

    @inline def apply(id: String): ClientId = ApplicationId(id)
  }

  sealed trait RequestMessage
  object RequestMessage {
    private case class Metadata(topics: List[TopicName]) extends RequestMessage
    private case class Produce() extends RequestMessage
    private case class Fetch() extends RequestMessage
    private case class Offset() extends RequestMessage
    private case class OffsetCommit() extends RequestMessage
    private case class OffsetFetch() extends RequestMessage

    @inline def metadata(topics: List[TopicName]): RequestMessage = Metadata(topics)
    @inline def produce(): RequestMessage = Produce()
    @inline def fetch(): RequestMessage = Fetch()
    @inline def offset(): RequestMessage = Offset()
    @inline def offsetCommit(): RequestMessage = OffsetCommit()
    @inline def offsetFetch(): RequestMessage = OffsetFetch()
  }

  sealed trait ResponseMessage
  object ResponseMessage {
    private case class Metadata(
        broker: Broker
      , topicMetadata: TopicMetadata
      , partitionMetadata: PartitionMetadata) extends ResponseMessage
  }

  case class Request(
      key: ApiKey
    , version: ApiVersion
    , correlationId: CorrelationId
    , clientId: ClientId
    , message: RequestMessage)

  case class Response(
      correlationId: CorrelationId
    , message: ResponseMessage)


  sealed trait Metadata
  case class TopicMetadata() extends Metadata
  case class PartitionMetadata() extends Metadata
}
