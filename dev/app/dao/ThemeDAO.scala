package dao

import scala.concurrent.Future
import javax.inject.{Inject, Singleton}
import models.Theme
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext


// We use a trait component here in order to share the StudentsTable class with other DAO, thanks to the inheritance.
trait ThemeComponent {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._

  // This class convert the database's theme table in a object-oriented entity: the Theme model.
  class ThemeTable(tag: Tag) extends Table[Theme](tag, "themes") {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc) // Primary key, auto-incremented

    def name = column[String]("NAME")

    // Map the attributes with the model; the ID is optional.
    def * = (id.?, name) <> (Theme.tupled, Theme.unapply)
  }

}


// This class contains the object-oriented list of users and offers methods to query the data.
// A DatabaseConfigProvider is injected through dependency injection; it provides a Slick type bundling a database and
// driver. The class extends the users' query table and loads the JDBC profile configured in the application's
// configuration file.
@Singleton
class ThemeDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) extends ThemeComponent with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  // Get the object-oriented list of users directly from the query table.
  val themes = TableQuery[ThemeTable]

  /** Retrieve the list of users */
  def list(): Future[Seq[Theme]] = {
    val query = themes.sortBy(s => (s.name))
    db.run(query.result)
  }

  /** Retrieve a theme from the id. */
  def findById(id: Long): Future[Option[Theme]] =
    db.run(themes.filter(_.id === id).result.headOption)

  /** Retrieve a theme from the username. */
  def findByName(name: String): Future[Option[Theme]] =
    db.run(themes.filter(_.name === name).result.headOption)

}