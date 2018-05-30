package dao

import javax.inject.{Inject, Singleton}
import models.{Organization, User}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

trait OrganizationComponent{
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._

  // This class convert the database's orgnaizations table in a object-oriented entity: the Organization model.
  class OrganizationTable(tag: Tag) extends Table[Organization](tag, "organizations") {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc) // Primary key, auto-incremented

    def organizationType = column[String]("TYPE")

    def name = column[String]("NAME")

    def address = column[String]("ADDRESS")

    // Map the attributes with the model; the ID is optional.
    def * = (id.?, organizationType, name, address) <> (Organization.tupled, Organization.unapply)
  }
}

@Singleton
class OrganizationDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) extends OrganizationComponent with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  // Get the object-oriented list of users directly from the query table.
  val organizations = TableQuery[OrganizationTable]

  /** Retrieve the list of organizations */
  def list(): Future[Seq[Organization]] = {
    val query = organizations.sortBy(s => (s.name))
    db.run(query.result)
  }

  /** Retrieve an organization from the id. */
  def findById(id: Long): Future[Option[Organization]] =
    db.run(organizations.filter(_.id === id).result.headOption)

  /** Retrieve an organizations from the name. */
  def findByName(name: String): Future[Option[Organization]] =
    db.run(organizations.filter(_.name === name).result.headOption)

  /** Insert a new organizations, then return it. */
  def insert(organization: Organization): Future[Organization] = {
    val insertQuery = organizations returning organizations.map(_.id) into ((organization, id) => organization.copy(Some(id)))
    db.run(insertQuery += organization)
  }

  /** Update an organization, then return an integer that indicate if the organization was found (1) or not (0). */
  def update(id: Long, organization: Organization): Future[Int] = {
    val organizationToUpdate: Organization = organization.copy(Some(id))
    db.run(organizations.filter(_.id === id).update(organizationToUpdate))
  }

  /** Delete an organization, then return an integer that indicate if the organization was found (1) or not (0). */
  def delete(id: Long): Future[Int] =
    db.run(organizations.filter(_.id === id).delete)
}

