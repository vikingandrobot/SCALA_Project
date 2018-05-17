package controllers

import dao.UserDAO
import javax.inject.{Inject, Singleton}
import models.User
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class UserController @Inject()(cc: ControllerComponents, userDAO: UserDAO) extends AbstractController(cc) {

  // Convert a User-model object into a JsValue representation, which means that we serialize it into JSON.
  implicit val userToJson: Writes[User] = (
    (JsPath \ "id").write[Option[Long]] and
      (JsPath \ "firstName").write[String] and
      (JsPath \ "lastName").write[String] and
      (JsPath \ "email").write[String] and
      (JsPath \ "username").write[String] and
      (JsPath \ "password").write[String]
    // Use the default 'unapply' method (which acts like a reverted constructor) of the User case class if order to get
    // back the User object's arguments and pass them to the JsValue.
    ) (unlift(User.unapply))

  // Convert a JsValue representation into a User-model object, which means that we deserialize the JSON.
  implicit val jsonToUser: Reads[User] = (
    (JsPath \ "id").readNullable[Long] and
      (JsPath \ "firstName").read[String](minLength[String](2)) and
      (JsPath \ "lastName").read[String](minLength[String](2)) and
      (JsPath \ "email").read[String](minLength[String](2)) and
      (JsPath \ "username").read[String](minLength[String](2)) and
      (JsPath \ "password").read[String](minLength[String](8))
    // Use the default 'apply' method (which acts like a constructor) of the User case class with the JsValue in order
    // to construct a user object from it.
    ) (User.apply _)


  /**
    * Get the list of all existing users, then return it.
    * The Action.async is used because the request is asynchronous.
    */
  def getUsers = Action.async {
    val userList = userDAO.list()
    userList map (s => Ok(Json.toJson(s)))
  }

}
