package controllers

import java.sql.Timestamp

import dao.{EventDAO, UserDAO, UserOrganizationDAO}
import javax.inject.{Inject, Singleton}
import models.{Event, EventForm, User}
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


@Singleton
class EventController @Inject()(cc: ControllerComponents, eventDAO: EventDAO, userDAO: UserDAO, userOrganizationDAO: UserOrganizationDAO) extends AbstractController(cc) with play.api.i18n.I18nSupport {

  // Form post action to set
  private val postNewEventUrl = routes.EventController.eventNew()


  def eventPage = Action { implicit request =>
    Ok(views.html.events())
  }


  def eventNew = Action.async { implicit request =>

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
        EventForm.form.bindFromRequest.fold(

          // On fail return the form with errors
          formWithErrors => {

            // Get the user's organizations
            val organizations = userOrganizationDAO.findOrganizationByUser(u.id.get)

            for {
              o <- organizations
            } yield {
              if (o.isEmpty) Unauthorized("Oops, you are not a part of an organization")
              else BadRequest(views.html.eventNew(formWithErrors, postNewEventUrl, o))
            }
          },

          // On success
          formData => {

            // Create a new event and save it
            val newUser = Event(
              None,
              formData.title,
              formData.description,
              formData.price.toFloat,
              formData.address,
              formData.region,
              new Timestamp(formData.startDate.getTime),
              new Timestamp(formData.endDate.getTime),
              formData.scheduleDescription,
              u.id.get,
              formData.organizationId
            )

            val event = eventDAO.insert(newUser)
            // TODO: Check if an error occurred (e.g. title? already in the database)

            // Redirect to the event page
            event map {
              e => Redirect(routes.EventController.eventDetail(e.id.get))
            }
          }
        )

      case None => Future.successful(Unauthorized("Oops, you are not connected"))
    }
  }


  def eventNewPage = Action.async { implicit request =>

    // Get the session
    request.session.get("connected") match {

      // If the session exists
      case Some(s) =>
        for {
          u <- userDAO.findByUsername(s)
          o <-
            if (u.isDefined) userOrganizationDAO.findOrganizationByUser(u.get.id.get)
            else Future.successful(Seq.empty)
        } yield {

          if (u.isEmpty) Unauthorized("Oops, you are not connected")
          else if (o.isEmpty) Unauthorized("Oops, you are not a part of an organization")
          else  Ok(views.html.eventNew(EventForm.form, postNewEventUrl, o))
        }

      // If the session does not exist
      case None => Future { Unauthorized("Oops, you are not connected") }
    }
  }


  def eventDetail(id: Long) = Action{ implicit request =>
    Ok(views.html.eventDetail())
  }
}

