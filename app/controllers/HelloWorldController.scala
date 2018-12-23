package controllers

import com.google.inject._
import play.api._
import play.api.mvc._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HelloWorldController @Inject() (cc: ControllerComponents) extends AbstractController(cc) {

  def index = Action {
    Ok("Hello, world")
  }

}
