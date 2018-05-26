package models

import play.api.libs.functional.syntax.unlift
import play.api.libs.json.{JsPath, Reads, Writes}
import play.api.libs.functional.syntax._
import play.api.libs.json._

// Class User (with optional id for new user)
case class User(
   id: Option[Long],
   firstName: String,
   lastName: String,
   email: String,
   username: String,
   password: String)

case class Theme(
  id: Option[Long],
  name: String)


case class Interest(
  id: Option[Long],
  userId: Long,
  themeId: Long)