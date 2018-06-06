package controllers

import dao.{InterestDAO, OrganizationDAO, UserDAO, UserOrganizationDAO}
import javax.inject.{Inject, Singleton}
import models.{LoginForm, SignUpForm, User}
import org.mindrot.jbcrypt.BCrypt
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}


@Singleton
class UserController @Inject()(cc: ControllerComponents, userDAO: UserDAO, interestDao: InterestDAO, userOrganizationDAO:UserOrganizationDAO) extends AbstractController(cc) with play.api.i18n.I18nSupport {

  // Form post action to set
  private val postJoinUrl = routes.UserController.join()
  private val postLoginUrl = routes.UserController.login()


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
  def join = Action.async { implicit request =>

    // Valid the form data
    SignUpForm.form.bindFromRequest.fold(
      formWithErrors => {
        Future {
          // Resend the form with error
          BadRequest(views.html.join(formWithErrors, postJoinUrl))
        }
      },
      formData => {

        // Hash the password
        val passwordHash = BCrypt.hashpw(formData.password, BCrypt.gensalt)

        // Create a new user and save it
        val newUser = User(None, formData.firstName, formData.lastName, formData.email, formData.username, passwordHash)
        val user = userDAO.insert(newUser)

        // TODO: Check if an error occurred (e.g. username already in the database)

        user map {
          u =>
            Redirect(routes.HomeController.index())
              .withSession("connected" -> u.username)
        }
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
  def login = Action.async { implicit request =>
    // Valid the form data
    LoginForm.form.bindFromRequest.fold(

      // On fail return the form with errors
      formWithErrors => {
        Future {
          BadRequest(views.html.login(formWithErrors, postLoginUrl))
        }
      },

      // On success compare the username and password on database
      formData => {
        val optionalUser = userDAO.findByUsername(formData.username)

        optionalUser map {
          case Some(u) if BCrypt.checkpw(formData.password, u.password) =>
            Redirect(routes.HomeController.index())
              .withSession("connected" -> u.username)
          case _ =>
            // Resend the form with error
            val formWithErrors = LoginForm.form.withGlobalError("User or password wrong")
            BadRequest(views.html.login(formWithErrors, postLoginUrl))
        }
      }
    )
  }

  def logout = Action { implicit request =>
    Redirect(routes.HomeController.index()).withNewSession
  }

  def profile = Action.async { implicit request =>
    val username = request.session.get("connected").get

    val user = userDAO.findByUsername(username)
    user flatMap {
      case Some(u) =>
        for{
          themes <- interestDao.findThemeByUser(u.id.get)
          organizations <- userOrganizationDAO.findOrganizationByUser(u.id.get)
        }yield {
          Ok(views.html.profile(u, organizations.toList, themes.toList))
        }
      /* {
        val interests = interestDao.findThemeByUser(u.id.get)
        interests map {
          si =>  Ok(views.html.profile())
        }

      }*/
      case _ => {
        Future{
          Unauthorized("Oops, your are not conneted")
        }
      }
    }
  }
}
