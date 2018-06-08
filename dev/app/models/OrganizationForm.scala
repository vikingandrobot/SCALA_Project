package models

import play.api.data.Form
import play.api.data.Forms._

case class OrganizationData(
                            id: Option[Long],
                            organizationType: String,
                            name: String,
                            address: String
                          )

object OrganizationForm {
  val form: Form[OrganizationData] = Form(
    mapping(
      "id" -> optional(longNumber),
      "organizationType" -> nonEmptyText(minLength = 3, maxLength = 100),
      "name" -> nonEmptyText(minLength = 3, maxLength = 100),
      "address" -> text(minLength=3, maxLength = 100)
    )(OrganizationData.apply)(OrganizationData.unapply)
  )
}
