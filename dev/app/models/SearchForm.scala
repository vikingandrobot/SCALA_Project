package models

import java.util.Date

import play.api.data.Form
import play.api.data.Forms._

case class SearchData(
   location: Option[String],
   date: Option[Date],
   endDate: Option[Date]
)

object SearchForm {
  // Set form structure
  val form: Form[SearchData] = Form(
    mapping(
      "location" -> optional(text),
      "date" -> optional(date),
      "endDate" -> optional(date)
    )(SearchData.apply)(SearchData.unapply)
  )
}