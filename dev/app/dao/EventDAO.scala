package dao

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Date

import akka.http.scaladsl.model.headers.Date

import scala.concurrent.Future
import javax.inject.{Inject, Singleton}
import models.{Event, Organization}
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext
import scala.reflect.internal.util.TableDef.Column


trait EventComponent {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._

  class EventTable(tag: Tag) extends Table[Event](tag, "events") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc) // Primary key, auto-incremented

    def title = column[String]("title")

    def description = column[String]("description")

    def price = column[Float]("price")

    def address = column[String]("address")

    def region = column[String]("region")

    def startDate = column[Timestamp]("start_date")

    def endDate = column[Timestamp]("end_date")

    def scheduleDescription = column[String]("schedule_description")

    def userId = column[Long]("user_id")

    def organizationId = column[Long]("organization_id")

    // Map the attributes with the model; the ID is optional.
    def * = (id.?, title, description, price, address, region, startDate, endDate, scheduleDescription, userId, organizationId) <> (Event.tupled, Event.unapply)
  }
}


@Singleton
class EventDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) extends EventComponent with OrganizationComponent with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  // Get the object-oriented list of users directly from the query table.
  val events = TableQuery[EventTable]
  val organization = TableQuery[OrganizationTable]

  /** Retrieve the list of event */
  def list(): Future[Seq[Event]] = {
    val query = events.sortBy(e => e.title)
    db.run(query.result)
  }

  /** Retrieve the list of event with organization */
  def listEventsWithOrganization(location:Option[String] = None, date:Option[java.util.Date] = None, endDate:Option[java.util.Date] = None): Future[Seq[(Event, Organization)]] = {
    val f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val eventQuery = events.filter { event =>
      List(
        location.map(event.region === _),
        date.map(d => event.endDate >= Timestamp.valueOf(f.format(d))),
        endDate.map(d => event.startDate <= Timestamp.valueOf(f.format(d)))
      ).collect({case Some(criteria)  => criteria}).reduceLeftOption(_ && _).getOrElse(slick.lifted.LiteralColumn(true))
    }

    val query = for {
      e <- eventQuery join organization on (_.organizationId === _.id)
    } yield (e._1, e._2)

    db.run(query.result)
  }

  /** Retrieve a event from the id. */
  def findById(id: Long): Future[Option[Event]] =
    db.run(events.filter(_.id === id).result.headOption)

  /** Retrieve a event from the id with orrganization. */
  def findByIdWithOrganization(id: Long): Future[Option[(Event, Organization)]] = {
    val query = events filter (_.id === id) join organization on (_.organizationId === _.id)

    db.run(query.result.headOption)
  }

  def findEventsByOrganization(id:Long) : Future[Seq[Event]] = {
    val query = events.filter(_.organizationId === id).sortBy(s => (s.title))
    db.run(query.result)
  }

  /** Insert a new event, then return it. */
  def insert(event: Event): Future[Event] = {
    val insertQuery = events returning events.map(_.id) into ((event, id) => event.copy(Some(id)))
    db.run(insertQuery += event)
  }

  /** Update a event, then return an integer that indicate if the event was found (1) or not (0). */
  def update(id: Long, event: Event): Future[Int] = {
    val eventToUpdate: Event = event.copy(Some(id))
    db.run(events.filter(_.id === id).update(eventToUpdate))
  }

  /** Delete a event, then return an integer that indicate if the event was found (1) or not (0). */
  def delete(id: Long): Future[Int] =
    db.run(events.filter(_.id === id).delete)
}