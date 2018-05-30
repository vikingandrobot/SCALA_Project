package controllers

import dao.OrganizationDAO
import javax.inject.{Inject, Singleton}
import models.{Organization, Theme}
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class OrganizationController @Inject()(cc: ControllerComponents, organizationDAO: OrganizationDAO) extends AbstractController(cc) with play.api.i18n.I18nSupport {


  /**
    * Get the list of all existing users, then return it.
    * The Action.async is used because the request is asynchronous.
    */
  def getOrganizations = Action.async {
    val organizationsList = organizationDAO.list()
    organizationsList map (s => Ok(s.mkString))
  }

}
