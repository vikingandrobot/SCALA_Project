package models

import play.api.data.Form
import play.api.data.Forms._

case class InsertInterestData (
  id:Option[Long],
  userId: Long,
  themeId: Long)

object InsertInterestForm {
  val form: Form[InsertInterestData] = Form(
    mapping(
      "id" -> optional(longNumber),
      "userId" -> longNumber,
      "themeId" -> longNumber
    )(InsertInterestData.apply)(InsertInterestData.unapply)
  )
}

case class DeleteInterestData(
  interestId:Long)

object DeleteInterestForm {
  val form: Form[DeleteInterestData] = Form(
    mapping(
      "interestId" -> longNumber
    )(DeleteInterestData.apply)(DeleteInterestData.unapply)
  )
}
