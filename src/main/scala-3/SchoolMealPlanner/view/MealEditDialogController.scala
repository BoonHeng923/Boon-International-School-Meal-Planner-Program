package SchoolMealPlanner.view

import javafx.fxml.FXML
import javafx.stage.Stage
import javafx.scene.control.{Button, ComboBox, Control, ListView, SelectionMode, TextArea, TextField}
import javafx.scene.image.{Image, ImageView}
import javafx.stage.FileChooser
import scalafx.scene.control.Alert
import scalafx.scene.control.Alert.AlertType
import SchoolMealPlanner.model.{Ingredient, Meal}
import javafx.scene.input.{KeyCode, KeyEvent}
import java.io.File
import java.nio.file.{Files, StandardCopyOption}
import scala.jdk.CollectionConverters.*
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import javafx.application.Platform
import javafx.event.ActionEvent

class MealEditDialogController:

  @FXML private var nameField: TextField = _
  @FXML private var imagePreview: ImageView = _
  @FXML private var chooseImageButton: Button = _
  @FXML private var imagePathField: TextField = _
  @FXML private var descriptionField: TextArea = _
  @FXML private var ingredientListView: ListView[String] = _
  @FXML private var weekBox: ComboBox[String] = _
  @FXML private var dayBox: ComboBox[String] = _
  @FXML private var okButton: Button = _
  @FXML private var cancelButton: Button = _

  private var ingredientIndex: Map[String, String] = Map.empty           
  private var nameToIngredient: Map[String, Ingredient] = Map.empty     

  var dialogStage: Stage = _
  private var _meal: Meal = _
  var okClicked: Boolean = false

  def meal = _meal

  def meal_=(m: Meal): Unit =
    _meal = m
    if _meal.originalName == null || _meal.originalName.trim.isEmpty then
      _meal.originalName = m.name.value

    nameField.setText(m.name.value)
    imagePathField.setText(m.imagePath.value)
    descriptionField.setText(m.description.value)
    weekBox.setValue(m.week.value)
    dayBox.setValue(m.day.value)

    showPreview(Option(m.imagePath.value))

    val isNewMeal =
      Option(m.name.value).forall(_.isEmpty) &&
        Option(m.week.value).forall(_.isEmpty) &&
        Option(m.day.value).forall(_.isEmpty)

    weekBox.setDisable(!isNewMeal)
    dayBox.setDisable(!isNewMeal)

    ingredientListView.getSelectionModel.setSelectionMode(SelectionMode.MULTIPLE)
    ingredientListView.getSelectionModel.clearSelection()

    m.ingredients.foreach(ingredient =>
      ingredientListView.getSelectionModel.select(ingredient.name.value)
    )

  @FXML
  def initialize(): Unit =
    val allIngredients = Ingredient.getAllIngredients
    val ingredientNames = allIngredients.map(_.name.value)

    ingredientIndex = ingredientNames.map(n => n.toLowerCase -> n).toMap
    nameToIngredient = allIngredients.map(i => i.name.value -> i).toMap

    ingredientListView.getItems.addAll(ingredientNames: _*)

    weekBox.getItems.addAll("July Week 1", "July Week 2", "July Week 3", "July Week 4", "July Week 5")
    dayBox.getItems.addAll("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
    weekBox.setDisable(false)
    dayBox.setDisable(false)

  @FXML
  def handleChooseImage(): Unit =
    val fileChooser = new FileChooser
    fileChooser.setTitle("Select Meal Image")
    fileChooser.getExtensionFilters.add(
      new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
    )

    val selectedFile: File = fileChooser.showOpenDialog(null)
    if selectedFile != null then
      val fileName = selectedFile.getName
      val destinationDir = File("src/main/resources/images")
      if !destinationDir.exists() then destinationDir.mkdirs()
      val destinationFile = File(destinationDir, fileName)

      try
        Files.copy(
          selectedFile.toPath,
          destinationFile.toPath,
          StandardCopyOption.REPLACE_EXISTING
        )
        println(s"Copied image to: ${destinationFile.getAbsolutePath}")
      catch case e: Exception =>
        println(s"Failed to copy image: ${e.getMessage}")

      val image = Image(destinationFile.toURI.toString)
      imagePreview.setImage(image)
      imagePathField.setText(fileName)

  private def showPreview(pathOpt: Option[String]): Unit =
    val imageFileOpt = pathOpt
      .filter(p => p != null && p.trim.nonEmpty)
      .map(p => new File("src/main/resources/images", p))
      .filter(_.exists())

    imageFileOpt match
      case Some(f) =>
        imagePreview.setImage(Image(f.toURI.toString, true))
      case None =>
        val ph = getClass.getResource("/SchoolMealPlanner/images/placeholder.png")
        if ph != null then
          imagePreview.setImage(new Image(ph.toExternalForm))
        else
          imagePreview.setImage(null)

  @FXML
  def handleOk(evt: ActionEvent): Unit =
    if !isInputValid() then return

    val rawName = nameField.getText.trim
    val rawDesc = descriptionField.getText.trim
    val formattedName = if rawName.nonEmpty then rawName.head.toUpper + rawName.tail else rawName
    val formattedDesc = if rawDesc.nonEmpty then rawDesc.head.toUpper + rawDesc.tail else rawDesc

    if !weekBox.isDisabled then
      val week = weekBox.getValue
      val day = dayBox.getValue
      if Meal.existsByWeekDay(week, day) then
        val alert = new Alert(AlertType.Error)
        alert.setTitle("Duplicate Meal")
        alert.setHeaderText(s"A meal for $week $day already exists.")
        alert.setContentText("Please choose a different slot.")
        alert.showAndWait()
        return
      meal.week.value = week
      meal.day.value = day

    meal.name.value = formattedName
    meal.imagePath.value = imagePathField.getText.trim
    meal.description.value = formattedDesc

    val selectedNames = ingredientListView.getSelectionModel.getSelectedItems.asScala.toList
    val normalizedNames = selectedNames.map(_.trim.toLowerCase).flatMap(ingredientIndex.get).distinct
    val selectedIngredients = normalizedNames.flatMap(nameToIngredient.get)

    meal.ingredients.clear()
    meal.ingredients ++= selectedIngredients

    val srcBtn = evt.getSource.asInstanceOf[Button]
    srcBtn.setDisable(true)

    Future {
      meal.save().get
    }.map { _ =>
      Platform.runLater(() =>
        okClicked = true
        srcBtn.setDisable(false)
        dialogStage.close()
      )
    }.recover { case e =>
      Platform.runLater(() =>
        srcBtn.setDisable(false)
        val alert = new Alert(AlertType.Error)
        alert.setTitle("Save failed")
        alert.setHeaderText("Could not save meal")
        alert.setContentText(Option(e.getMessage).getOrElse(e.toString))
        alert.showAndWait()
      )
    }

  @FXML
  def handleOk(): Unit =
    Option(okButton).foreach(_.setDisable(true))
    handleOk(new ActionEvent(Option(okButton).orNull, Option(okButton).orNull))

  def isInputValid(): Boolean =
    var errorMessage = ""

    def isEmpty(text: String): Boolean = text == null || text.trim.isEmpty

    if isEmpty(nameField.getText) then errorMessage += "Meal name cannot be empty!\n"
    if isEmpty(imagePathField.getText) then errorMessage += "Please choose an image!\n"
    if isEmpty(descriptionField.getText) then errorMessage += "Description cannot be empty!\n"
    if ingredientListView.getSelectionModel.getSelectedItems.isEmpty then
      errorMessage += "At least one ingredient must be selected!\n"
    if isEmpty(weekBox.getValue) then errorMessage += "Week must be selected!\n"
    if isEmpty(dayBox.getValue) then errorMessage += "Day must be selected!\n"

    if errorMessage.nonEmpty then
      val alert = new Alert(AlertType.Error)
      alert.setTitle("Invalid Input")
      alert.setHeaderText("Please correct the following:")
      alert.setContentText(errorMessage)
      alert.showAndWait()
      false
    else true

  @FXML
  def handleCancel(): Unit =
    dialogStage.close()

  @FXML
  def handleKeyNavigation(event: KeyEvent): Unit =
    val navigation = Map[Control, (Option[Control], Option[Control], Option[Control], Option[Control])](
      nameField -> (None, Some(imagePathField), None, None),
      imagePathField -> (Some(nameField), Some(descriptionField), None, None),
      descriptionField -> (Some(imagePathField), Some(ingredientListView), None, None),
      ingredientListView -> (Some(descriptionField), Some(weekBox), None, None),
      weekBox -> (Some(ingredientListView), Some(dayBox), None, None),
      dayBox -> (Some(weekBox), Some(okButton), None, None),
      okButton -> (Some(dayBox), None, None, Some(cancelButton)),
      cancelButton -> (Some(dayBox), None, Some(okButton), None)
    )

    val control = event.getSource.asInstanceOf[Control]

    navigation.get(control).foreach {
      case (up, down, left, right) =>
        event.getCode match
          case KeyCode.UP => up.foreach(_.requestFocus())
          case KeyCode.DOWN => down.foreach(_.requestFocus())
          case KeyCode.LEFT => left.foreach(_.requestFocus())
          case KeyCode.RIGHT => right.foreach(_.requestFocus())
          case KeyCode.ENTER =>
            if control == okButton then handleOk()
            else if control == cancelButton then handleCancel()
            else down.orElse(right).foreach(_.requestFocus())
          case KeyCode.ESCAPE => handleCancel()
          case _ =>
    }