package controllers


import javax.inject.Inject
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.ControllerComponents

import scala.concurrent.ExecutionContext


class ApplicationController @Inject()(cc: ControllerComponents)(implicit ec: ExecutionContext) extends AbstractController(cc) {



  def index = Action(parse.anyContent) {
    implicit request =>
      Ok(views.html.index("Chirper"))
  }


  def logging = Action(parse.anyContent) {
    implicit request =>
      request.body.asJson.foreach { msg =>
        println(s"CLIENT - $msg")
      }
      Ok("")
  }


  def userStream(userId: String) = Action {
    Ok(views.html.index("Chirper"))
  }

  def circuitBreaker = Action {
    //Ok(views.html.circuitbreaker.render())
    Ok(views.html.index("Chirper"))
  }
}
