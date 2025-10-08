package SchoolMealPlanner.view

import SchoolMealPlanner.MainApp
import SchoolMealPlanner.model.{Meal, Role}
import javafx.fxml.FXML
import javafx.scene.control.{Alert, Button, ButtonType, ComboBox, Label}
import javafx.scene.image.{Image, ImageView}
import javafx.scene.layout.VBox
import scalafx.Includes.*
import org.apache.pdfbox.pdmodel.*
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject

import java.io.File

class MealPlanningOverviewController:

  @FXML private var weekSelector: ComboBox[String] = _
  @FXML private var weekLabel: Label = _
  @FXML private var mondayMeal: Label = _
  @FXML private var mondayImage: ImageView = _
  @FXML private var mondayButton: Button = _
  @FXML private var tuesdayMeal: Label = _
  @FXML private var tuesdayImage: ImageView = _
  @FXML private var tuesdayButton: Button = _
  @FXML private var wednesdayMeal: Label = _
  @FXML private var wednesdayImage: ImageView = _
  @FXML private var wednesdayButton: Button = _
  @FXML private var thursdayMeal: Label = _
  @FXML private var thursdayImage: ImageView = _
  @FXML private var thursdayButton: Button = _
  @FXML private var fridayMeal: Label = _
  @FXML private var fridayImage: ImageView = _
  @FXML private var fridayButton: Button = _
  @FXML private var addButton: Button = _
  @FXML private var deleteButton: Button = _
  @FXML private var downloadPdfButton: Button = _
  @FXML private var backBox: VBox = _

  private var lastClickedDay: Option[String] = None
  private var selectedBox: Option[VBox] = None
  
  @FXML
  def initialize(): Unit = 
    val weeks = Seq("July Week 1", "July Week 2", "July Week 3", "July Week 4", "July Week 5")
    weekSelector.getItems.addAll(weeks: _*)
    weekSelector.setValue(weeks.head)
    updateMealBoxes()
    weekSelector.setOnAction(_ => updateMealBoxes())

    MainApp.currentUser match
          case Some(user) =>
            if user.role == Role.CafeteriaStaff then
              addButton.setVisible(false)
              deleteButton.setVisible(false)
          case None =>
            addButton.setVisible(false)
            deleteButton.setVisible(false)
  
  private def updateMealBoxes(): Unit =
    selectedBox.foreach(_.getStyleClass.remove("selected"))
    selectedBox = None
    val selectedWeek = weekSelector.getValue
    val meals = MainApp.mealData.filter(_.week.value == selectedWeek)

    def update(day: String, label: Label, imgView: ImageView, btn: Button): Unit =
      val mealOpt = meals.find(_.day.value == day)
      label.setText(mealOpt.map(_.name.value).getOrElse("No meal exists"))

      val imgPath = mealOpt.flatMap(m => Option(m.imagePath.value).filter(_.nonEmpty)).getOrElse("Ingredients.png")
      val resource = getClass.getResource(s"/images/$imgPath")
      val image = Image(Option(resource).map(_.toExternalForm).getOrElse(getClass.getResource("/images/Ingredients.png").toExternalForm))
      imgView.setImage(image)

      btn.setVisible(mealOpt.isDefined)

      mealOpt.foreach(m => 
        btn.setOnAction(_ => 
          lastClickedDay = Some(day)
          val okClicked = MainApp.showMealDetailDialog(m)
          if okClicked then
            MainApp.mealData.clear()
            MainApp.mealData ++= Meal.getAllMeals
            updateMealBoxes()
        )
      )
      
      imgView.getParent match
        case vbox: VBox =>
          vbox.setOnMouseClicked(_ => 
            lastClickedDay = Some(day)
            selectedBox.foreach(_.getStyleClass.remove("selected"))
            if !vbox.getStyleClass.contains("selected") then
              vbox.getStyleClass.add("selected")
            selectedBox = Some(vbox)
          )
        case _ => ()

    update("Monday", mondayMeal, mondayImage, mondayButton)
    update("Tuesday", tuesdayMeal, tuesdayImage, tuesdayButton)
    update("Wednesday", wednesdayMeal, wednesdayImage, wednesdayButton)
    update("Thursday", thursdayMeal, thursdayImage, thursdayButton)
    update("Friday", fridayMeal, fridayImage, fridayButton)

  @FXML
  def handleAddMeal(): Unit =
    val newMeal = new Meal()
    val okClicked = MainApp.showMealEditDialog(newMeal, "Add Meal")
    if okClicked then
      MainApp.mealData += newMeal
      updateMealBoxes()

  @FXML
  def handleDeleteMeal(): Unit =
    val selectedWeek = weekSelector.getValue
    lastClickedDay match
      case Some(day) =>
        MainApp.mealData.find(m => m.week.value == selectedWeek && m.day.value == day) match
          case Some(meal) =>
            val alert = new Alert(Alert.AlertType.CONFIRMATION)
            alert.setTitle("Delete Confirmation")
            alert.setHeaderText(s"Are you sure you want to delete '${meal.name.value}'?")
            alert.setContentText("This action cannot be undone.")
            val result = alert.showAndWait()
            if result.isPresent && result.get == ButtonType.OK then
              meal.delete()
              MainApp.mealData -= meal
              updateMealBoxes()
              lastClickedDay = None
          case None =>
            val alert = new Alert(Alert.AlertType.WARNING)
            alert.setTitle("No Meal Found")
            alert.setHeaderText("Meal not found for deletion.")
            alert.showAndWait()
      case None =>
        val alert = new Alert(Alert.AlertType.WARNING)
        alert.setTitle("No Day Selected")
        alert.setHeaderText("Please click a day's meal before deleting.")
        alert.showAndWait()

  @FXML
  def handleDownloadPdf(): Unit = 
    val selectedWeek = weekSelector.getValue
    val meals = MainApp.mealData.filter(_.week.value == selectedWeek)
    val days = Seq("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")

    val document = new PDDocument()
    var page = new PDPage(PDRectangle.A4)
    document.addPage(page)

    var contentStream = new org.apache.pdfbox.pdmodel.PDPageContentStream(document, page)
    val fontRegular = PDType1Font.HELVETICA
    val fontBold = PDType1Font.HELVETICA_BOLD
    val margin = 50f
    val pageTop = PDRectangle.A4.getHeight - margin
    val pageBottom = margin
    var yPos = pageTop

    def newPage(): Unit =
      contentStream.close()
      page = new PDPage(PDRectangle.A4)
      document.addPage(page)
      contentStream = new org.apache.pdfbox.pdmodel.PDPageContentStream(document, page)
      yPos = pageTop

    def writeLine(text: String, leading: Float = 16f, bold: Boolean = false): Unit =
      if (yPos - leading < pageBottom) newPage()
      contentStream.beginText()
      contentStream.setFont(if bold then fontBold else fontRegular, 12)
      contentStream.newLineAtOffset(margin, yPos)
      contentStream.showText(text)
      contentStream.endText()
      yPos -= leading

    writeLine(s"Meal Planning for $selectedWeek", 22f, bold = true)
    yPos -= 6f

    for day <- days do
      val mealOpt = meals.find(_.day.value == day)

      val imgW = 100f
      val imgH = 100f
      val lineH = 16f
      val textLines = if mealOpt.isDefined then 4 else 1
      val totalHeightNeeded = imgH + (textLines * lineH) + 30f

      if yPos - totalHeightNeeded < pageBottom then newPage()

      writeLine(s"$day:", 18f, bold = true)

      mealOpt match
        case Some(meal) =>
          val res = getClass.getResource("/images/" + meal.imagePath.value)
          if res != null then
            val imgFile = new File(res.toURI)
            val pdImg = PDImageXObject.createFromFile(imgFile.getAbsolutePath, document)
            contentStream.drawImage(pdImg, margin, yPos - imgH, imgW, imgH)

          yPos -= (imgH + 16f)
          writeLine(s"Name: ${meal.name.value}")
          writeLine(s"Description: ${meal.description.value}")
          writeLine(f"Calories: ${meal.calories}%.1f kcal")
          writeLine("Ingredients: " + meal.ingredients.mkString(", "))
          yPos -= 22f

        case None =>
          writeLine("No meal exists.")
          yPos -= 12f

    contentStream.close()

    val fileName = s"${selectedWeek.replaceAll(" ", "_")}_Meal_Planning.pdf"
    val outputFile = File(fileName)
    document.save(outputFile)
    document.close()

    val alert = Alert(Alert.AlertType.INFORMATION)
    alert.setTitle("Download Complete")
    alert.setHeaderText(null)
    alert.setContentText(s"PDF downloaded as: $fileName")
    alert.showAndWait()
  
  @FXML
  def handleBack(): Unit =
    val resource = getClass.getResource("/SchoolMealPlanner/view/HomePage.fxml")
    val loader = new javafx.fxml.FXMLLoader(resource)
    val homePage = loader.load[javafx.scene.Parent]()
    val root = backBox.getScene.getRoot match
      case bp: javafx.scene.layout.BorderPane => bp
      case other => throw new IllegalStateException("Expected BorderPane as root layout")
    root.setCenter(homePage)