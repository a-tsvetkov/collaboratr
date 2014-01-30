package actors

import scala.language.postfixOps
import scala.concurrent._, ExecutionContext.Implicits.global, duration._
import akka.actor._
import akka.util.Timeout
import play.api._, libs.iteratee._, libs.json._
import play.api.libs.concurrent.Akka
import play.api.Play.current

import models._

object DocumentWatcher {

  implicit val resolveTimeout = Timeout(100 milliseconds)

  def getOrCreateForDocument(document: Document): Future[ActorRef] = {
    Akka.system.actorSelection(getActorPath(document)).resolveOne.recover {
      case ex: ActorNotFound => Akka.system.actorOf(props(document), getActorId(document))
    }
  }

  def props(document: Document): Props = Props(classOf[DocumentWatcher], document)

  def getActorId(document: Document) = "documentWatcher." + document.id

  def getActorPath(document: Document) = "/user/" + getActorId(document)
}

class DocumentWatcher(val document: Document) extends Actor with ActorLogging {

  val (out, channel) = Concurrent.broadcast[DocumentUpdate]

  var listnersCount = 0

  def receive = {
    case Listen(user, doc) if doc == document => {
      sender ! out
      listnersCount += 1
    }
    case StopListening(user, doc) if document == document => {
      listnersCount -= 1
    }
    case upd: DocumentUpdate if upd.documentId == document.id => {
      upd.field match {
        case "title" => document.title.patch(upd.from, upd.update, upd.replaced)
        case "body" => document.body.patch(upd.from, upd.update, upd.replaced)
      }
      channel.push(upd)
    }
  }
}
