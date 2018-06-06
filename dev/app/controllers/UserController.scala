package controllers

import dao.{InterestDAO, OrganizationDAO, UserDAO, UserOrganizationDAO}
import javax.inject.{Inject, Singleton}
import models.{LoginForm, SignUpForm, ProfileUpdateForm, User, UserData}
import org.mindrot.jbcrypt.BCrypt
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


@Singleton
class UserController @Inject()(cc: ControllerComponents, userDAO: UserDAO, interestDao: InterestDAO, userOrganizationDAO: UserOrganizationDAO) extends AbstractController(cc) with play.api.i18n.I18nSupport {

  // Form post action to set
  private val postJoinUrl = routes.UserController.join()
  private val postLoginUrl = routes.UserController.login()
  private val postUpdateProfileUrl = routes.UserController.profileUpdate()

  def profileUpdatePage = Action.async { implicit request =>
    request.session.get("connected") match {
      case Some(u) =>
        for (
          user <- userDAO.findByUsername(u)
          if (user.isDefined)
        ) yield {

          val data = UserData(
            None,
            user.get.firstName,
            user.get.lastName,
            user.get.email,
            user.get.username,
            "")

          Ok(views.html.profileEdit(ProfileUpdateForm.form.fill(data), postUpdateProfileUrl))
        }
      case None => Future {
        Unauthorized("Oops, you are not connected")
      }
    }
  }

  def profileUpdate = Action.async { implicit request =>
    request.session.get("connected") match {
      case Some(u) =>
        ProfileUpdateForm.form.bindFromRequest.fold(

          formWithErrors => {
            Future {
              BadRequest(views.html.profileEdit(formWithErrors, postUpdateProfileUrl))
            }
          },

          formData => {
            for {
              oldUser <- userDAO.findByUsername(u)
            } yield {
              val newUser = User(
                oldUser.get.id,
                formData.firstName,
                formData.lastName,
                formData.email,
                formData.username,
                formData.password
              )
              userDAO.update(oldUser.get.id.get, newUser)
              Redirect(routes.UserController.profile)
            }
          }
        )
      case None => Future {
        Unauthorized("Oops, you are not connected")
      }
    }
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
    request.session.get("connected") match {
      case Some(u) =>
        for {
          user <- userDAO.findByUsername(u)
          themes <- interestDao.findThemeByUser(user.get.id.get)
          organizations <- userOrganizationDAO.findOrganizationByUser(user.get.id.get)
        } yield {
          Ok(views.html.profile(user.get, organizations.toList, themes.toList))
        }
      case None =>
        Future {
          Unauthorized("Oops, your are not conneted")
        }
    }
  }
}
