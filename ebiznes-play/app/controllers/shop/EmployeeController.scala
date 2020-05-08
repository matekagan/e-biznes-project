package controllers.shop

import javax.inject.{Inject, Singleton}
import models.Employee
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc.{MessagesAbstractController, MessagesControllerComponents}
import repositories.EmployeeRepository

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EmployeeController @Inject()(employeeRepository: EmployeeRepository, cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  def newEmployeeForm: Form[AddEmployeeForm] = Form {
    mapping(
      "firstName" -> nonEmptyText,
      "lastName" -> nonEmptyText,
      "position" -> nonEmptyText
    )(AddEmployeeForm.apply)(AddEmployeeForm.unapply)
  }

  def updatingEmployeeForm: Form[UpdateEmployeeForm] = Form {
    mapping(
      "id" -> number,
      "firstName" -> nonEmptyText,
      "lastName" -> nonEmptyText,
      "position" -> nonEmptyText
    )(UpdateEmployeeForm.apply)(UpdateEmployeeForm.unapply)
  }

  def addEmployeeForm = Action { implicit request =>
    Ok(views.html.employees.addEmployeeForm(newEmployeeForm))
  }

  def addEmployeeHandle = Action.async { implicit request =>
    newEmployeeForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.employees.addEmployeeForm(errorForm))
        )
      },
      employee => {
        employeeRepository.create(employee.firstName, employee.lastName, employee.position).map { _ =>
          Redirect(routes.EmployeeController.viewEmployees()).flashing("success" -> "employee added")
        }
      }
    )
  }

  def updateEmployeeForm(id: Int) = Action.async { implicit request =>
    val employeeOption = employeeRepository.getByIDOption(id)
    employeeOption.map {
      employee =>
        employee.map(value => updatingEmployeeForm.fill(UpdateEmployeeForm(value)))
          .map(form => Ok(views.html.employees.updateEmployeeForm(form)))
          .getOrElse(Ok("dupa"))
    }
  }

  def updateEmployeeHandle = Action.async { implicit request =>
    updatingEmployeeForm.bindFromRequest.fold(
      errors => Future.successful(Ok(views.html.employees.updateEmployeeForm(errors))),
      employee => {
        employeeRepository.update(employee.id, employee.getEmployee).map { _ =>
          Redirect(routes.EmployeeController.viewEmployees()).flashing("success" -> "employee updated")
        }
      }
    )
  }

  def deleteEmployee(id: Int) = Action.async { implicit request =>
    employeeRepository.delete(id)
      .map(_ => Redirect(routes.EmployeeController.viewEmployees()).flashing("success" -> "employee deleted"))
  }

  def listEmployees = Action.async {
    employeeRepository.list()
      .map(list => Json.toJson(list))
      .map(jsVal => Ok(jsVal))
  }

  def getEmployee(id: Int) = Action.async {
    employeeRepository.getByIDOption(id) map {
      case Some(employee) => Ok(Json.toJson(employee))
      case None => NotFound("employee not found")
    }
  }

  def viewEmployees() = Action.async { implicit request =>
    employeeRepository.list()
      .map(list => Ok(views.html.employees.employeesView(list)))
  }
}

case class AddEmployeeForm(firstName: String, lastName: String, position: String)

case class UpdateEmployeeForm(id: Int, firstName: String, lastName: String, position: String) {
  def getEmployee = new Employee(id, firstName, lastName, position)
}

object UpdateEmployeeForm {
  def apply(id: Int, firstName: String, lastName: String, position: String): UpdateEmployeeForm = new UpdateEmployeeForm(id, firstName, lastName, position)

  def apply(employee: Employee) = new UpdateEmployeeForm(employee.id, employee.firstName, employee.lastName, employee.position)
}