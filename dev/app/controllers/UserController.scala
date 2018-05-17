package controllers

import dao.UserDAO
import javax.inject.{Inject, Singleton}
import models.User
import models.SignUpForm
import org.mindrot.jbcrypt.BCrypt
import play.api.libs.json._
import play.api.libs.json.Reads.minLength
import play.api.libs.functional.syntax._
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global



@Singleton
class UserController @Inject()(cc: MessagesControllerComponents, userDAO: UserDAO) extends MessagesAbstractController(cc) {

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


  // Form post action to set
  private val postUrl = routes.UserController.join()


  def validateJson[A: Reads] = parse.json.validate(
    _.validate[A].asEither.left.map(e => BadRequest(JsError.toJson(e)))
  )


  /**
    * Get the list of all existing users, then return it.
    * The Action.async is used because the request is asynchronous.
    */
  def getUsers = Action.async {
    val userList = userDAO.list()
    userList map (s => Ok(Json.toJson(s)))
  }

  /**
    * Display the sign up form
    */
  def joinPage = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.join(SignUpForm.form, postUrl))
  }

  /**
    * Post action for the sign up form
    */
  def join = Action { implicit request =>

    // Valid the form data
    SignUpForm.form.bindFromRequest.fold(
      formWithErrors => {
        // Resend the form with error
        BadRequest(views.html.join(formWithErrors, postUrl))
      },
      formData => {

        // Hash the password
        val passwordHash = BCrypt.hashpw(formData.password, BCrypt.gensalt)

        // Create a new user and save it
        val newUser = User(None, formData.firstName, formData.lastName, formData.email, formData.username, passwordHash)
        val user = userDAO.insert(newUser)

        Ok(user.toString)
      }
    )
  }
}
