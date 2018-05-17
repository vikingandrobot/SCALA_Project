package models

import play.api.data.Form
import play.api.data.Forms._

// Case class that contains the elements in the form
case class UserData(
                     id: Option[Long],
                     firstName: String,
                     lastName: String,
                     email: String,
                     username: String,
                     password: String
                   )

object SignUpForm {
  // Set form structure
  val form: Form[UserData] = Form(
    mapping(
      "id" -> optional(longNumber),
      "firstName" -> nonEmptyText(minLength = 3, maxLength = 100),
      "lastName" -> nonEmptyText(minLength = 3, maxLength = 100),
      "email" -> email,
      "username" -> nonEmptyText(minLength = 3, maxLength = 80),
      "password" -> nonEmptyText(minLength = 8, maxLength = 100)
    )(UserData.apply)(UserData.unapply)
  )
}

