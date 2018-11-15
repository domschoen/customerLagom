package com.example.playscalajs.controllers

import javax.inject._
import com.example.playscalajs.shared.SharedMessages
import play.api.mvc._

import scala.concurrent.ExecutionContext

@Singleton
class Main @Inject()(cc: ControllerComponents)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  def index = Action {
    Ok(views.html.index("Customer Repo"))
  }

}
