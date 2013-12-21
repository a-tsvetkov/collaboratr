package models

import scala.concurrent._
import scalikejdbc._, async._, FutureImplicits._, SQLInterpolation._
import play.api.libs.json._
import org.joda.time.DateTime

case class Document(
  id: Long,
  title: String,
  body: String,
  ownerId: Long,
  owner: Option[User] = None,
  editors: Seq[User] = Seq(),
  createdAt: DateTime,
  updatedAt: DateTime)

object Document extends SQLSyntaxSupport[Document] with ShortenedNames {

  implicit val documentFormat = Json.format[Document]

  override val tableName = "document"
  override val columns = Seq("id", "title", "body", "owner_id", "created_at", "updated_at")

  def fromResultSet(d: ResultName[Document])(rs: WrappedResultSet): Document = new Document(
    id = rs.long(d.id),
    title = rs.string(d.title),
    body = rs.string(d.body),
    ownerId = rs.long(d.ownerId),
    createdAt = rs.timestamp(d.createdAt).toDateTime,
    updatedAt = rs.timestamp(d.updatedAt).toDateTime)
  def fromResultSet(d: SyntaxProvider[Document])(rs: WrappedResultSet): Document = fromResultSet(d.resultName)(rs)

  def fromResultSet(d: SyntaxProvider[Document], u: SyntaxProvider[User])(rs: WrappedResultSet): Document = {
    fromResultSet(d.resultName)(rs).copy(owner = Some(User.fromResultSet(u)(rs)))
  }

  val d = Document.syntax("d")
  private val (u, e, du) = (User.syntax("u"), User.syntax("e"), DocumentUser.syntax("du"))

  def getById(id: Long)(
    implicit session: AsyncDBSession = AsyncDB.sharedSession,
    ctx: EC = ECGlobal): Future[Option[Document]] = {
    withSQL {
      select.from(Document as d)
        .leftJoin(User as u).on(d.ownerId, u.id)
        .leftJoin(DocumentUser as du).on(du.documentId, d.id)
        .leftJoin(User as e).on(e.id, du.userId)
        .where.eq(d.id, id)
    }.one(Document.fromResultSet(d, u))
      .toMany(User.fromResultSetOpt(e))
      .map((document, editors) => document.copy(editors = editors))
      .single.future
  }

  def getByOwnerId(ownerId: Long)(
    implicit session: AsyncDBSession = AsyncDB.sharedSession,
    ctx: EC = ECGlobal): Future[List[Document]] = {
    withSQL {
      select.from(Document as d)
        .where.eq(d.ownerId, ownerId)
        .orderBy(d.updatedAt).desc
    }.map(Document.fromResultSet(d)).list.future
  }

  def create(title: String, owner: User)(
    implicit session: AsyncDBSession = AsyncDB.sharedSession,
    ctx: EC = ECGlobal): Future[Document] = {
    val createdAt = DateTime.now
    withSQL {
      insert.into(Document).namedValues(
        column.title -> title,
        column.ownerId -> owner.id,
        column.createdAt -> createdAt,
        column.updatedAt -> createdAt).returningId
    }.updateAndReturnGeneratedKey.future map { id =>
      new Document(
        id = id,
        title = title,
        body = "",
        ownerId = owner.id,
        owner = Some(owner),
        createdAt = createdAt,
        updatedAt = createdAt)
    }
  }

  def remove(id: Long, ownerId: Long)(
    implicit session: AsyncDBSession = AsyncDB.sharedSession,
    ctx: EC = ECGlobal): Future[Int] = {
    withSQL {
      delete.from(Document as d).where.eq(d.id, id).and.eq(d.ownerId, ownerId)
    }.update.future
  }
}
