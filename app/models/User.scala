package models

import scala.concurrent._
import scalikejdbc._, async._, FutureImplicits._, SQLInterpolation._
import org.joda.time.DateTime
import com.github.t3hnar.bcrypt._
import play.api.libs.json._
import play.api.libs.functional.syntax._

case class User(
  id: Long,
  email: String,
  password: String,
  salt: String,
  firstName: Option[String] = None,
  lastName: Option[String]= None,
  dateJoined: DateTime
) {

  def checkPassword(plainPassword: String): Boolean = {
    plainPassword.bcrypt(salt) == password
  }
}

object User extends SQLSyntaxSupport[User] with ShortenedNames {

  val userReads = Json.reads[User]
  val userWrites = new Writes[User] {
    def writes(u: User): JsValue = {
      Json.obj(
        "id" -> u.id,
        "email" -> u.email,
        "firstName" -> u.firstName,
        "lastName" -> u.lastName,
        "dateJoined" -> u.dateJoined
      )
    }
  }

  implicit val userFormat = Format(userReads, userWrites)

  override val tableName = "users"
  override val columns = Seq("id", "email", "password", "salt", "first_name", "last_name", "date_joined")

  def fromResultSet(u: ResultName[User])(rs: WrappedResultSet): User = new User(
    id = rs.long(u.id),
    email = rs.string(u.email),
    password = rs.string(u.password),
    salt = rs.string(u.salt),
    firstName = rs.stringOpt(u.firstName),
    lastName = rs.stringOpt(u.lastName),
    dateJoined = rs.timestamp(u.dateJoined).toDateTime
  )
  def fromResultSet(u: SyntaxProvider[User])(rs: WrappedResultSet): User = fromResultSet(u.resultName)(rs)

  def fromResultSetOpt(u: SyntaxProvider[User])(rs: WrappedResultSet): Option[User] = {
    rs.longOpt(u.resultName.id).map(_ => User.fromResultSet(u)(rs))
  }

  val u = User.syntax("u")

  def getById(id: Long)(
    implicit session: AsyncDBSession = AsyncDB.sharedSession,
    ctx: EC = ECGlobal): Future[Option[User]] = {
    withSQL {
      select.from(User as u).where.eq(u.id, id)
    }.map(User.fromResultSet(u)).single.future
  }

  def getByEmail(email: String)(
    implicit session: AsyncDBSession = AsyncDB.sharedSession,
    ctx: EC = ECGlobal): Future[Option[User]] = {
    withSQL {
      select.from(User as u).where.eq(u.email, email)
    }.map(User.fromResultSet(u)).single.future
  }

  def authenticate(email: String, password: String)(
    implicit session: AsyncDBSession = AsyncDB.sharedSession,
    ctx: EC = ECGlobal): Future[Option[User]] = {
    getByEmail(email) map {_.filter {_.checkPassword(password)}}
  }

  def create(
    email: String,
    password:String,
    salt: String,
    firstName: Option[String] = None,
    lastName: Option[String] = None,
    dateJoined: DateTime)(
    implicit session: AsyncDBSession = AsyncDB.sharedSession,
    ctx: EC = ECGlobal): Future[User] = {
    withSQL {
      insert.into(User).namedValues(
        column.email -> email,
        column.password -> password,
        column.salt -> salt,
        column.firstName -> firstName,
        column.lastName -> lastName,
        column.dateJoined -> dateJoined
      ).returningId
    }.updateAndReturnGeneratedKey.future map { id =>
      new User(
        id = id,
        email = email,
        password = password,
        salt = salt,
        firstName = firstName,
        lastName = lastName,
        dateJoined = dateJoined
      )
    }
  }

  def createByEmailAndPassword(email:String, passwordPlain:String)(
    implicit session: AsyncDBSession = AsyncDB.sharedSession,
    ctx: EC = ECGlobal): Future[User] = {
    val salt = generateSalt
    val passwordHash = passwordPlain.bcrypt(salt)
    val dateJoined = DateTime.now()

    create(
      email = email,
      password = passwordHash,
      salt = salt,
      dateJoined = dateJoined
    )
  }

  def save(user: User)(
    implicit session: AsyncDBSession = AsyncDB.sharedSession,
    ctx: EC = ECGlobal): Future[User] = {
    withSQL {
      update(User).set(
        User.column.email -> user.email,
        User.column.password -> user.password,
        User.column.salt -> user.salt,
        User.column.firstName -> user.firstName,
        User.column.lastName -> user.lastName,
        User.column.dateJoined -> user.dateJoined
      ).where.eq(User.column.id, user.id)
    }.update.future map { _ =>
      user
    }
  }

  def updateInfo(user: User, firstName: String, lastName: String)(
    implicit session: AsyncDBSession = AsyncDB.sharedSession,
    ctx: EC = ECGlobal): Future[User] = {
    withSQL {
      update(User).set(
        User.column.firstName -> firstName,
        User.column.lastName -> lastName
      ).where.eq(User.column.id, user.id)
    }.update.future map { _ =>
      user.copy(firstName = Some(firstName), lastName = Some(lastName))
    }
  }
}
