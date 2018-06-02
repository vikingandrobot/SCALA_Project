package controllers

import dao.{OrganizationDAO, UserDAO, UserOrganizationDAO}
import javax.inject.{Inject, Singleton}
import models.{Organization,OrganizationForm,User,UserOrganization}
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class OrganizationController @Inject()(cc: ControllerComponents, userDAO: UserDAO, organizationDAO: OrganizationDAO, userOrganizationDAO: UserOrganizationDAO) extends AbstractController(cc) with play.api.i18n.I18nSupport {

  private val postNewOrganizationUrl = routes.OrganizationController.organizationNew()

  def organizationNew = Action.async { implicit request =>
    // Get the connected user
    val user: Future[Option[User]] = {

      // Get the session
      val session = request.session.get("connected")
      if(session.isEmpty)
        Future.failed(new RuntimeException("user not found"))
      else {
        val user = userDAO.findByUsername(session.get)
        for {
          u <- user
          if u.isDefined
        } yield u
      }
    }

    // Check for valid user
    user flatMap {

      case Some(u) =>

        // Valid the form data
        OrganizationForm.form.bindFromRequest.fold(

          // On fail return the form with errors
          formWithErrors => {
           Future{
            BadRequest(views.html.organizationNew(formWithErrors, postNewOrganizationUrl))
            }
          },

          // On success
          formData => {

            // Create a new event and save it
            val newOrganization = Organization(
              None,
              formData.organizationType,
              formData.name,
              formData.address
            )

           val organization = for{
             o <- organizationDAO.insert(newOrganization);
             uo <- userOrganizationDAO.insert(new UserOrganization(None,u.id.get,o.id.get))
           } yield o

            // Redirect to the event page
            organization map {
              o => Redirect(routes.OrganizationController.organizationDetail(o.id.get))
            }
          }
        )

      case None => Future.successful(Unauthorized("Oops, you are not connected"))
    }
  }

  def organizationNewPage = Action.async { implicit request =>
    // Get the session
    request.session.get("connected") match {

      // If the session exists
      case Some(s) =>
        for {
          u <- userDAO.findByUsername(s)
        } yield {
          if (!u.isDefined) Future.successful(Seq.empty)
          if (u.isEmpty) Unauthorized("Oops, you are not connected")
          else  Ok(views.html.organizationNew(OrganizationForm.form, postNewOrganizationUrl))
        }

      // If the session does not exist
      case None => Future { Unauthorized("Oops, you are not connected") }
    }
  }

  /**
    * Get the list of all existing users, then return it.
    * The Action.async is used because the request is asynchronous.
    */
  def getOrganizations = Action.async {
    val newUser = User(None, "Mathieu", "Monteverde", "mathieu@gmail.com", "dsakmdklasmdlakmdlkamlsdkma", "jansdkjansdjkanlsdknaskldn")
    val user = userDAO.insert(newUser)

    user flatMap {
      u => {
        val newOrganization = Organization(None, "LOLx3 Corporation", "Finance", "Adresse n°2")
        val org = organizationDAO.insert(newOrganization)
        org flatMap {
          o => {
            val newUO = UserOrganization(None, u.id.get, o.id.get)
            val uo = userOrganizationDAO.insert(newUO)
            uo map {
              no => Ok(no.id + ", " + u.username + ", " + o.name)
            }
          }
        }
      }
    }
  }

  def organizationDetail(id : Long) = Action.async { implicit request =>

    organizationDAO.findById(id) flatMap {
      case Some(o) =>
        Future { Ok(views.html.organizationDetail(o)) }

      case None =>
        for {
          o <- organizationDAO.list()
        } yield Ok(views.html.organizations(o))
    }
  }
}
