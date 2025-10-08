package SchoolMealPlanner.view

import javafx.fxml.FXML
import javafx.scene.control.{Alert, ChoiceBox, Control, PasswordField, TextField}
import javafx.event.ActionEvent
import SchoolMealPlanner.MainApp
import SchoolMealPlanner.model.User
import javafx.scene.input.{KeyCode, KeyEvent}

class RegisterController:

  @FXML private var nameField: TextField = _
  @FXML private var emailField: TextField = _
  @FXML private var passwordField: PasswordField = _
  @FXML private var confirmPasswordField: PasswordField = _
  @FXML private var userTypeChoiceBox: ChoiceBox[String] = _

  @FXML
  def initialize(): Unit =
    userTypeChoiceBox.getItems.addAll("Cafeteria Staff", "Nutritionist")

  @FXML
  def handleRegister(): Unit =
    val name = nameField.getText.trim
    val email = emailField.getText.trim.toLowerCase
    val password = passwordField.getText
    val confirmPassword = confirmPasswordField.getText
    val userType = userTypeChoiceBox.getValue

    if !isEmailValid(email) || !isPasswordValid(password) || password != confirmPassword || userType == null || userType.trim.isEmpty || name.isEmpty then
      val alert = new Alert(Alert.AlertType.ERROR)
      alert.setTitle("Registration Failed")
      alert.setHeaderText("Invalid Input")
      alert.setContentText(buildErrorMessage(name, email, password, confirmPassword, userType))
      alert.showAndWait()
    else
      User.findByEmail(email) match
        case Some(_) =>
          val alert = new Alert(Alert.AlertType.ERROR)
          alert.setTitle("Registration Failed")
          alert.setHeaderText("User Already Exists")
          alert.setContentText("This email is already registered! Please use a different email.")
          alert.showAndWait()
        case None =>
          val newUser = User(name, email, password, userType)
          newUser.save()

          val alert = Alert(Alert.AlertType.INFORMATION)
          alert.setTitle("Registration Successful")
          alert.setHeaderText("Account Created")
          alert.setContentText("Congratulations! You have successfully registered. Please log in.")
          alert.showAndWait()
      
          MainApp.switchScene("/SchoolMealPlanner/view/Login.fxml")

  private def isEmailValid(email: String): Boolean =
    email != null && email.matches("^[A-Za-z0-9._%+-]+@gmail\\.com$")

  private def isPasswordValid(password: String): Boolean =
    password != null && password.length >= 8

  private def buildErrorMessage(name: String, email: String, password: String, confirmPassword: String, userType: String): String = 
    val errors = Seq(
      if name == null || name.isBlank then "Name must be entered!" else "",
      if !isEmailValid(email) then "Email must be in the format xxx@gmail.com!" else "",
      if userType == null || userType.isBlank then "User type must be selected!" else "",
      if !isPasswordValid(password) then "Password must be at least 8 characters!" else "",
      if password != confirmPassword then "Passwords do not match!" else ""
    ).filter(_.nonEmpty)

    errors.mkString("\n")

  @FXML
  def handleCancel(event: ActionEvent): Unit = 
    MainApp.switchScene("/SchoolMealPlanner/view/Login.fxml")

  @FXML
  def handleKeyNavigation(event: KeyEvent): Unit = 
    val navigation = Map(
      nameField -> (None, Some(emailField)),
      emailField -> (Some(nameField), Some(userTypeChoiceBox)),
      userTypeChoiceBox -> (Some(emailField), Some(passwordField)),
      passwordField -> (Some(userTypeChoiceBox), Some(confirmPasswordField)),
      confirmPasswordField -> (Some(passwordField), None)
    )

    navigation.get(event.getSource.asInstanceOf[Control]).foreach { case (up, down) =>
      event.getCode match
        case KeyCode.UP => up.foreach(_.requestFocus())
        case KeyCode.DOWN | KeyCode.ENTER =>
          if event.getSource == confirmPasswordField && event.getCode == KeyCode.ENTER then handleRegister()
          else down.foreach(_.requestFocus())
        case _ =>
      }

    if event.getCode == KeyCode.ESCAPE then
      MainApp.switchScene("/SchoolMealPlanner/view/Login.fxml")