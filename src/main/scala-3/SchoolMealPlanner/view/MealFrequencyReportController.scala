package SchoolMealPlanner.view

import javafx.fxml.FXML
import javafx.scene.chart.{BarChart, XYChart}
import SchoolMealPlanner.MainApp
import javafx.application.Platform
import javafx.scene.layout.VBox

class MealFrequencyReportController:

  @FXML private var mealChart: BarChart[String, Number] = _
  @FXML private var backBox: VBox = _

  @FXML
  def initialize(): Unit =
    val mealCounts = MainApp.mealData.groupBy(_.name.value).view.mapValues(_.size).toMap

    val series = XYChart.Series[String, Number]()
    series.setName("Meal Frequency")

    for mealName -> count <- mealCounts do
      series.getData.add(XYChart.Data[String, Number](mealName, count))

    mealChart.getData.clear()
    mealChart.getData.add(series)

    val colors = List(
      "#4CAF50","#2196F3","#FF9800","#9C27B0","#F44336","#00BCD4","#8BC34A",
      "#E91E63","#3F51B5","#CDDC39","#795548","#FFEB3B","#607D8B","#673AB7"
    )

    Platform.runLater(() =>
      var i = 0
      series.getData.forEach { data =>
        val node = data.getNode
        if node != null then
          node.getStyleClass.removeIf(s => s.startsWith("default-color"))
          node.setStyle(s"-fx-bar-fill: ${colors(i % colors.length)};")
          i += 1
        }
    )

  @FXML
  def handleBack(): Unit =
    val resource = getClass.getResource("/SchoolMealPlanner/view/ReportOverview.fxml")
    val loader = new javafx.fxml.FXMLLoader(resource)
    val reportPage = loader.load[javafx.scene.Parent]()
    val root = backBox.getScene.getRoot match
      case bp: javafx.scene.layout.BorderPane => bp
      case other => throw new IllegalStateException("Expected BorderPane as root layout")

    root.setCenter(reportPage)