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

  def organizazionNew = Action { implicit request =>
    OrganizationForm.form.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.organizationNew(formWithErrors,postNewOrganizationUrl))
      },
      formData => {

        val newOrganization = Organization(
          None,
          formData.organizationType,
          formData.name,
          formData.adress)

        Ok(newOrganization.toString)
      }
    )
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

}
