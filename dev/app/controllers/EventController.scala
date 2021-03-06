package controllers

import java.sql.Timestamp
import java.util.Date

import dao.{EventDAO, OrganizationDAO, UserDAO, UserOrganizationDAO}
import javax.inject.{Inject, Singleton}
import models._
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


@Singleton
class EventController @Inject()(cc: ControllerComponents, eventDAO: EventDAO, organizationDAO: OrganizationDAO, userDAO: UserDAO, userOrganizationDAO: UserOrganizationDAO) extends AbstractController(cc) with play.api.i18n.I18nSupport {

  // Form post action to set
  private val postNewEventUrl = routes.EventController.eventNew()

  // Form post action to set
  private val postEditEventUrl = routes.EventController.eventEdit()


  def eventPage() = Action.async { implicit request =>

    SearchForm.form.bindFromRequest.fold(

      // On fail return the form with errors
      formWithErrors => {
        println(formWithErrors.errors.toString())
        for {
          e <- eventDAO.listEventsWithOrganization()
        } yield Ok(views.html.events(SearchForm.form, routes.EventController.eventPage(), e))
      },

      // On success
      formData => {
        println(formData.location)
        for {
          e <- eventDAO.listEventsWithOrganization(formData.location, formData.date, formData.endDate)
        } yield Ok(views.html.events(SearchForm.form, routes.EventController.eventPage(), e))
      }
    )
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
              else {
                val list: Seq[(String, String)] = o.map(x => (x.id.get.toString, x.name))
                BadRequest(views.html.eventNew(formWithErrors, postNewEventUrl, list, None))
              }
            }
          },

          // On success
          formData => {

            // Create a new event and save it
            val newEdit = Event(
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

            val event = eventDAO.insert(newEdit)
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


  def eventNewPage(organizationId: Option[Long]) = Action.async { implicit request =>

    // Get the session
    request.session.get("connected") match {

      // If the session exists
      case Some(s) =>
        for {
          u <- userDAO.findByUsername(s)
          o <-
            if(organizationId.isDefined) organizationDAO.findById(organizationId.get)
            else Future.successful(None)
          os <-
            if (u.isDefined) userOrganizationDAO.findOrganizationByUser(u.get.id.get)
            else Future.successful(Seq.empty)
        } yield {

          if (u.isEmpty) Unauthorized("Oops, you are not connected")
          else if (os.isEmpty) Unauthorized("Oops, you are not a part of an organization")
          else  {

            val list: Seq[(String, String)] = os.map(x => (x.id.get.toString, x.name))

            Ok(views.html.eventNew(EventForm.form, postNewEventUrl, list, o))
          }
        }

      // If the session does not exist
      case None => Future { Unauthorized("Oops, you are not connected") }
    }
  }

  def eventEdit = Action.async { implicit request =>

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
              else {
                val list: Seq[(String, String)] = o.map(x => (x.id.get.toString, x.name))

                BadRequest(views.html.eventEdit(formWithErrors, postEditEventUrl, list))
              }
            }
          },

          // On success
          formData => {

            val oldEvent = eventDAO.findById(formData.id.get)

            oldEvent flatMap {
              case Some(o) =>
                // Create a new event and save it
                val newEdit = Event(
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

                val event = eventDAO.update(o.id.get, newEdit)
                // TODO: Check if an error occurred (e.g. title? already in the database) with the map below...

                // Redirect to the event page
                event map {
                  e => Redirect(routes.EventController.eventDetail(o.id.get))
                }
              case None => Future { Redirect(routes.EventController.eventPage()) }
            }
          }
        )

      case None => Future.successful(Unauthorized("Oops, you are not connected"))
    }
  }

  def eventEditPage(id: Long) = Action.async { implicit request =>

    // Get the session
    request.session.get("connected") match {

      // If the session exists
      case Some(s) =>
        for {
          u <- userDAO.findByUsername(s)
          e <-
            if (u.isDefined) eventDAO.findById(id)
            else Future.successful(None)
          o <-
            if (u.isDefined) userOrganizationDAO.findOrganizationByUser(u.get.id.get)
            else Future.successful(Seq.empty)
        } yield {

          if (u.isEmpty) Unauthorized("Oops, you are not connected")
          else if (e.isEmpty) Redirect(routes.EventController.eventPage())
          else if (e.get.userId != u.get.id.get) Unauthorized("Oops, you are not authorized to edit this event")
          else if (o.isEmpty) Unauthorized("Oops, you are not a part of an organization")
          else {
            val event = e.get
            val data = EventData(
              event.id,
              event.title,
              event.description,
              BigDecimal(event.price),
              event.address,
              event.region,
              event.startDate,
              event.endDate,
              event.scheduleDescription,
              event.organizationId
            )

            val list: Seq[(String, String)] = o.map(x => (x.id.get.toString, x.name))

            Ok(views.html.eventEdit(EventForm.form.fill(data), postEditEventUrl, list))
          }
        }

      // If the session does not exist
      case None => Future { Unauthorized("Oops, you are not connected") }
    }
  }

  def eventDetail(id: Long) = Action.async { implicit request =>

    eventDAO.findByIdWithOrganization(id) flatMap {
      case Some(e) =>
        Future { Ok(views.html.eventDetail(e)) }

      case None =>
        for {
          e <- eventDAO.listEventsWithOrganization()
        } yield Ok(views.html.events(SearchForm.form, routes.EventController.eventPage(), e))
    }
  }

  def eventDelete(id:Long) = Action.async{ implicit request =>
    request.session.get("connected") match {

      // If the session exists
      case Some(s) =>
        for{
          eo <-  eventDAO.findByIdWithOrganization(id)
          user <- userDAO.findByUsername(s)
          users <- userOrganizationDAO.findUserByOrganization(eo.get._2.id.get)
        }yield{
            if(users.toList.contains(user.get) && eo.isDefined) {
              eventDAO.delete(id)
              Redirect(routes.OrganizationController.organizationDetail(eo.get._2.id.get))
            }else{
              Unauthorized("Oops, you are not authorized to acces this organization")
            }
        }
      case None => Future { Unauthorized("Oops, you are not connected") }
    }
  }
}

