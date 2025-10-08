package SchoolMealPlanner.util
import scalikejdbc.*
import SchoolMealPlanner.model.{User, Meal, Feedback, Ingredient}

trait Database :
  val derbyDriverClassname = "org.apache.derby.jdbc.EmbeddedDriver"
  val dbURL = "jdbc:derby:myDB;create=true;";
  Class.forName(derbyDriverClassname)
  ConnectionPool.singleton(dbURL, "me", "mine")
  given AutoSession = AutoSession

object Database extends Database :
  def setupDB() =
    if (!hasDBInitialize) {
      User.initializeTable()
      Meal.initializeTable()
      Ingredient.initializeTable()
      Feedback.initializeTable()
    }

  def hasDBInitialize : Boolean =
    DB getTable "User"
    DB getTable "Meal"
    DB getTable "Ingredient"
    DB getTable "Feedback" match
      case Some(x) => true
      case None => false

