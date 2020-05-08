package controllers.shop

import javax.inject.{Inject, Singleton}
import models.Category
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc._
import repositories.CategoryRepository

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CategoriesController @Inject()(categoryRepository: CategoryRepository, cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  def newCategoryForm: Form[CreateCategoryForm] = Form {
    mapping(
      "name" -> nonEmptyText
    )(CreateCategoryForm.apply)(CreateCategoryForm.unapply)
  }

  def updatingCategoryForm: Form[UpdateCategoryForm] = Form {
    mapping(
      "id" -> number,
      "name" -> nonEmptyText
    )(UpdateCategoryForm.apply)(UpdateCategoryForm.unapply)
  }

  def createCategoryForm = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.categories.categoryCreate(newCategoryForm))
  }

  def createCategoryHandle: Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    newCategoryForm.bindFromRequest.fold(
      errorForm => Future.successful(BadRequest(views.html.categories.categoryCreate(errorForm))),
      cat => categoryRepository.create(cat.name).map { _ =>
        Redirect(routes.CategoriesController.viewCategories()).flashing("success" -> "category created")
      }
    )
  }

  def updateCategoryForm(id: Int): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val category = categoryRepository.getByID(id)
    category.map(cat => {
      val catForm = updatingCategoryForm.fill(UpdateCategoryForm(cat.id, cat.name))
      Ok(views.html.categories.categoryUpdate(catForm))
    })
  }

  def updateCategoryHanlde = Action.async { implicit request =>
    updatingCategoryForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.categories.categoryUpdate(errorForm))
        )
      },
      category => {
        categoryRepository.update(category.id, Category(category.id, category.name)).map { _ =>
          Redirect(routes.CategoriesController.viewCategories()).flashing("success" -> "category updated")
        }
      }
    )
  }

  def deleteCategory(id: Int) = Action.async { implicit request =>
    categoryRepository.delete(id).map(_ => Redirect(routes.CategoriesController.viewCategories()).flashing("success" -> "category deleted"))
  }

  def listCategories: Action[AnyContent] = Action.async { implicit request =>
    categoryRepository.list()
      .map(Json.toJson(_))
      .map(Ok(_))
  }

  def getCategory(id: Int): Action[AnyContent] = Action.async { implicit request =>
    categoryRepository.getByIDOption(id) map {
      case Some(category) => Ok(Json.toJson(category))
      case None => NotFound("Category not found")
    }
  }

  def viewCategories() = Action.async { implicit  reqiest =>
    categoryRepository.list()
      .map(list => Ok(views.html.categories.categoriesView(list)))
  }
}

case class CreateCategoryForm(name: String)

case class UpdateCategoryForm(id: Int, name: String)
