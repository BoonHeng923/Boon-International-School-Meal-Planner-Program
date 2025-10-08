package SchoolMealPlanner.model

import scalafx.beans.property.StringProperty
import scalafx.collections.ObservableBuffer
import SchoolMealPlanner.util.Database
import scalikejdbc.*
import scala.util.Try

class Meal(
            val nameS: String,
            val descriptionS: String,
            val imagePathS: String,
            val caloriesS: Double,                 
            val ingredientsS: List[Ingredient],   
            val weekS: String,
            val dayS: String
          ) extends Database:

  def this() = this(null, null, null, 0.0, List.empty, null, null)

  var name        = StringProperty(nameS)
  var description = StringProperty(descriptionS)
  var imagePath   = StringProperty(imagePathS)
  var ingredients = ObservableBuffer[Ingredient](ingredientsS*)
  var week        = StringProperty(weekS)
  var day         = StringProperty(dayS)
  
  var originalName: String = nameS
  
  def calories: Double =
    ingredients.map(_.calorie.value).sum

  def save(): Try[Int] =
    val ingredientsCsv = ingredients.map(_.name.value).mkString(",")
    if originalName == null || originalName.trim.isEmpty || !isExist then
      val res = Try(DB autoCommit { implicit session =>
        sql"""
          insert into meal (name, description, imagePath, calories, ingredients, week, day)
          values (${name.value}, ${description.value}, ${imagePath.value}, $calories,
                  $ingredientsCsv, ${week.value}, ${day.value})
        """.update.apply()
      })
      res.foreach(_ => originalName = name.value)
      res
    else
      val res = Try(DB autoCommit { implicit session =>
        sql"""
          update meal
          set "NAME"        = ${name.value},
              "DESCRIPTION" = ${description.value},
              "IMAGEPATH"   = ${imagePath.value},
              "CALORIES"    = ${calories},
              "INGREDIENTS" = $ingredientsCsv
          where "NAME" = ${originalName}
            and "DAY"  = ${day.value}
            and "WEEK" = ${week.value}
        """.update.apply()
      })
      res.foreach(_ => originalName = name.value)
      res

  def delete(): Try[Int] =
    if isExist then
      Try(DB autoCommit { implicit session =>
        sql"""
          delete from meal
          where "NAME" = ${name.value} and "DAY" = ${day.value} and "WEEK" = ${week.value}
        """.update.apply()
      })
    else
      throw new Exception("Meal does not exist.")

  def isExist: Boolean =
    DB readOnly { implicit session =>
      sql"""
        select 1 from meal
        where "NAME" = ${originalName} and "DAY" = ${day.value} and "WEEK" = ${week.value}
      """.map(_.int(1)).list.apply().nonEmpty
    }

object Meal extends Database:

  def apply(
             nameS: String,
             descriptionS: String,
             imagePathS: String,
             caloriesS: Double,
             ingredientsS: List[Ingredient],  
             weekS: String,
             dayS: String
           ): Meal =
    new Meal(nameS, descriptionS, imagePathS, caloriesS, ingredientsS, weekS, dayS)

  def initializeTable(): Unit =
    DB autoCommit { implicit session =>
      val tableExists = DB.getTable("meal").isDefined
      if !tableExists then
        sql"""
          CREATE TABLE meal (
            id INT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) PRIMARY KEY,
            name VARCHAR(100),
            description VARCHAR(1000),
            imagePath VARCHAR(255),
            calories DOUBLE,
            ingredients VARCHAR(1000),
            week VARCHAR(50),
            day VARCHAR(20)
          )
        """.execute.apply()
    }

  private def namesToIngredients(csv: String, ingredientMap: Map[String, Ingredient]): List[Ingredient] =
    Option(csv).getOrElse("")
      .split(",")
      .toList
      .map(_.trim)
      .filter(_.nonEmpty)
      .flatMap(ingredientMap.get)

  def getAllMeals: List[Meal] =
    val ingredientMap = Ingredient.getAllIngredients.map(i => i.name.value -> i).toMap

    DB readOnly { implicit session =>
      sql"select * from meal"
        .map { rs =>
          val ings = namesToIngredients(rs.string("ingredients"), ingredientMap)
          Meal(
            rs.string("name"),
            rs.string("description"),
            rs.string("imagePath"),
            0.0,                 
            ings,
            rs.string("week"),
            rs.string("day")
          )
        }.list.apply()
    }

  def existsByWeekDay(week: String, day: String): Boolean =
    DB readOnly { implicit session =>
      sql"""select 1 from meal where "WEEK" = $week and "DAY" = $day"""
        .map(_.int(1)).list.apply().nonEmpty
    }

  def listByWeekDay(week: String, day: String): List[Meal] =
    val ingredientMap = Ingredient.getAllIngredients.map(i => i.name.value -> i).toMap

    DB readOnly { implicit session =>
      sql"""select * from meal where "WEEK" = $week and "DAY" = $day order by "NAME""""
        .map { rs =>
          val ings = namesToIngredients(rs.string("ingredients"), ingredientMap)
          Meal(
            rs.string("name"),
            rs.string("description"),
            rs.string("imagePath"),
            0.0,
            ings,
            rs.string("week"),
            rs.string("day")
          )
        }.list.apply()
    }