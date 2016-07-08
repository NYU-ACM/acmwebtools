package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.mvc.Flash

@Singleton
class Webtools extends Controller {
  def index = Action { implicit request =>
    Ok(views.html.webtools())
  }
}