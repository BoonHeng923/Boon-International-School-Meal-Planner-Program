package SchoolMealPlanner.view

import SchoolMealPlanner.MainApp
import javafx.fxml.FXML
import javafx.scene.control.{Button, Label}
import javafx.scene.image.{Image, ImageView}
import SchoolMealPlanner.model.{Meal, Role}

class MealDetailDialogController:

  var dialogStage: scalafx.stage.Stage = _
  private var okClicked = false
  private var meal: Meal = _
  @FXML private var editButton: Button = _
  @FXML private var mealNameLabel: Label = _
  @FXML private var mealImage: ImageView = _
  @FXML private var descriptionLabel: Label = _
  @FXML private var caloriesLabel: Label = _
  @FXML private var ingredientsLabel: Label = _

  def setMeal(meal: Meal): Unit =
    this.meal = meal
    mealNameLabel.setText(meal.name.value)
    descriptionLabel.setText(meal.description.value)
    caloriesLabel.setText(f"Calories: ${meal.calories}%.1f kcal")
    ingredientsLabel.setText("Ingredients: " + meal.ingredients.map(_.name.value).mkString(", "))

    val imagePath = meal.imagePath.value
    val url = getClass.getResource("/images/" + imagePath)

    if url != null then
      val image = new Image(url.toExternalForm)
      mealImage.setImage(image)
      println("Image not found: " + imagePath)
    
    MainApp.currentUser match
      case Some(user) =>
        if user.role == Role.CafeteriaStaff then
          editButton.setVisible(false)
      case None =>
        editButton.setVisible(false)

  def isOkClicked: Boolean = okClicked

  @FXML
  def handleOk(): Unit = 
    okClicked = true
    if dialogStage != null then
      dialogStage.close()

  @FXML
  def handleEditMeal(): Unit =
    val okClicked = SchoolMealPlanner.MainApp.showMealEditDialog(meal)
    if okClicked then
      setMeal(meal)

      val alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION)
      alert.setTitle("Meal Updated")
      alert.setHeaderText(null)
      alert.setContentText("Meal details have been updated.")
      alert.showAndWait()