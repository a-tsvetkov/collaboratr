package models

import scalikejdbc._, SQLInterpolation._

case class DocumentUser(userId: Long, documentId: Long)

object DocumentUser extends SQLSyntaxSupport[DocumentUser] {
  override val tableName = "document_user"
  override val columnNames = Seq("user_id", "document_id")
}
