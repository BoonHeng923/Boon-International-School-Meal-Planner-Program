package SchoolMealPlanner.view

import scalafx.stage.Stage
import javafx.fxml.FXML
import javafx.scene.control.{Button, RadioButton}

class IngredientSortDialogController:
  var dialogStage: Stage = _
  var selectedOption: Option[String] = None

  @FXML private var nameAZ: RadioButton = _
  @FXML private var nameZA: RadioButton = _
  @FXML private var calorieAsc: RadioButton = _
  @FXML private var calorieDesc: RadioButton = _
  @FXML private var okButton: Button = _

  @FXML
  def initialize(): Unit =
    nameAZ.setOnAction(_ => okButton.requestFocus())
    nameZA.setOnAction(_ => okButton.requestFocus())
    calorieAsc.setOnAction(_ => okButton.requestFocus())
    calorieDesc.setOnAction(_ => okButton.requestFocus())

  @FXML
  def handleOk(): Unit =
    selectedOption =
      if nameAZ.isSelected then Some("Name A-Z")
      else if nameZA.isSelected then Some("Name Z-A")
      else if calorieAsc.isSelected then Some("Calories ↑")
      else if calorieDesc.isSelected then Some("Calories ↓")
      else None

    dialogStage.close()

  @FXML
  def handleCancel(): Unit =
    selectedOption = None
    dialogStage.close()