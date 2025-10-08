package SchoolMealPlanner.view

import javafx.fxml.FXML
import javafx.scene.control.Label
import javafx.scene.image.{Image, ImageView}
import javafx.stage.FileChooser
import SchoolMealPlanner.MainApp
import javafx.scene.layout.VBox
import java.io.File
import java.nio.file.{Files, StandardCopyOption}

class UserProfileController:

  @FXML private var avatarPreview: ImageView = _
  @FXML private var nameLabel: Label = _
  @FXML private var emailLabel: Label = _
  @FXML private var roleLabel: Label = _
  @FXML private var backBox: VBox = _

  private val avatarDir = File("src/main/resources/ProfilePicture")

  @FXML
  def initialize(): Unit =
    MainApp.currentUser.foreach { u =>
      nameLabel.setText(u.name.value)
      emailLabel.setText(u.email.value)
      roleLabel.setText(u.role.label)
      loadAvatar(u.avatarPath.value)
    }

  @FXML
  def handleChangePhoto(): Unit =
    val chooser = FileChooser()
    chooser.setTitle("Select Profile Photo")
    chooser.getExtensionFilters.add(
      FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
    )
    val chosen: File = chooser.showOpenDialog(null)
    if chosen == null then return

    if !avatarDir.exists() then avatarDir.mkdirs()

    val fileName = chosen.getName
    val dest = File(avatarDir, fileName)

    try
      Files.copy(chosen.toPath, dest.toPath, StandardCopyOption.REPLACE_EXISTING)
      println(s"Copied avatar to: ${dest.getAbsolutePath}")
    catch case e: Exception =>
        println(s"Failed to copy avatar: ${e.getMessage}")
        return

    avatarPreview.setImage(Image(dest.toURI.toString, true))

    MainApp.currentUser.foreach { u =>
      u.avatarPath.value = fileName
      u.save()
    }

  @FXML
  def handleRemovePhoto(): Unit =
    MainApp.currentUser.foreach { u =>
      u.avatarPath.value = null
      u.save()
    }

    val placeholderStream = getClass.getResourceAsStream("/images/UserPlaceholder.png")
    if placeholderStream != null then
      avatarPreview.setImage(Image(placeholderStream))
    else
      println("Image not found.")

  private def loadAvatar(fileNameOrNull: String | Null): Unit =
    val img =
      if fileNameOrNull == null || fileNameOrNull.trim.isEmpty then
        Image(getClass.getResourceAsStream("/images/UserPlaceholder.png"))
      else
        Image(File(avatarDir, fileNameOrNull).toURI.toString, true)

    avatarPreview.setImage(img)

  @FXML
  def handleBack(): Unit =
    val resource = getClass.getResource("/SchoolMealPlanner/view/HomePage.fxml")
    val loader = new javafx.fxml.FXMLLoader(resource)
    val homePage = loader.load[javafx.scene.Parent]()
    val root = backBox.getScene.getRoot match
      case bp: javafx.scene.layout.BorderPane => bp
      case other => throw new IllegalStateException("Expected BorderPane as root layout")

    root.setCenter(homePage)