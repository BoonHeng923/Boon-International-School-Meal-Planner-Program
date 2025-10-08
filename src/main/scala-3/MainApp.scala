package SchoolMealPlanner

import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.stage.Stage
import scalafx.scene.Scene
import javafx.fxml.FXMLLoader
import javafx.scene.Parent as JfxParent
import scalafx.collections.ObservableBuffer
import SchoolMealPlanner.model.{Feedback, Ingredient, Meal, User}
import SchoolMealPlanner.util.Database
import scalafx.stage.Modality
import scalafx.Includes.*
import SchoolMealPlanner.view.{AboutController, AnnouncementDialogController, FeedbackEditDialogController, FeedbackSortDialogController, IngredientEditDialogController, IngredientSortDialogController, MealDetailDialogController, MealEditDialogController, RootLayoutController}
import javafx.scene.layout.BorderPane
import javafx.util.Duration
import javafx.animation.PauseTransition
import java.net.URL

object MainApp extends JFXApp3:

  Database.setupDB()

  var rootPane: Option[javafx.scene.layout.BorderPane] = None
  var rootLayout: BorderPane = _
  var currentUser: Option[User] = None
  val mealData: ObservableBuffer[Meal] = ObservableBuffer()
  val ingredientData: ObservableBuffer[Ingredient] = ObservableBuffer()
  val feedbackData: ObservableBuffer[Feedback] = ObservableBuffer()

  mealData ++= Meal.getAllMeals
  ingredientData ++= Ingredient.getAllIngredients.reverse
  feedbackData ++= Feedback.getAllFeedback.reverse

  override def start(): Unit =
    User.initializeTable()
    Meal.initializeTable()
    Ingredient.initializeTable()
    Feedback.initializeTable()

    val rootLayoutResource: URL = getClass.getResource("/SchoolMealPlanner/view/RootLayout.fxml")
    val loader = new FXMLLoader(rootLayoutResource)
    val rootLayout = loader.load[BorderPane]()
    rootPane = Some(rootLayout)

    stage = new PrimaryStage():
      title = "School Meal Planner"
      scene = Scene(rootLayout)
      minWidth = 900
      minHeight = 600
      resizable = true
      maximized = true
    stage.centerOnScreen()

    showLoginPage()

  def loginSuccessful(user: User): Unit =
    currentUser = Some(user)
    showHomePage()

    val delay = new PauseTransition(Duration.millis(200))
    delay.setOnFinished(_ => showAnnouncementDialog())
    delay.play()

  def showLoginPage(): Unit =
    switchScene("/SchoolMealPlanner/view/Login.fxml")
    RootLayoutController.instance.foreach(_.hideTopBar())

  def showAnnouncementDialog(): Unit =
    val loader = FXMLLoader(getClass.getResource("/SchoolMealPlanner/view/AnnouncementDialog.fxml"))
    val root = loader.load[JfxParent]()
    val ctrl = loader.getController[AnnouncementDialogController]

    val dialog = new scalafx.stage.Stage():
      initOwner(stage)
      initModality(Modality.ApplicationModal)
      resizable = false
      title = "Announcement"
      scene = Scene(new scalafx.scene.Parent(root) {})
    ctrl.stage = Option(dialog)
    dialog.show()

  def showHomePage(): Unit =
    switchScene("/SchoolMealPlanner/view/HomePage.fxml")
    RootLayoutController.instance.foreach(_.showTopBar())

  def showAbout(): Boolean =
    val loader = FXMLLoader(getClass.getResource("/SchoolMealPlanner/view/About.fxml"))
    loader.load()
    val pane = loader.getRoot[javafx.scene.layout.AnchorPane]()
    val myWindow = new Stage():
      initOwner(stage)
      initModality(Modality.ApplicationModal)
      title = "About"
      scene = new Scene(new scalafx.scene.Parent(pane) {})
    val ctrl = loader.getController[AboutController]()
    ctrl.stage = Option(myWindow)
    myWindow.showAndWait()
    ctrl.okClicked

  def switchScene(fxmlPath: String): Unit =
    val resource = getClass.getResource(fxmlPath)
    println(s"switchScene: resource = $resource")
    if resource == null then
      println(s"ERROR: FXML file not found at path: $fxmlPath")
      return

    val loader = new FXMLLoader(resource)
    val content = loader.load[javafx.scene.Parent]()
    rootPane match
      case Some(borderPane) => borderPane.setCenter(content)
      case None => println("ERROR: Root layout is not initialized.")

  private def showDialog[T](
    fxml: String,
    dialogTitle: String,
   init: (T, Stage) => Unit
  ): Option[T] =
    val loader = FXMLLoader(getClass.getResource(fxml))
    val root: JfxParent = loader.load()
    val controller = loader.getController[T]

    val dialog = new Stage:
      initModality(Modality.ApplicationModal)
      initOwner(stage)
      resizable = false
      this.title = dialogTitle
      scene = Scene(new scalafx.scene.Parent(root) {})

    init(controller, dialog)
    dialog.showAndWait()
    Some(controller)

  def showMealEditDialog(meal: Meal, dialogTitle: String = "Edit Meal"): Boolean =
    showDialog[MealEditDialogController]("/SchoolMealPlanner/view/MealEditDialog.fxml", dialogTitle,
      (ctrl, dialog) =>
        ctrl.dialogStage = dialog
        ctrl.meal = meal
    ).exists(_.okClicked)

  def showMealDetailDialog(meal: Meal, dialogTitle: String = "Meal Details"): Boolean =
    showDialog[MealDetailDialogController]("/SchoolMealPlanner/view/MealDetailDialog.fxml", dialogTitle,
      (ctrl, dialog) =>
        ctrl.dialogStage = dialog
        ctrl.setMeal(meal)
    ).exists(_.isOkClicked)

  def showIngredientEditDialog(ingredient: Ingredient, dialogTitle: String = "Edit Ingredient"): Boolean =
    showDialog[IngredientEditDialogController]("/SchoolMealPlanner/view/IngredientEditDialog.fxml", dialogTitle,
      (ctrl, dialog) =>
        ctrl.dialogStage = dialog
        ctrl.ingredient = ingredient
    ).exists(_.okClicked)

  def showIngredientsSortDialog(): Option[String] =
    showDialog[IngredientSortDialogController]("/SchoolMealPlanner/view/IngredientSortDialog.fxml", "Sort Options",
      (ctrl, dialog) => ctrl.dialogStage = dialog
    ).flatMap(_.selectedOption)

  def showFeedbackEditDialog(feedback: Feedback, dialogTitle: String = "Edit Feedback"): Boolean =
    showDialog[FeedbackEditDialogController]("/SchoolMealPlanner/view/FeedbackEditDialog.fxml", dialogTitle,
      (ctrl, dialog) =>
        ctrl.dialogStage = dialog
        ctrl.feedback = feedback
    ).exists(_.okClicked)

  def showFeedbackSortDialog(): Option[String] =
    showDialog[FeedbackSortDialogController]("/SchoolMealPlanner/view/FeedbackSortDialog.fxml", "Sort Options",
      (ctrl, dialog) => ctrl.dialogStage = dialog
    ).flatMap(_.selectedOption)