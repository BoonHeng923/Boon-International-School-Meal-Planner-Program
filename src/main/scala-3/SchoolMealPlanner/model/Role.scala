package SchoolMealPlanner.model

sealed trait Role:
  def label: String
  def canEditMeals: Boolean
  def canEditIngredients: Boolean
  def canEditFeedback: Boolean

object Role:
  case object Nutritionist extends Role:
    val label = "Nutritionist"
    val canEditMeals = true
    val canEditIngredients = true
    val canEditFeedback = false

  case object CafeteriaStaff extends Role:
    val label = "Cafeteria Staff"
    val canEditMeals = false
    val canEditIngredients = false
    val canEditFeedback = true
  
  def fromString(s: String): Role = s match
    case "Nutritionist"    => Nutritionist
    case "Cafeteria Staff" => CafeteriaStaff
    case other             => throw IllegalArgumentException(s"Unknown role: $other")

  def toString(r: Role): String = r.label