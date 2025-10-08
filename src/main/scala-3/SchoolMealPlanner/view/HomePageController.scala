package SchoolMealPlanner.view

import javafx.fxml.FXML
import javafx.scene.control.{Label, Button}
import scalafx.animation.{KeyFrame, Timeline}
import scalafx.util.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import SchoolMealPlanner.MainApp

class HomePageController:

  @FXML private var dateTimeLabel: Label = _
  @FXML private var welcomeLabel: Label = _
  @FXML private var mealPlanningBtn: Button = _
  @FXML private var ingredientsLibraryBtn: Button = _
  @FXML private var reportBtn: Button = _
  @FXML private var feedbackBtn: Button = _

  @FXML
  def handleMealPlanning(): Unit =
    MainApp.switchScene("/SchoolMealPlanner/view/MealPlanningOverview.fxml")

  @FXML
  private def handleIngredientsLibrary(): Unit =
    MainApp.switchScene("/SchoolMealPlanner/view/IngredientOverview.fxml")

  @FXML
  private def handleReport(): Unit =
    MainApp.switchScene("/SchoolMealPlanner/view/ReportOverview.fxml")

  @FXML
  private def handleFeedback(): Unit =
    MainApp.switchScene("/SchoolMealPlanner/view/FeedbackOverview.fxml")

  @FXML
  def initialize(): Unit =
    val formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy HH:mm:ss")

    val clock = new Timeline {
      cycleCount = Timeline.Indefinite
      keyFrames = Seq(
        KeyFrame(Duration.Zero, onFinished = _ =>
          val now = LocalDateTime.now()
          dateTimeLabel.setText(now.format(formatter))
        ),
        KeyFrame(Duration(1000))
      )
    }
    clock.play()

    MainApp.currentUser.foreach: user =>
      val name = Option(user.name.value).filter(_.nonEmpty)
        .getOrElse(user.email.value.split("@").headOption.getOrElse("User"))
      welcomeLabel.setText(s"Welcome back, $name!")

    val float = new javafx.animation.TranslateTransition(javafx.util.Duration.seconds(1.5), welcomeLabel)
    float.setFromY(0)
    float.setToY(-8)
    float.setAutoReverse(true)
    float.setCycleCount(javafx.animation.Animation.INDEFINITE)
    float.play()