package SchoolMealPlanner.model

import scalafx.beans.property.{StringProperty, DoubleProperty}
import SchoolMealPlanner.util.Database
import scalikejdbc._
import scala.util.Try

class Ingredient(
                  val nameS: String,
                  val calorieS: Double,
                  val proteinS: Double,
                  val fatS: Double,
                  val carbohydrateS: Double,
                  val vitaminS: String
                ) extends Database:
  def this() = this(null, 0.0, 0.0, 0.0, 0.0, "")

  var name    =  StringProperty(nameS)
  var calorie =  DoubleProperty(calorieS)
  var protein =  DoubleProperty(proteinS)
  var fat     =  DoubleProperty(fatS)
  var carbohydrate = DoubleProperty(carbohydrateS)
  var vitamin = StringProperty(vitaminS)

  def save(): Try[Int] =
    if (!isExist) {
      Try(DB autoCommit { implicit session =>
        sql"""
          INSERT INTO ingredient (name, calorie, protein, fat, carbohydrate, vitamin)
          VALUES (${name.value}, ${calorie.value}, ${protein.value}, ${fat.value}, ${carbohydrate.value}, ${vitamin.value})
        """.update.apply()
      })
    } else {
      Try(DB autoCommit { implicit session =>
        sql"""
          UPDATE ingredient
          SET calorie = ${calorie.value},
              protein = ${protein.value},
              fat = ${fat.value},
              carbohydrate = ${carbohydrate.value},
              vitamin = ${vitamin.value}
          WHERE name = ${name.value}
        """.update.apply()
      })
    }

  def delete(): Try[Int] =
    if (isExist) then
      Try(DB autoCommit { implicit session =>
        sql"""
        delete from ingredient where name = ${name.value}
        """.update.apply()
      })
    else
      throw new Exception("Ingredient not Exists in Database")

  def isExist: Boolean =
    DB readOnly { implicit session =>
      sql"select * from ingredient where name = ${name.value}"
        .map(rs => rs.string("name")).single.apply()
    } match
      case Some(_) => true
      case None    => false

  override def toString: String = name.value

object Ingredient extends Database:
  
  def apply(
             nameS: String,
             calorieS: Double,
             proteinS: Double,
             fatS: Double,
             carbohydrateS: Double,
             vitaminS: String
           ): Ingredient =
    new Ingredient(nameS, calorieS, proteinS, fatS, carbohydrateS, vitaminS)

  def initializeTable(): Unit = {
    if (DB.getTable("INGREDIENT").isEmpty) {
      DB autoCommit { implicit session =>
        sql"""
          CREATE TABLE ingredient (
            id INT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) PRIMARY KEY,
            name VARCHAR(100),
            calorie DOUBLE,
            protein DOUBLE,
            fat DOUBLE,
            carbohydrate DOUBLE,
            vitamin VARCHAR(255)
          )
        """.execute.apply()
      }
    }
  }

  def getAllIngredients: List[Ingredient] =
    DB readOnly { implicit session =>
      sql"select * from ingredient"
        .map(rs => Ingredient(
          rs.string("name"),
          rs.double("calorie"),
          rs.double("protein"),
          rs.double("fat"),
          rs.double("carbohydrate"),
          rs.string("vitamin")
        )).list.apply()
    }