package SchoolMealPlanner.view

import javafx.fxml.FXML
import javafx.scene.input.KeyCode
import javafx.scene.layout.AnchorPane
import scalafx.stage.Stage

class AnnouncementDialogController:

  var stage: Option[Stage] = None

  @FXML private var root: AnchorPane = _

  @FXML
  def initialize(): Unit =
    root.setOnMouseClicked(_ => stage.foreach(_.close()))

    root.sceneProperty().addListener((_, _, scene) => 
      if scene != null then
        scene.setOnKeyPressed(e => 
          if e.getCode == KeyCode.ESCAPE then
            stage.foreach(_.close())
        )
    )