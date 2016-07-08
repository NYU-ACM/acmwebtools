package actors

import akka.actor.{ Props, Actor, ActorRef }
import protocol.Protocol._
import scala.sys.process._

object MarcActor {
  def props = Props[MarcActor]
}

class MarcActor extends Actor {
  def receive = {
  	case m: MarcRequest => {
  	  generateMarc(m)   
  	}
  	case _ => println("MARC-ACTOR")
  }

  def generateMarc(mr: MarcRequest) {
    try{
      println("MARC_ACTOR-MARC_REQUEST " + mr)
      
      val processLogger = ProcessLogger(
      	(o: String) => println(o),
      	(e: String) => println(s"ERR: $e")
      )

      val repo = mr.repositoryId.toString	
      val resource = mr.resourceId.toString

      Seq("ruby", "/vagrant/aspace-marc-cli/download_resource.rb", repo, resource) ! processLogger

    } catch {
      case e: Exception => println(e)
    }
  }
}