package controllers

import dao.UserDAO
import javax.inject.{Inject, Singleton}
import models.{LoginForm, SignUpForm, User}
import org.mindrot.jbcrypt.BCrypt
import play.api.libs.json._
import play.api.libs.json.Reads.minLength
import play.api.libs.functional.syntax._
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global


@Singleton
class UserController @Inject()(cc: ControllerComponents, userDAO: UserDAO) extends AbstractController(cc) with play.api.i18n.I18nSupport {

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
  private val postJoinUrl = routes.UserController.join()
  private val postLoginUrl = routes.UserController.login()


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
    * Display the sign up form.
    */
  def joinPage = Action { implicit request =>
    Ok(views.html.join(SignUpForm.form, postJoinUrl))
  }

  /**
    * Post action for the sign up form.
    * Valid the data and create a new user.
    */
  def join = Action { implicit request =>

    // Valid the form data
    SignUpForm.form.bindFromRequest.fold(
      formWithErrors => {
        // Resend the form with error
        BadRequest(views.html.join(formWithErrors, postJoinUrl))
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

  /**
    * Display the login form.
    */
  def loginPage = Action { implicit request =>
    Ok(views.html.login(LoginForm.form, postLoginUrl))
  }

  /**
    * Post action for the login form.
    * Valid the data and check if the username and the password exist.
    */
  def login = Action { implicit request =>

    Ok("TODO")

    // Valid the form data
    /*LoginForm.form.bindFromRequest.fold(
      formWithErrors => {
        // Resend the form with error
        BadRequest(views.html.login(formWithErrors, postLoginUrl))
      },
      formData => {

        // Get user by username
        val optionalUser = userDAO.findByUsername(formData.username)

        optionalUser.map {
          case None => {
            // Resend the form with error
            val formWithErrors = LoginForm.form.withGlobalError("User or password wrong")
            BadRequest(views.html.login(formWithErrors, postLoginUrl))
          }
          case Some(u) if BCrypt.checkpw(formData.password, u.password) => Ok("Looool")
        }

      }
    )*/
  }
}
