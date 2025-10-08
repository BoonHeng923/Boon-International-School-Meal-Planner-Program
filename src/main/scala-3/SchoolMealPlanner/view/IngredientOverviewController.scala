package SchoolMealPlanner.view

import SchoolMealPlanner.model.{Ingredient, Role}
import SchoolMealPlanner.MainApp
import javafx.fxml.FXML
import javafx.scene.control.{Alert, Button, Label, TableColumn, TableView, TextField}
import javafx.scene.layout.VBox
import scalafx.Includes.*
import scalafx.collections.ObservableBuffer

class IngredientOverviewController:
  @FXML private var ingredientTable: TableView[Ingredient] = _
  @FXML private var nameColumn: TableColumn[Ingredient, String] = _
  @FXML private var calorieColumn: TableColumn[Ingredient, Number] = _
  @FXML private var nameLabel: Label = _
  @FXML private var calorieLabel: Label = _
  @FXML private var proteinLabel: Label = _
  @FXML private var fatLabel: Label = _
  @FXML private var carbohydrateLabel: Label = _
  @FXML private var vitaminLabel: Label = _
  @FXML private var searchField: TextField = _
  @FXML private var addButton: Button = _
  @FXML private var deleteButton: Button = _
  @FXML private var editButton: Button = _
  @FXML private var sortButton: Button = _
  @FXML private var backBox: VBox = _

  @FXML
  def initialize(): Unit =
    ingredientTable.items = MainApp.ingredientData
    nameColumn.setCellValueFactory(cellData => cellData.getValue.name)
    calorieColumn.setCellValueFactory(cellData => cellData.getValue.calorie)
    showIngredientDetails(None)
    
    ingredientTable.getSelectionModel.selectedItemProperty.addListener(
      (_, _, newValue) => showIngredientDetails(Option(newValue))
    )

    MainApp.currentUser match
      case Some(user) =>
        if user.role == Role.CafeteriaStaff then
          addButton.setVisible(false)
          editButton.setVisible(false)
          deleteButton.setVisible(false)
      case None =>
        addButton.setVisible(false)
        editButton.setVisible(false)
        deleteButton.setVisible(false)
  
  private def showIngredientDetails(ingredient: Option[Ingredient]): Unit =
    ingredient match
      case Some(i) =>
        nameLabel.setText(i.name.value)
        calorieLabel.setText("%.1f".format(i.calorie.value))
        proteinLabel.setText("%.1f".format(i.protein.value))
        fatLabel.setText("%.1f".format(i.fat.value))
        carbohydrateLabel.setText("%.1f".format(i.carbohydrate.value))
        vitaminLabel.setText(i.vitamin.value)
      case None =>
        nameLabel.setText("")
        calorieLabel.setText("")
        proteinLabel.setText("")
        fatLabel.setText("")
        carbohydrateLabel.setText("")
        vitaminLabel.setText("")

  @FXML
  def handleNewIngredient(): Unit =
    val ingredient = new Ingredient("", 0.0, 0.0, 0.0, 0.0, "")
    val okClicked = MainApp.showIngredientEditDialog(ingredient, "Add Ingredient")
    if okClicked then
      ingredient.save()
      MainApp.ingredientData.insert(0, ingredient)
      ingredientTable.items = MainApp.ingredientData
      ingredientTable.getSelectionModel.select(0)

  @FXML
  def handleDeleteIngredient(): Unit =
    val selected = ingredientTable.getSelectionModel.getSelectedItem

    if selected == null then
      val alert = new Alert(Alert.AlertType.ERROR)
      alert.setTitle("No Selection")
      alert.setHeaderText("No Ingredient Selected")
      alert.setContentText("Please select an ingredient in the table.")
      alert.showAndWait()
      return

    val confirm = new Alert(Alert.AlertType.CONFIRMATION)
    confirm.setTitle("Delete Ingredient")
    confirm.setHeaderText("Are you sure want to delete this ingredient?")
    confirm.setContentText(s"Name: ${selected.name.value}")

    val result = confirm.showAndWait()
    if result.isPresent && result.get == javafx.scene.control.ButtonType.OK then
      selected.delete()
      MainApp.ingredientData -= selected
      ingredientTable.items = MainApp.ingredientData

  @FXML
  def handleEditIngredient(): Unit =
    val selected = ingredientTable.getSelectionModel.getSelectedItem
    if selected != null then
      val okClicked = MainApp.showIngredientEditDialog(selected)
      if okClicked then 
        selected.save()
        ingredientTable.refresh()
      showIngredientDetails(Some(selected))
    else
      val alert = new Alert(Alert.AlertType.ERROR)
      alert.setTitle("No Selection")
      alert.setHeaderText("No Ingredient Selected")
      alert.setContentText("Please select an ingredient in the table.")
      alert.showAndWait()

  @FXML
  def handleSort(): Unit =
    MainApp.showIngredientsSortDialog() match
      case Some("Name A-Z") =>
        ingredientTable.items = ObservableBuffer.from(MainApp.ingredientData.sortBy(_.name.value.toLowerCase))
      case Some("Name Z-A") =>
        ingredientTable.items = ObservableBuffer.from(MainApp.ingredientData.sortBy(_.name.value.toLowerCase).reverse)
      case Some("Calories ↑") =>
        ingredientTable.items = ObservableBuffer.from(MainApp.ingredientData.sortBy(_.calorie.value))
      case Some("Calories ↓") =>
        ingredientTable.items = ObservableBuffer.from(MainApp.ingredientData.sortBy(_.calorie.value).reverse)
      case _ => 

  @FXML
  def handleSearch(): Unit =
    val keyword = searchField.getText.trim.toLowerCase
    if keyword.nonEmpty then
      MainApp.ingredientData.find(_.name.value.toLowerCase.contains(keyword)) match
        case Some(ingredient) =>
          showIngredientDetails(Some(ingredient))
        case None =>
          val alert = new Alert(Alert.AlertType.ERROR)
          alert.setTitle("Ingredient Not Found")
          alert.setHeaderText("No match found")
          alert.setContentText(s"No ingredient found matching \"$keyword\".")
          alert.showAndWait()
          showIngredientDetails(None)
    else 
      ingredientTable.getSelectionModel.clearSelection()
      showIngredientDetails(None)

  @FXML
  def handleBack(): Unit =
    val resource = getClass.getResource("/SchoolMealPlanner/view/HomePage.fxml")
    val loader = new javafx.fxml.FXMLLoader(resource)
    val homePage = loader.load[javafx.scene.Parent]()
    val root = backBox.getScene.getRoot match
      case bp: javafx.scene.layout.BorderPane => bp
      case other => throw new IllegalStateException("Expected BorderPane as root layout")

    root.setCenter(homePage)