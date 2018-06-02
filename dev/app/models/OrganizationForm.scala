package models

import play.api.data.Form
import play.api.data.Forms._

case class OrgnizationData(
  id:Option[Long],
  organizationType:String,
  name:String,
  adress:String
)

object OrganizationForm {
  val form: Form[OrgnizationData] = Form(
    mapping(
      "id" -> optional(longNumber),
      "organizationType" -> nonEmptyText(minLength=3, maxLength=100),
      "name" ->  nonEmptyText(minLength=3, maxLength=100),
      "adresse"-> nonEmptyText(minLength=3, maxLength=100)
    )(OrgnizationData.apply)(OrgnizationData.unapply)
  )
}
