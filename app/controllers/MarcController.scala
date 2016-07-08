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

@Singleton
class MarcController @Inject() (system: ActorSystem) extends Controller {

  val marc = system.actorOf(MarcActor.props, "Marc-Actor")

  def index = Action { implicit request =>
    val files = new File("/opt/fs/marc").listFiles
    Ok(views.html.marc(files))
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
      content = new java.io.File(s"/opt/fs/marc/$id"),
      fileName = _ => s"$id"
    )    
  }
}
