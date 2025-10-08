package SchoolMealPlanner.view

import javafx.fxml.FXML
import SchoolMealPlanner.MainApp
import javafx.scene.layout.VBox

class ReportOverviewController:

  @FXML private var backBox: VBox = _
  
  @FXML
  def handleViewMealFrequency(): Unit = 
    MainApp.switchScene("/SchoolMealPlanner/view/MealFrequencyReport.fxml")

  @FXML
  def handleViewIngredientUsage(): Unit = 
    MainApp.switchScene("/SchoolMealPlanner/view/IngredientUsageReport.fxml")

  @FXML
  def handleViewFeedbackSummary(): Unit = 
    MainApp.switchScene("/SchoolMealPlanner/view/FeedbackSummaryReport.fxml")

  @FXML
  def handleViewNutritionalOverview(): Unit =
    MainApp.switchScene("/SchoolMealPlanner/view/NutritionalOverviewReport.fxml")

  @FXML
  def handleBack(): Unit = 
    val resource = getClass.getResource("/SchoolMealPlanner/view/HomePage.fxml")
    val loader = new javafx.fxml.FXMLLoader(resource)
    val homePage = loader.load[javafx.scene.Parent]()
    val root = backBox.getScene.getRoot match
      case bp: javafx.scene.layout.BorderPane => bp
      case other => throw new IllegalStateException("Expected BorderPane as root layout")

    root.setCenter(homePage)