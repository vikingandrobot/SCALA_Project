package models

import play.api.data.Form
import play.api.data.Forms._

case class InterestData (
                          id:Option[Long],
                          userId: Option[Long],
                          themeId: Long)

object NewInterestForm {
  val form: Form[InterestData] = Form(
    mapping(
      "id" -> optional(longNumber),
      "userId" -> optional(longNumber),
      "themeId" -> longNumber
    )(InterestData.apply)(InterestData.unapply)
  )
}

case class DeleteInterestData(interestId:Long)

object DeleteInterestForm {
  val form: Form[DeleteInterestData] = Form(
    mapping(
      "interestId" -> longNumber
    )(DeleteInterestData.apply)(DeleteInterestData.unapply)
  )
}
