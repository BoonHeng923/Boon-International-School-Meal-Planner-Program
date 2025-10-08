package SchoolMealPlanner.model

class CafeteriaStaff(nameS: String, emailS: String, passwordS: String)
  extends User(nameS, emailS, passwordS, Role.CafeteriaStaff)

object CafeteriaStaff:
  def apply(name: String, email: String, password: String): CafeteriaStaff =
    new CafeteriaStaff(name, email, password)