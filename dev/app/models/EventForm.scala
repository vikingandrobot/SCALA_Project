package models

import java.sql.Timestamp
import java.util.Date

import play.api.data.Form
import play.api.data.Forms._

// Case class that contains the elements in the event creation form
case class EventData(
  id: Option[Long],
  title: String,
  description: String,
  price: BigDecimal,
  address: String,
  region: String,
  startDate: Date,
  endDate: Date,
  scheduleDescription: String,
  organizationId: Long
)

object EventForm {
  // Set form structure
  val form: Form[EventData] = Form(
    mapping(
      "id" -> optional(longNumber),
      "title" -> nonEmptyText(minLength = 3, maxLength = 100),
      "description" -> nonEmptyText(minLength = 3, maxLength = 100),
      "price" -> bigDecimal(10, 2),
      "address" -> nonEmptyText(minLength = 3, maxLength = 80),
      "region" -> nonEmptyText(minLength = 8, maxLength = 100),
      "startDate" -> date,
      "endDate" -> date,
      "scheduleDescription" -> nonEmptyText(minLength = 8, maxLength = 100),
      "organizationId" -> longNumber
    )(EventData.apply)(EventData.unapply)
  )
}



