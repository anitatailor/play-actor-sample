package controllers

import com.google.inject._
import play.api._
import play.api.mvc._
import scala.concurrent.{ ExecutionContext, Future, Promise }
import scala.concurrent.duration._
import actors._
import actors.KeyLookupActor._
import akka.pattern.{ ask, pipe }
import akka.util.Timeout
import scala.util.{ Success, Failure }
import play.api.Logger
import akka.actor._
import gnu.trove.map.hash.THashMap

@Singleton
class KEYController @Inject() (cc: ControllerComponents, system: ActorSystem)(implicit exec: ExecutionContext) extends AbstractController(cc) {

  Logger.info(s"KEYController started ...")
  implicit val timeout = Timeout(30 seconds)
  val logger: Logger = Logger(this.getClass())
  val keyLookupActor = system.actorOf(Props(new KeyLookupActor("http://localhost:9000/testkey")), name = "KeyLookupActor")

  def testKey = Action {
    Ok("key1")
  }
  def getKey = Action.async {
    (keyLookupActor ? GetLastResponse).mapTo[String].map(resp => Ok(resp))
  }
}
