package controllers

import dao.{OrganizationDAO, UserDAO, UserOrganizationDAO}
import javax.inject.{Inject, Singleton}
import models.{Organization, Theme, User, UserOrganization}
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class OrganizationController @Inject()(cc: ControllerComponents, userDAO: UserDAO, organizationDAO: OrganizationDAO, userOrganizationDAO: UserOrganizationDAO) extends AbstractController(cc) with play.api.i18n.I18nSupport {


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
