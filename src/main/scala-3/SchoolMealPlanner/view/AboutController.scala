package SchoolMealPlanner.view

import javafx.event.ActionEvent
import javafx.fxml.FXML
import scalafx.stage.Stage

class AboutController:
  var stage: Option[Stage] = None
  var okClicked = false

  @FXML
  def handleClose(action: ActionEvent): Unit =
    okClicked = true
    stage.foreach(_.close())