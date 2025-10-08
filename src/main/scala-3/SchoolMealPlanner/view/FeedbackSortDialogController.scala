package SchoolMealPlanner.view

import scalafx.stage.Stage
import javafx.fxml.FXML
import javafx.scene.control.{Button, RadioButton}

class FeedbackSortDialogController:
  var dialogStage: Stage = _
  var selectedOption: Option[String] = None

  @FXML private var nameAZ: RadioButton = _
  @FXML private var nameZA: RadioButton = _
  @FXML private var dateLatest: RadioButton = _
  @FXML private var dateOldest: RadioButton = _
  @FXML private var okButton: Button = _

  @FXML
  def initialize(): Unit = 
    nameAZ.setOnAction(_ => okButton.requestFocus())
    nameZA.setOnAction(_ => okButton.requestFocus())
    dateLatest.setOnAction(_ => okButton.requestFocus())
    dateOldest.setOnAction(_ => okButton.requestFocus())

  @FXML
  def handleOk(): Unit = 
    selectedOption =
      if nameAZ.isSelected then Some("Name A-Z")
      else if nameZA.isSelected then Some("Name Z-A")
      else if dateLatest.isSelected then Some("Date ↑")
      else if dateOldest.isSelected then Some("Date ↓")
      else None

    dialogStage.close()
  
  @FXML
  def handleCancel(): Unit = 
    selectedOption = None
    dialogStage.close()