package dao

import scala.concurrent.Future
import javax.inject.{Inject, Singleton}
import models._
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext

trait InterestComponent {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._
  class InterestTable(tag:Tag) extends Table[Interest](tag, "interests"){
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)

    def userId = column[Long]("user_id")
    def themeId = column[Long]("theme_id")

    def * = (id.?, userId, themeId) <> (Interest.tupled, Interest.unapply)
  }
}

@Singleton
class InterestDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends InterestComponent with ThemeComponent with UserComponent with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  // Get the object-oriented list of users directly from the query table.
  val themes = TableQuery[ThemeTable]
  val users = TableQuery[UserTable]
  val interests = TableQuery[InterestTable]


  /** Retrieve the list of interests */
  def list(): Future[Seq[Interest]] = {
    val query = interests.sortBy(s => (s.userId, s.themeId))
    db.run(query.result)
  }

  /** Retrieve a interest from the id. */
  def findById(id:Long):Future[Option[Interest]] =
    db.run(interests.filter(_.id === id).result.headOption)

  /** Retrieve the list of interest by user */
  def findUserByTheme(themeId:Long):Future[Seq[User]] = {
    val query = for {
      interest <- interests if interest.themeId === themeId
      user <- users if user.id === interest.userId
    } yield user
    db.run(query.result)
  }

  /** Retrieve the list of interest by theme*/
  def findThemeByUser(userId:Long):Future[Seq[Theme]] = {
    val query = for {
      interest <- interests if interest.userId === userId
      theme <- themes if theme.id === interest.themeId
    } yield theme
    db.run(query.result)
  }

  /** Insert a new interest */
  def insert(interest: Interest): Future[Interest] = {
    val insertQuery = interests returning interests.map(_.id) into ((interest,id)=> interest.copy(Some(id)))
    db.run(insertQuery += interest)
  }

  /** Delete a interest */
  def delete(id: Long): Future[Int] =
    db.run(interests.filter(_.id === id).delete)


}
