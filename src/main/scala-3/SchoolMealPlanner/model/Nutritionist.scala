package SchoolMealPlanner.model

class Nutritionist(nameS: String, emailS: String, passwordS: String)
  extends User(nameS, emailS, passwordS, Role.Nutritionist)

object Nutritionist:
  def apply(name: String, email: String, password: String): Nutritionist =
    new Nutritionist(name, email, password)