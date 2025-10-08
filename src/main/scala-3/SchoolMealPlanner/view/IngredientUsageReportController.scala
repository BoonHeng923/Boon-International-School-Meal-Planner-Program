package SchoolMealPlanner.view

import javafx.fxml.FXML
import javafx.scene.chart.{BarChart, XYChart}
import SchoolMealPlanner.MainApp
import javafx.application.Platform
import javafx.scene.layout.VBox

class IngredientUsageReportController:

  @FXML private var ingredientChart: BarChart[String, Number] = _
  @FXML private var backBox: VBox = _

  @FXML
  def initialize(): Unit =
    val ingredientCounts = MainApp.mealData
      .flatMap(_.ingredients)
      .filter(i => i != null && i.name.value.trim.nonEmpty)
      .map(_.name.value.trim.toLowerCase)
      .groupBy(identity)
      .view.mapValues(_.size)
      .toMap

    val series = XYChart.Series[String, Number]()
    series.setName("Ingredient Usage Count")

    for ingredient -> count <- ingredientCounts.toSeq.sortBy(_._1) do
      val label = ingredient.split(" ").map(_.capitalize).mkString(" ")
      series.getData.add(XYChart.Data[String, Number](label, count: java.lang.Integer))

    ingredientChart.setAnimated(false)
    ingredientChart.getData.clear()
    ingredientChart.getData.add(series)
    
    val colors = List(
      "#4CAF50","#2196F3","#FF9800","#9C27B0","#F44336","#00BCD4","#8BC34A","#E91E63",
      "#3F51B5","#CDDC39","#795548","#FFEB3B","#607D8B","#673AB7","#FF5722","#009688",
      "#FFC107","#9E9E9E","#C2185B","#7B1FA2","#1976D2","#0288D1","#388E3C","#F57C00",
      "#455A64","#D32F2F","#303F9F","#00796B","#689F38","#FBC02D","#5D4037","#8E24AA"
    )
    
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
      case other => throw new IllegalStateException("Expected BorderPane as root layout")

    root.setCenter(reportPage)