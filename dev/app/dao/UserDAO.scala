package dao

import scala.concurrent.Future
import javax.inject.{Inject, Singleton}
import models.User
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext


// We use a trait component here in order to share the StudentsTable class with other DAO, thanks to the inheritance.
trait UserComponent {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._

  // This class convert the database's user table in a object-oriented entity: the User model.
  class UserTable(tag: Tag) extends Table[User](tag, "users") {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc) // Primary key, auto-incremented

    def firstName = column[String]("FIRSTNAME")

    def lastName = column[String]("LASTNAME")

    def email = column[String]("EMAIL")

    def username = column[String]("USERNAME")

    def password = column[String]("PASSWORD")

    // Map the attributes with the model; the ID is optional.
    def * = (id.?, firstName, lastName, email, username, password) <> (User.tupled, User.unapply)
  }

}


// This class contains the object-oriented list of users and offers methods to query the data.
// A DatabaseConfigProvider is injected through dependency injection; it provides a Slick type bundling a database and
// driver. The class extends the users' query table and loads the JDBC profile configured in the application's
// configuration file.
@Singleton
class UserDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) extends UserComponent with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  // Get the object-oriented list of users directly from the query table.
  val users = TableQuery[UserTable]

  /** Retrieve the list of users */
  def list(): Future[Seq[User]] = {
    val query = users.sortBy(s => (s.lastName, s.firstName))
    db.run(query.result)
  }

  /** Retrieve a user from the id. */
  def findById(id: Long): Future[Option[User]] =
    db.run(users.filter(_.id === id).result.headOption)

  /** Retrieve a user from the username. */
  def findByUsername(username: String): Future[Option[User]] =
    db.run(users.filter(_.username === username).result.headOption)

  /** Insert a new user, then return it. */
  def insert(user: User): Future[User] = {
    val insertQuery = users returning users.map(_.id) into ((user, id) => user.copy(Some(id)))
    db.run(insertQuery += user)
  }

  /** Update a user, then return an integer that indicate if the user was found (1) or not (0). */
  def update(id: Long, user: User): Future[Int] = {
    val userToUpdate: User = user.copy(Some(id))
    db.run(users.filter(_.id === id).update(userToUpdate))
  }

  /** Delete a user, then return an integer that indicate if the user was found (1) or not (0). */
  def delete(id: Long): Future[Int] =
    db.run(users.filter(_.id === id).delete)
}