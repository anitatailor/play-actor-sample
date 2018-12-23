package actors

import akka.actor._
import scala.util.{ Success, Failure }
import scala.concurrent.{ ExecutionContext, Future, Promise }
import ExecutionContext.Implicits.global
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import play.api.libs.ws._
import play.api.libs.ws.ahc.AhcWSClient
import scala.concurrent.Await
import scala.concurrent.duration._
import play.api.Logger

object KeyLookupActor {
  def props = Props[KeyLookupActor]
  case object RetryKey
  case object GetLastResponse
}

class KeyLookupActor(url: String) extends Actor {
  import KeyLookupActor._

  implicit val materializer = ActorMaterializer()
  var wsClient: AhcWSClient = null
  var retryKeyCancellable: Cancellable = null
  var lastResp: Option[String] = None

  override def preStart() {
    retryKeyCancellable = context.system.scheduler.schedule(2.seconds, 10.seconds, self, RetryKey)
    wsClient = AhcWSClient()
  }

  override def postStop() {
    retryKeyCancellable.cancel()
    wsClient.close()
  }

  def receive = {
    case RetryKey =>
      Logger.info(s"Retrying key lookup at url : $url")
      val stime = System.currentTimeMillis()
      wsClient.url(url).get()
        .map(keyResp => {
          lastResp = Some(keyResp.body)
          val start_dur = System.currentTimeMillis() - stime
          Logger.info(s"Got key : ${keyResp} in millis: ${start_dur}")
        })
    case GetLastResponse =>
      Logger.info(s"someone asked for last response")
      sender() ! lastResp.getOrElse("none")
  }
}
