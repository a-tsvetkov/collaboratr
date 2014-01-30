package actors

import play.api.libs.json._
import models._

case class Listen(user: User, document: Document)
case class StopListening(user: User, document: Document)
case class DocumentUpdate(
  userIs: Long,
  documentId: Long,
  field: String,
  from: Int,
  update: String,
  replaced: Int)

object DocumentUpdate {
  implicit val updateFormat = Json.format[DocumentUpdate]
}
