package actors

import akka.actor.{ Props, Actor, ActorRef }
import play.api._
import protocol.Protocol._
import scala.sys.process._
import java.io._
import javax.inject._

class Supervisor extends Actor {
  var requestStatus = Map[String,String]()
  
  def receive = {
    case a: AddRequest => { requestStatus = requestStatus + (a.id -> "PROCESSING") }

    case r: RemoveRequest => { requestStatus = requestStatus - r.id }

    case e: ErrorRequest => { requestStatus = requestStatus + (e.id -> "ERROR") }

    case s: RequestSuccessful => { requestStatus = requestStatus + (s.id -> "SUCCESS") }

    case GetProcessing => {

      sender ! requestStatus
    }

    case _ =>
  }
}

class MarcActor (conf: play.api.Configuration, supervisor: ActorRef) extends Actor {
  def receive = {
  	case m: MarcRequest => {
  	  generateMarc(m)   
  	}
  	case _ => 
  }

  def generateMarc(mr: MarcRequest) {
    val repo = mr.repositoryId.toString 
    val resource = mr.resourceId.toString
    val rId = repo + "_" + resource
    supervisor ! AddRequest(rId)
    
    val errorWriter = new FileWriter("/opt/aspace-marc-cli/error.log")

    var error = false

    val processLogger = ProcessLogger(
    	(o: String) => {  },
    	(e: String) => {  
        error = true
        errorWriter.append(e + "\n")
        errorWriter.flush
      }
    )

    Seq("ruby", conf.underlying.getString("marc.location"), repo, resource) ! processLogger

    errorWriter.close

    error match {
      case true => { 
        supervisor ! RemoveRequest(rId)
        supervisor ! ErrorRequest(rId)
      }
      case false => {
        supervisor ! RemoveRequest(rId)
        supervisor ! RequestSuccessful(rId)
      }
    }
    

  }
}