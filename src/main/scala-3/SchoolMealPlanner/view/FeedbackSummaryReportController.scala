package SchoolMealPlanner.view

import javafx.fxml.FXML
import javafx.scene.chart.{BarChart, XYChart}
import SchoolMealPlanner.MainApp
import javafx.application.Platform
import javafx.scene.layout.VBox

class FeedbackSummaryReportController:

  @FXML private var feedbackChart: BarChart[String, Number] = _
  @FXML private var backBox: VBox = _

  @FXML
  def initialize(): Unit =
    val feedbackCounts = MainApp.feedbackData
      .groupBy(_.feedbackType.value.trim)
      .view.mapValues(_.size)
      .toMap

    val series = new XYChart.Series[String, Number]()
    series.setName("Feedback Count")

    for ((feedbackType, count) <- feedbackCounts) do
      series.getData.add(new XYChart.Data[String, Number](feedbackType, count))

    feedbackChart.setAnimated(false)
    feedbackChart.getData.clear()
    feedbackChart.getData.add(series)
    
    val colors = List("#4CAF50", "#9C27B0", "#FFC107", "#2196F3")
    
    Platform.runLater(() =>
      var i = 0
      series.getData.forEach { data =>
        val node = data.getNode
        if node != null then
          node.getStyleClass.removeIf(s => s.startsWith("default-color"))
          val color = colors(i % colors.length)
          node.setStyle(s"-fx-bar-fill: $color;")
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
      case _ => throw new IllegalStateException("Expected BorderPane as root layout")

    root.setCenter(reportPage)