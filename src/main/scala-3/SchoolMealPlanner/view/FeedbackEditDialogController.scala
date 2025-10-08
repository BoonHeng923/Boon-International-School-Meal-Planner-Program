package SchoolMealPlanner.view

import SchoolMealPlanner.MainApp
import SchoolMealPlanner.model.{Feedback, Meal, User}
import scalafx.stage.Stage
import javafx.fxml.FXML
import javafx.scene.control.{ChoiceBox, TextField}
import scalafx.scene.control.Alert
import scalafx.scene.control.Alert.AlertType

import java.time.LocalDate

class FeedbackEditDialogController:

  @FXML private var weekChoice: ChoiceBox[String] = _
  @FXML private var dayChoice: ChoiceBox[String] = _
  @FXML private var typeChoice: ChoiceBox[String] = _
  @FXML private var contentArea: TextField = _

  var dialogStage: Stage = _
  private var _feedback: Feedback = _
  var okClicked: Boolean = false

  def feedback: Feedback = _feedback

  def feedback_=(fb: Feedback): Unit =
    _feedback = fb
    
    weekChoice.getItems.setAll("July Week 1", "July Week 2", "July Week 3", "July Week 4", "July Week 5")
    dayChoice.getItems.setAll("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
    
    weekChoice.setValue(fb.week.value)
    dayChoice.setValue(fb.day.value)
    
    val editingExisting = fb.week.value != null && fb.week.value.nonEmpty &&
      fb.day.value != null && fb.day.value.nonEmpty
    weekChoice.setDisable(editingExisting)
    dayChoice.setDisable(editingExisting)
    
    typeChoice.getItems.setAll("Unpopular", "Uneaten", "Allergy Issue", "Other")
    if fb.feedbackType.value != null && fb.feedbackType.value.nonEmpty then
      typeChoice.setValue(fb.feedbackType.value)

    contentArea.setText(Option(fb.content.value).getOrElse(""))
  
  @FXML
  def handleOk(): Unit =
    if isInputValid() then
      val selectedWeek = weekChoice.getValue
      val selectedDay = dayChoice.getValue

      val meals = Meal.listByWeekDay(selectedWeek, selectedDay)
      if meals.isEmpty then
        val alert = new Alert(AlertType.Warning)
        alert.setTitle("No Meal Found")
        alert.setHeaderText("No meal available")
        alert.setContentText(s"No meal found for $selectedWeek - $selectedDay.")
        alert.showAndWait()
        return
      

      val meal = meals.head

      _feedback.meal = meal
      _feedback.week.value = selectedWeek
      _feedback.day.value = selectedDay
      _feedback.date.value = LocalDate.now()
      _feedback.feedbackType.value = typeChoice.getValue
      _feedback.content.value = contentArea.getText.trim
      _feedback.submittedBy = MainApp.currentUser.getOrElse(new User())

      okClicked = true
      dialogStage.close()

  @FXML
  def handleCancel(): Unit =
    dialogStage.close()

  private def isInputValid(): Boolean =
    val error = new StringBuilder

    if Option(weekChoice.getValue).forall(_.trim.isEmpty) then
      error ++= "Week must be selected!\n"
    if Option(dayChoice.getValue).forall(_.trim.isEmpty) then 
      error ++= "Day must be selected!\n"
    if Option(typeChoice.getValue).forall(_.trim.isEmpty) then
      error ++= "Feedback type must be selected!\n"
    if Option(contentArea.getText).forall(_.trim.isEmpty) then 
      error ++= "Feedback content cannot be empty!\n"

    if (error.isEmpty) true
    else
      val alert = new Alert(AlertType.Error)
      alert.setTitle("Invalid Fields")
      alert.setHeaderText("Please correct the following fields")
      alert.setContentText(error.toString)
      alert.showAndWait()
      false