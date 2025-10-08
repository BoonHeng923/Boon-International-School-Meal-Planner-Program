package SchoolMealPlanner.view

import SchoolMealPlanner.model.{Feedback, Role}
import SchoolMealPlanner.MainApp
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import scalafx.Includes.*
import javafx.beans.property.ReadOnlyStringWrapper
import javafx.scene.layout.VBox
import scalafx.collections.ObservableBuffer

class FeedbackOverviewController:

  @FXML private var feedbackTable: TableView[Feedback] = _
  @FXML private var mealColumn: TableColumn[Feedback, String] = _
  @FXML private var weekDayColumn: TableColumn[Feedback, String] = _
  @FXML private var mealLabel: Label = _
  @FXML private var weekDayLabel: Label = _
  @FXML private var typeLabel: Label = _
  @FXML private var contentLabel: Label = _
  @FXML private var submitterLabel: Label = _
  @FXML private var addButton: Button = _
  @FXML private var editButton: Button = _
  @FXML private var deleteButton: Button = _
  @FXML private var sortButton: Button = _
  @FXML private var searchField: TextField = _
  @FXML private var backBox: VBox = _

  private var isNutritionist: Boolean = false
  private val feedbackData = MainApp.feedbackData

  @FXML
  def initialize(): Unit =
    feedbackTable.items = feedbackData

    mealColumn.setCellValueFactory(cellData => new ReadOnlyStringWrapper(cellData.getValue.meal.name.value))
    weekDayColumn.setCellValueFactory(cellData =>
      new ReadOnlyStringWrapper(cellData.getValue.week.value + " - " + cellData.getValue.day.value)
    )

    showFeedbackDetails(None)

    feedbackTable.getSelectionModel.selectedItemProperty.addListener(
      (_, _, newValue) => showFeedbackDetails(Option(newValue))
    )

    MainApp.currentUser match
      case Some(user) =>
        if user.role == Role.Nutritionist then
          isNutritionist = true
          addButton.setVisible(false)
          editButton.setVisible(false)
          deleteButton.setVisible(false)
      case _ =>
        addButton.setVisible(false)
        editButton.setVisible(false)
        deleteButton.setVisible(false)

    searchField.setOnKeyPressed { event =>
      if (event.getCode == KeyCode.ENTER) handleSearch()
    }

  private def showFeedbackDetails(feedbackOpt: Option[Feedback]): Unit =
    feedbackOpt match
      case Some(f) =>
        mealLabel.setText(f.meal.name.value)
        weekDayLabel.setText(s"${f.week.value} - ${f.day.value}")
        typeLabel.setText(f.feedbackType.value)

        val original = f.content.value.trim
        val formatted = if original.nonEmpty then original.head.toUpper + original.tail.toLowerCase else ""
        contentLabel.setText(formatted)

        submitterLabel.setText(f.submittedBy.name.value)
      case None =>
        mealLabel.setText("")
        weekDayLabel.setText("")
        typeLabel.setText("")
        contentLabel.setText("")
        submitterLabel.setText("")

  @FXML
  def handleAdd(): Unit =
    val newFeedback = new Feedback()
    val okClicked = MainApp.showFeedbackEditDialog(newFeedback, "Add Feedback")
    if okClicked then 
      newFeedback.save()
      feedbackData.insert(0, newFeedback)
      feedbackTable.items = feedbackData
      feedbackTable.getSelectionModel.select(0)

  @FXML
  def handleDelete(): Unit =
    val selected = feedbackTable.getSelectionModel.getSelectedItem
    if selected != null then
      val confirm = new Alert(Alert.AlertType.CONFIRMATION)
      confirm.setTitle("Delete Feedback")
      confirm.setHeaderText("Are you sure you want to delete this feedback?")
      confirm.setContentText(s"Meal: ${selected.meal.name.value}")

      val result = confirm.showAndWait()
      if result.isPresent && result.get == ButtonType.OK then
        selected.delete()
        feedbackData -= selected
        feedbackTable.items = feedbackData
        
    else 
      val alert = new Alert(Alert.AlertType.ERROR)
      alert.setTitle("No Selection")
      alert.setHeaderText("No Feedback Selected")
      alert.setContentText("Please select a feedback in the table.")
      alert.showAndWait()
    
  @FXML
  def handleEdit(): Unit =
    val selected = feedbackTable.getSelectionModel.getSelectedItem
    if selected != null then
      val okClicked = MainApp.showFeedbackEditDialog(selected, "Edit Feedback")
      if okClicked then
        selected.save()
        feedbackTable.refresh()
        showFeedbackDetails(Some(selected))

    else 
      val alert = new Alert(Alert.AlertType.ERROR)
      alert.setTitle("No Selection")
      alert.setHeaderText("No Feedback Selected")
      alert.setContentText("Please select a feedback in the table.")
      alert.showAndWait()

  private def parseWeek(week: String): Int = 
    """Week (\d+)""".r.findFirstMatchIn(week).map(_.group(1).toInt).getOrElse(0)

  private def parseDay(day: String): Int = 
    day.toLowerCase match
      case "monday" => 1
      case "tuesday" => 2
      case "wednesday" => 3
      case "thursday" => 4
      case "friday" => 5
      case _ => 6

  @FXML
  def handleSort(): Unit =
    MainApp.showFeedbackSortDialog() match
      case Some("Name A-Z") =>
        feedbackTable.items = ObservableBuffer.from(feedbackData.sortBy(_.meal.name.value.toLowerCase))
      case Some("Name Z-A") =>
        feedbackTable.items = ObservableBuffer.from(feedbackData.sortBy(_.meal.name.value.toLowerCase).reverse)
      case Some("Date ↑") =>
        feedbackTable.items = ObservableBuffer.from(feedbackData.sortBy(fb => (parseWeek(fb.week.value), parseDay(fb.day.value))))
      case Some("Date ↓") =>
        feedbackTable.items = ObservableBuffer.from(feedbackData.sortBy(fb => (parseWeek(fb.week.value), parseDay(fb.day.value))).reverse)
      case _ =>

  @FXML
  def handleSearch(): Unit =
    val keyword = searchField.getText.trim.toLowerCase

    if keyword.nonEmpty then
      val matchOpt = feedbackData.find(fb => fb.meal.name.value.toLowerCase.contains(keyword))

      matchOpt match 
        case Some(feedback) =>
          showFeedbackDetails(Some(feedback))
        case None =>
          val alert = new Alert(Alert.AlertType.ERROR)
          alert.setTitle("Meal Not Found")
          alert.setHeaderText("No match found")
          alert.setContentText(s"No meal found matching \"$keyword\".")
          alert.showAndWait()
          showFeedbackDetails(None)
      
    else
      feedbackTable.getSelectionModel.clearSelection()
      showFeedbackDetails(None)

  @FXML
  def handleBack(): Unit =
    val resource = getClass.getResource("/SchoolMealPlanner/view/HomePage.fxml")
    val loader = new javafx.fxml.FXMLLoader(resource)
    val homePage = loader.load[javafx.scene.Parent]()
    val root = backBox.getScene.getRoot match
      case bp: javafx.scene.layout.BorderPane => bp
      case other => throw new IllegalStateException("Expected BorderPane as root layout")

    root.setCenter(homePage)