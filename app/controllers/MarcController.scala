package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import java.io.{ FileInputStream, File }
import scala.io.Source
import play.api.mvc.Flash
import protocol.Protocol._
import actors._
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._


@Singleton
class MarcController @Inject()(conf: play.api.Configuration) (system: ActorSystem) extends Controller {

  
  val supervisor = system.actorOf(Props[Supervisor], "Supervisor")
  val marcProps = Props(new MarcActor(conf, supervisor))
  val marc = system.actorOf(marcProps, "MARC")

  def index = Action { implicit request =>

    implicit val timeout = Timeout(5 seconds)
    supervisor ! GetProcessing
    
    val future = supervisor ? GetProcessing
    val status = Await.result(future, timeout.duration).asInstanceOf[Map[String, String]]
    
    val processing = status.filter(_._2 == "PROCESSING")
    val errors = status.filter(_._2 == "ERROR")
    val success = status.filter(_._2 == "SUCCESS")

    val files = new File(conf.underlying.getString("fs.complete")).listFiles

    Ok(views.html.marc(files, processing, errors, success))
  }

  def  generate = Action(parse.multipartFormData) { implicit request =>
  	
  	//generate the marc
  	val id = java.util.UUID.randomUUID
    val form = request.body.asFormUrlEncoded
    val marcRequest = new MarcRequest(id, form.get("repositoryid").get(0).toInt, form.get("resourceid").get(0).toInt)
    marc ! marcRequest
  	//redirect to display page
  	Redirect(routes.MarcController.index)
  }

  def download(id: String) = Action {
    Ok.sendFile(
      content = new java.io.File(conf.underlying.getString("fs.complete") + id),
      fileName = _ => s"$id"
    )    
  }
}
