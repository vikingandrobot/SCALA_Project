package controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc.{AbstractController, ControllerComponents}


@Singleton
class EventController @Inject()(cc: ControllerComponents) extends AbstractController(cc) with play.api.i18n.I18nSupport {
  def eventPage = Action { implicit request =>
    Ok(views.html.events())
  }
}
