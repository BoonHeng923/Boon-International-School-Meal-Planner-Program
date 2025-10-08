package SchoolMealPlanner.view

import javafx.event.ActionEvent
import javafx.fxml.{FXML, FXMLLoader}
import javafx.scene.input.MouseEvent
import javafx.scene.layout.{VBox, HBox}
import javafx.scene.control.{Alert, ButtonType}
import SchoolMealPlanner.MainApp

class RootLayoutController:
  
  @FXML private var topNavBar: HBox = _
  RootLayoutController.instance = Some(this)

  @FXML private var homePageBox: VBox = _
  @FXML private var mealPlanningBox: VBox = _
  @FXML private var ingredientsBox: VBox = _
  @FXML private var feedbackBox: VBox = _
  @FXML private var reportBox: VBox = _
  @FXML private var logoutBox: VBox = _

  @FXML
  def initialize(): Unit =
    RootLayoutController.instance = Some(this)
    showTopBar()

  private def loadPage(fxml: String): Unit =
    val resource = getClass.getResource("/SchoolMealPlanner/view/" + fxml)
    val content = FXMLLoader.load[javafx.scene.Parent](resource)
    MainApp.rootPane.foreach(_.setCenter(content))

    if fxml == "Login.fxml" then hideTopBar()
    else showTopBar()

  @FXML def handleHomePageClick(event: MouseEvent): Unit = loadPage("HomePage.fxml")
  @FXML def handleMealPlanningClick(event: MouseEvent): Unit = loadPage("MealPlanningOverview.fxml")
  @FXML def handleIngredientsClick(event: MouseEvent): Unit = loadPage("IngredientOverview.fxml")
  @FXML def handleFeedbackClick(event: MouseEvent): Unit = loadPage("FeedbackOverview.fxml")
  @FXML def handleReportClick(event: MouseEvent): Unit = loadPage("ReportOverview.fxml")

  @FXML
  def handleLogoutClick(event: MouseEvent): Unit =
    val alert = new Alert(Alert.AlertType.CONFIRMATION)
    alert.setTitle("Logout Confirmation")
    alert.setHeaderText("Are you sure want to logout?")
    alert.setContentText("Your session will be cleared.")
    val result = alert.showAndWait()

    if result.isPresent && result.get == ButtonType.OK then
      loadPage("Login.fxml")

  def hideTopBar(): Unit =
    if topNavBar != null then
      topNavBar.setVisible(false)
      topNavBar.setManaged(false)

  def showTopBar(): Unit =
    if topNavBar != null then
      topNavBar.setVisible(true)
      topNavBar.setManaged(true)

  @FXML
  def handleClose(action: ActionEvent): Unit =
    System.exit(0)

  @FXML
  def handleAbout(action: ActionEvent): Unit =
    MainApp.showAbout()

  @FXML
  def handleUserProfileClick(event: MouseEvent): Unit =
    val resource = getClass.getResource("/SchoolMealPlanner/view/UserProfile.fxml")
    val content = FXMLLoader.load[javafx.scene.Parent](resource)
    MainApp.rootPane.foreach(_.setCenter(content))

object RootLayoutController:
  var instance: Option[RootLayoutController] = None