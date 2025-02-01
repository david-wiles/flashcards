package net.davidwiles.flashcards.api.engine

import net.davidwiles.flashcards.common.models.User
import org.mindrot.jbcrypt.BCrypt
import net.davidwiles.flashcards.api.ServiceError
import scalikejdbc.*
import scalikejdbc.TxBoundary.Either.*

import java.util.UUID
import scala.util.Try

trait UserEngine:

  def hashPass(password: String): Either[ServiceError, String] =
    Try(BCrypt.hashpw(password, BCrypt.gensalt())).toEither.left.map(ex =>
      ServiceError.BcryptError("unable to create password hash", Some(ex))
    )

  def checkPass(password: String, hash: String): Either[ServiceError, Boolean] =
    Try(BCrypt.checkpw(password, hash)).toEither.left.map(ex =>
      ServiceError.BcryptError("unable to check password hash", Some(ex))
    )

  def create(username: String, email: String, passHash: String): Either[ServiceError, User]

  def update(user: User): Either[ServiceError, Boolean]

  def delete(user: User): Either[ServiceError, Unit]

  def get(username: String): Either[ServiceError, Option[User]]

  protected def translateErrors[A](thunk: => A): Either[ServiceError, A] =
    Try(thunk).toEither.left.map(ex => ServiceError.SQLExceptionError(ex.getMessage, ex))

class UserEngineImpl extends UserEngine:

  implicit def boundary: TxBoundary[Either[ServiceError, Any]] = eitherTxBoundary[ServiceError, Any]

  extension (rs: WrappedResultSet)

    def toUserOpt: Option[User] =
      Try {
        User(
          id = UUID.fromString(rs.string("id")),
          username = rs.string("username"),
          email = rs.string("email"),
          isActive = rs.boolean("is_active"),
          passHash = rs.string("pass_hash"),
          dateCreated = rs.zonedDateTime("date_created"),
          dateModified = rs.zonedDateTime("date_modified")
        )
      }.toOption

  override def create(username: String, email: String, passHash: String): Either[ServiceError, User] =
    DB.localTx { implicit session =>
      for {
        _          <- translateErrors(createImpl(username, email, passHash))
        userResult <- translateErrors(getImpl(username))
        user       <- userResult.toRight(ServiceError.UserNotCreatedError("user not created"))
      } yield user
    }

  private def createImpl(username: String, email: String, passHash: String)(implicit session: DBSession): Unit =
    sql"""
        INSERT INTO users (username, email, is_active, pass_hash)
        VALUES ($username, $email, true, $passHash)
      """.execute
      .apply()

  override def update(user: User): Either[ServiceError, Boolean] =
    DB.localTx { implicit session =>
      translateErrors(updateImpl(user))
    }

  private def updateImpl(user: User)(implicit session: DBSession): Boolean =
    sql"""
      UPDATE users
      SET username = ${user.username}
      WHERE id = ${user.id}
    """.update
      .apply() == 1

  override def delete(user: User): Either[ServiceError, Unit] =
    DB.localTx { implicit session =>
      translateErrors(deleteImpl(user))
    }

  private def deleteImpl(user: User)(implicit session: DBSession): Unit =
    sql"""
      DELETE FROM users
      WHERE id = ${user.id}
    """.execute
      .apply()

  override def get(username: String): Either[ServiceError, Option[User]] =
    DB.readOnly { implicit session =>
      translateErrors(getImpl(username))
    }

  private def getImpl(username: String)(implicit session: DBSession): Option[User] =
    sql"""
      SELECT id, username, email, is_active, pass_hash, date_created, date_modified
      FROM users
      WHERE username = $username
    """
      .map(_.toUserOpt)
      .single
      .apply()
      .flatten

object UserEngine:

  def apply(): UserEngine = new UserEngineImpl()
