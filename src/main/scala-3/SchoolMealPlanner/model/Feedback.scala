package SchoolMealPlanner.model

import scalafx.beans.property.{StringProperty, ObjectProperty}
import SchoolMealPlanner.util.Database
import scalikejdbc.*
import java.time.LocalDate
import scala.util.Try

class Feedback(
                var meal: Meal,
                val weekS: String,
                val dayS: String,
                val dateS: LocalDate,
                val typeS: String,
                val contentS: String,
                var submittedBy: User
              ) extends Database:

  def this() = this(new Meal(), "", "", LocalDate.now(), "", "", new User())

  var id           : Option[Long] = None
  var week         = StringProperty(weekS)
  var day          = StringProperty(dayS)
  var date         = ObjectProperty[LocalDate](dateS)
  var feedbackType = StringProperty(typeS)
  var content      = StringProperty(contentS)

  def save(): Try[Int] =
    id match
      case None =>
        Try(DB autoCommit { implicit session =>
          sql"""
              INSERT INTO feedback (meal, week, day, date, feedbackType, content, submittedBy)
              VALUES (${meal.name.value}, ${week.value}, ${day.value}, ${date.value},
                      ${feedbackType.value}, ${content.value}, ${submittedBy.name.value})
            """.updateAndReturnGeneratedKey.apply()
        }).map { newId => id = Some(newId); 1 }
      case Some(existingId) =>
        Try(DB autoCommit { implicit session =>
          sql"""
              UPDATE feedback
              SET meal = ${meal.name.value},
                  week = ${week.value},
                  day  = ${day.value},
                  date = ${date.value},
                  feedbackType = ${feedbackType.value},
                  content = ${content.value},
                  submittedBy = ${submittedBy.email.value}
              WHERE id = $existingId
            """.update.apply()
        })

  def delete(): Try[Int] =
    id match
      case Some(existingId) =>
        Try(DB autoCommit { implicit session =>
          sql"DELETE FROM feedback WHERE id = $existingId".update.apply()
        })
      case None =>
        throw new Exception("Feedback not found in database")

object Feedback extends Database:

  def apply(
             meal: Meal,
             weekS: String,
             dayS: String,
             dateS: LocalDate,
             typeS: String,
             contentS: String,
             submittedBy: User
           ): Feedback =
    new Feedback(meal, weekS, dayS, dateS, typeS, contentS, submittedBy)

  def initializeTable(): Unit =
    DB autoCommit { implicit session =>
      DB.getTable("feedback") match
        case Some(_) =>
        case None =>
          sql"""
            CREATE TABLE feedback (
              id INT GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) PRIMARY KEY,
              meal VARCHAR(100),
              week VARCHAR(50),
              day  VARCHAR(20),
              date DATE,
              feedbackType VARCHAR(100),
              content VARCHAR(1000),
              submittedBy VARCHAR(100)
            )
          """.execute.apply()
    }

  def getAllFeedback: List[Feedback] =
    val mealsMap = Meal.getAllMeals.map(m => m.name.value -> m).toMap
    val usersMap = User.getAllUsers.map(u => u.name.value -> u).toMap

    DB readOnly { implicit session =>
      sql"SELECT * FROM feedback ORDER BY date DESC, week, day, meal"
        .map { rs =>
          val mealName = rs.string("meal")
          val email = rs.string("submittedBy")
          val meal = mealsMap.getOrElse(mealName, new Meal(mealName, "", "", 0.0, Nil, "", ""))
          val user = usersMap.getOrElse(email, new User("", email, "", Role.CafeteriaStaff))

          val fb = Feedback(
            meal,
            rs.string("week"),
            rs.string("day"),
            rs.localDate("date"),
            rs.string("feedbackType"),
            rs.string("content"),
            user
          )
          fb.id = Some(rs.long("id"))
          fb
        }.list.apply()
    }

  def countByWeek: Map[String, Int] =
    getAllFeedback.groupBy(_.week.value).view.mapValues(_.size).toMap

  def countByType: Map[String, Int] =
    getAllFeedback.groupBy(_.feedbackType.value).view.mapValues(_.size).toMap