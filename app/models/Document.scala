package models

import scala.concurrent._
import scalikejdbc._, async._, FutureImplicits._, SQLInterpolation._
import org.joda.time.DateTime

case class Document(
  id: Long,
  title: Option[String],
  body: Option[String],
  ownerId: Long,
  owner: Option[User] = None,
  editors: Seq[User] = Seq(),
  createdAt: DateTime,
  updatedAt: DateTime
)

object Document extends SQLSyntaxSupport[Document] with ShortenedNames {

  override val tableName = "dociment"
  override val columns = Seq("id", "title", "body", "owner_id", "created_at", "updated_at")
  override val nameConverters = Map(
    "ownerId" -> "owner_id",
    "createdAt" -> "created_at",
    "updatedAt" -> "updated_at"
  )

  def apply(d: ResultName[Document])(rs: WrappedResultSet): Document = new Document(
    id = rs.long(d.id),
    title = rs.stringOpt(d.title),
    body = rs.stringOpt(d.body),
    ownerId = rs.long(d.ownerId),
    createdAt = rs.timestamp(d.createdAt).toDateTime,
    updatedAt = rs.timestamp(d.updatedAt).toDateTime
  )
  def apply(d: SyntaxProvider[Document])(rs: WrappedResultSet): Document = apply(d.resultName)(rs)

  def apply(d: SyntaxProvider[Document], u: SyntaxProvider[User])(rs: WrappedResultSet): Document = {
    apply(d.resultName)(rs).copy(owner = Some(User(u)(rs)))
  }

  val (d, u) = (Document.syntax("d"), User.syntax("u"))

  def getById(id: Long)(
    implicit session: AsyncDBSession = AsyncDB.sharedSession,
    ctx: EC = ECGlobal): Future[Option[Document]] = {
    withSQL {
      select.from(Document as d)
        .leftJoin(User as u).on(d.ownerId, u.id)
        .where.eq(d.id, id)
    }.map(Document(d, u)).single.future
  }

  def getByOwnerId(ownerId: Long)(
    implicit session: AsyncDBSession = AsyncDB.sharedSession,
    ctx: EC = ECGlobal): Future[List[Document]] = {
    withSQL {
      select.from(Document as d).where.eq(d.ownerId, ownerId)
    }.map(Document(d)).list.future
  }
}
