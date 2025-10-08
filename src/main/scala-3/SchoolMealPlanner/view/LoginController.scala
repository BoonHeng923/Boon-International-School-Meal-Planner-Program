package SchoolMealPlanner.view

import javafx.fxml.FXML
import javafx.scene.control.{Alert, PasswordField, TextField}
import SchoolMealPlanner.MainApp
import SchoolMealPlanner.model.User
import javafx.animation.{FadeTransition, ParallelTransition, TranslateTransition}
import javafx.event.ActionEvent
import javafx.scene.input.{KeyCode, KeyEvent}
import javafx.scene.layout.VBox
import javafx.scene.media.{Media, MediaPlayer, MediaView}
import javafx.scene.text.Text
import javafx.util.Duration
import javafx.animation.{KeyFrame, Timeline}

class LoginController:

  @FXML private var emailField: TextField = _
  @FXML private var passwordField: PasswordField = _
  @FXML private var bgVideo: MediaView = _
  @FXML private var leftBox: VBox = _
  @FXML private var rightBox: VBox = _
  @FXML private var typingText: Text = _
  
  @FXML
  def initialize(): Unit =
    try
      val url = getClass.getResource("/videos/HealthyFood.mp4")
      if url != null then
        val media = new Media(url.toExternalForm)
        val player = new MediaPlayer(media)
        player.setCycleCount(MediaPlayer.INDEFINITE)
        player.setMute(true)
        player.play()
        bgVideo.setMediaPlayer(player)
        bgVideo.setPreserveRatio(false)
        bgVideo.setFitWidth(900) 
        bgVideo.setFitHeight(600)
        bgVideo.setMouseTransparent(true)

        val clip = new javafx.scene.shape.Rectangle()
        clip.setWidth(bgVideo.getFitWidth)
        clip.setHeight(bgVideo.getFitHeight)
        clip.setArcWidth(80)
        clip.setArcHeight(80)
        bgVideo.setClip(clip)
      
      Seq(leftBox, rightBox).foreach: box =>
        box.setOpacity(0)
        box.setTranslateY(20)

        val fade = new FadeTransition(Duration.seconds(1.5), box)
        fade.setFromValue(0)
        fade.setToValue(1)

        val move = new TranslateTransition(Duration.seconds(1.5), box)
        move.setFromY(20)
        move.setToY(0)

        ParallelTransition(fade, move).play()
      
      startTypingLoop()
    catch case e: Exception => e.printStackTrace()

  def startTypingLoop(): Unit =
    val message = "Healthy Meals for Bright Futures"
    val delay = 60
    val timeline = new Timeline()

    for i <- 1 to message.length do
      val partial = message.substring(0, i)
      val keyFrame = new KeyFrame(
        Duration.millis(i * delay),
        (_: ActionEvent) => typingText.setText(partial)
      )
      timeline.getKeyFrames.add(keyFrame)
    
    val pauseAndClear = new KeyFrame(
      Duration.millis(message.length * delay + 1500),
      (_: ActionEvent) => typingText.setText("")
    )
    timeline.getKeyFrames.add(pauseAndClear)

    timeline.setOnFinished(_ =>
      val restart = new Timeline(new KeyFrame(Duration.millis(300), _ => startTypingLoop()))
      restart.play()
    )

    timeline.play()

  @FXML
  def handleLogin(): Unit =
    val email = emailField.getText
    val password = passwordField.getText

    if !isEmailValid(email) || !isPasswordValid(password) then
      val alert = new Alert(Alert.AlertType.ERROR)
      alert.setTitle("Login Failed")
      alert.setHeaderText("Invalid Input")
      alert.setContentText(buildErrorMessage(email, password))
      alert.showAndWait()
    else
      User.findByEmail(email) match
        case Some(_) =>
          User.findByEmailAndPassword(email, password) match
            case Some(user) =>
              val alert = new Alert(Alert.AlertType.INFORMATION)
              alert.setTitle("Login Successful")
              alert.setHeaderText("Welcome!")
              alert.setContentText("You have successfully logged in.")
              alert.showAndWait()
              MainApp.loginSuccessful(user)
            case None =>
              val alert = new Alert(Alert.AlertType.ERROR)
              alert.setTitle("Login Failed")
              alert.setHeaderText("Incorrect Password")
              alert.setContentText("Wrong Password! Please enter again!")
              alert.showAndWait()
        case None =>
          val alert = new Alert(Alert.AlertType.ERROR)
          alert.setTitle("Login Failed")
          alert.setHeaderText("User Not Found")
          alert.setContentText("No matching user found in the system!")
          alert.showAndWait()
  
  private def isEmailValid(email: String): Boolean =
    email != null && email.matches("^[A-Za-z0-9._%+-]+@gmail\\.com$")

  private def isPasswordValid(password: String): Boolean =
    password != null && password.length >= 8

  private def buildErrorMessage(email: String, password: String): String =
    val errors = Seq(
      if !isEmailValid(email) then "Email must be in the format xxx@gmail.com" else "",
      if !isPasswordValid(password) then "Password must be at least 8 characters" else ""
    ).filter(_.nonEmpty)
    errors.mkString("\n")

  @FXML
  def handleSignUp(): Unit =
    MainApp.switchScene("/SchoolMealPlanner/view/Register.fxml")

  @FXML
  def handleEmailKey(event: KeyEvent): Unit =
    if event.getCode == KeyCode.DOWN || event.getCode == KeyCode.ENTER then
      passwordField.requestFocus()

  @FXML
  def handlePasswordKey(event: KeyEvent): Unit =
    event.getCode match
      case KeyCode.ENTER => handleLogin()
      case KeyCode.UP => emailField.requestFocus()
      case _ => ()