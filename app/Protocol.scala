package protocol

import java.util.UUID

object Protocol {
  case class MarcRequest(id: UUID, repositoryId: Int, resourceId: Int)	
  case class AddRequest(id: String)
  case class RemoveRequest(id: String)
  case class ErrorRequest(id: String)
  case object GetProcessing
}