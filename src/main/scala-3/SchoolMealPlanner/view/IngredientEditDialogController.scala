package SchoolMealPlanner.view

import SchoolMealPlanner.MainApp
import SchoolMealPlanner.model.Ingredient
import javafx.collections.ObservableList
import scalafx.stage.Stage
import javafx.fxml.FXML
import javafx.scene.control.{ListView, TextField}
import scalafx.scene.control.Alert
import scalafx.scene.control.Alert.AlertType

class IngredientEditDialogController:

  @FXML private var nameField: TextField = _
  @FXML private var calorieField: TextField = _
  @FXML private var proteinField: TextField = _
  @FXML private var fatField: TextField = _
  @FXML private var carbohydrateField: TextField = _
  @FXML private var vitaminListView: ListView[String] = _

  var dialogStage: Stage = _
  private var _ingredient: Ingredient = _
  var okClicked: Boolean = false

  def ingredient: Ingredient = _ingredient

  def ingredient_=(i: Ingredient): Unit =
    _ingredient = i
    nameField.setText(i.name.value)
    calorieField.setText(i.calorie.value.toString)
    proteinField.setText(i.protein.value.toString)
    fatField.setText(i.fat.value.toString)
    carbohydrateField.setText(i.carbohydrate.value.toString)

    vitaminListView.getItems.setAll("A", "B", "C", "D", "E", "K")
    vitaminListView.getSelectionModel.setSelectionMode(javafx.scene.control.SelectionMode.MULTIPLE)

    val selectedVitamins = i.vitamin.value.split(",").map(_.trim)
    selectedVitamins.foreach: v =>
      val index = vitaminListView.getItems.indexOf(v)
      if index >= 0 then vitaminListView.getSelectionModel.select(index)

  @FXML
  def handleOk(): Unit =
    if isInputValid() then
      val rawName = nameField.getText.trim
      val formattedName = if rawName.nonEmpty then rawName.head.toUpper + rawName.tail.toLowerCase else rawName
      
      val isEditing = MainApp.ingredientData.contains(_ingredient)
      val isDuplicate = MainApp.ingredientData.exists(i =>
        i.name.value.equalsIgnoreCase(formattedName) && i != _ingredient
      )

      if isDuplicate && !isEditing then
        val alert = new Alert(AlertType.Error)
        alert.setTitle("Duplicate Ingredient")
        alert.setHeaderText("Ingredient already exists")
        alert.setContentText(s"The ingredient '$formattedName' already exists in the system.")
        alert.showAndWait()
      else
        _ingredient.name.value = formattedName
        _ingredient.calorie.value = calorieField.getText.toDouble
        _ingredient.protein.value = proteinField.getText.toDouble
        _ingredient.fat.value = fatField.getText.toDouble
        _ingredient.carbohydrate.value = carbohydrateField.getText.toDouble

        val selectedVitamins: ObservableList[String] = vitaminListView.getSelectionModel.getSelectedItems
        _ingredient.vitamin.value = selectedVitamins.toArray.map(_.toString).mkString(", ")

        okClicked = true
        dialogStage.close()

  @FXML
  def handleCancel(): Unit =
    dialogStage.close()

  def isInputValid(): Boolean =
    var errorMessage = ""

    def parsePositiveDouble(text: String, fieldName: String): Option[Double] =
      try
        val value = text.toDouble
        if value < 0 then
          errorMessage += s"$fieldName value cannot be a negative number!\n"
          None
        else Some(value)
      catch
        case _: NumberFormatException =>
          errorMessage += s"$fieldName value must be a number!\n"
          None

    def validateField(field: TextField, fieldName: String): Option[Double] =
      val text = Option(field.getText).getOrElse("")
      if text.trim.isEmpty then
        errorMessage += s"The $fieldName value cannot be empty!\n"
        None
      else parsePositiveDouble(text, fieldName)

    if Option(nameField.getText).getOrElse("").trim.isEmpty then
      errorMessage += "The ingredient name cannot be empty!\n"

    val calorieOpt = validateField(calorieField, "calorie")
    val fatOpt = validateField(fatField, "fat")
    val proteinOpt = validateField(proteinField, "protein")
    val carbOpt = validateField(carbohydrateField, "carbohydrate")

    if vitaminListView.getSelectionModel.getSelectedItems.isEmpty then
      errorMessage += "Please select at least a vitamin type!\n"

    if errorMessage.isEmpty then true
    else
      val alert = new Alert(AlertType.Error)
      alert.setTitle("Invalid Fields")
      alert.setHeaderText("Please correct invalid fields")
      alert.setContentText(errorMessage)
      alert.showAndWait()
      false