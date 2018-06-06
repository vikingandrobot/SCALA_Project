package dao

import javax.inject.{Inject, Singleton}
import models._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

trait UserOrganizationComponent {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._
  class UserOrganizationTable(tag:Tag) extends Table[UserOrganization](tag, "users_organizations"){
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)

    def userId = column[Long]("USER_ID")
    def organizationId = column[Long]("ORGANIZATION_ID")

    def * = (id.?, userId, organizationId) <> (UserOrganization.tupled, UserOrganization.unapply)
  }
}

@Singleton
class UserOrganizationDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends UserOrganizationComponent with OrganizationComponent with UserComponent with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  // Get the object-oriented list of users directly from the query table.
  val organizations = TableQuery[OrganizationTable]
  val users = TableQuery[UserTable]
  val userOrganizations = TableQuery[UserOrganizationTable]


  /** Retrieve the list of UserOrganization entries */
  def list(): Future[Seq[UserOrganization]] = {
    val query = userOrganizations.sortBy(s => (s.userId, s.organizationId))
    db.run(query.result)
  }

  /** Retrieve a UserOrganization from the id. */
  def findById(id:Long):Future[Option[UserOrganization]] =
    db.run(userOrganizations.filter(_.id === id).result.headOption)

  /** Retrieve the list of users by organization */
  def findUserByOrganization(organizationId:Long):Future[Seq[User]] = {
    val query = for {
      userOrganization <- userOrganizations if userOrganization.organizationId === organizationId
      user <- users if user.id === userOrganization.userId
    } yield user
    db.run(query.result)
  }

  def findByUserAndOrganization(organizationId:Long, userId:Long) :Future[Option[UserOrganization]] = {
   db.run(userOrganizations.filter(x=> x.userId === userId && x.organizationId === organizationId).result.headOption)
  }

  /** Retrieve the list of organizations by user*/
  def findOrganizationByUser(userId:Long):Future[Seq[Organization]] = {
    val query = for {
      userOrganization <- userOrganizations if userOrganization.userId === userId
      organization <- organizations if organization.id === userOrganization.organizationId
    } yield organization
    db.run(query.result)
  }

  /** Insert a new UserOrganization */
  def insert(userOrganization: UserOrganization): Future[UserOrganization] = {
    val insertQuery = userOrganizations returning userOrganizations.map(_.id) into ((userOrganization,id)=> userOrganization.copy(Some(id)))
    db.run(insertQuery += userOrganization)
  }

  /** Delete a UserOrganization */
  def delete(id: Long): Future[Int] =
    db.run(userOrganizations.filter(_.id === id).delete)


}