package protocol

import java.util.UUID

object Protocol {
  case class MarcRequest(id: UUID, repositoryId: Int, resourceId: Int)	
}