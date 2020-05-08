package repositories

import javax.inject.{Inject, Singleton}
import models.Employee
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EmployeeRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private class EmployeeTable(tag: Tag) extends Table[Employee](tag, "employees") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def firstName = column[String]("first_name")
    def lastName = column[String]("last_name")
    def position = column[String]("position")

    def * = (id, firstName, lastName, position) <> ((Employee.apply _).tupled, Employee.unapply)
  }

  private val employees = TableQuery[EmployeeTable]

  def create(firstName: String, lastName: String, position: String) = db.run {
    (employees.map(c => (c.firstName, c.lastName, c.position))
      returning employees.map(_.id)
      into { case ((firstName, lastName, position), id) => Employee(id, firstName, lastName, position) }
      ) += (firstName, lastName, position)
  }

  def list(): Future[Seq[Employee]] = db.run(employees.result)

  def getByIDOption(id: Int):Future[Option[Employee]] = db.run(employees.filter(_.id === id).result.headOption)

  def delete(id: Int) = db.run(employees.filter(_.id === id).delete).map(_ => ())

  def update(id: Int, newEmployee: Employee) = {
    val newEmployeeUpdate = newEmployee.copy(id)
    db.run(employees.filter(_.id === id).update(newEmployeeUpdate)).map(_ => ())
  }
}
