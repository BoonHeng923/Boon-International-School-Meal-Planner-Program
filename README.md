# Boon-International-School-Meal-Planner-Program

## Overview
The **Boon International School's Meal Planner Program** is a Scala-based dekstop application designed to streamline collaboration between nutitionists and cafeteria staff in school meal planning. It was developed to support United Nations Sustainable Development Goal (SDG) 2 - Zero Hunger, promoting efficient food manangement, balanced nutrition, and sustainable meal planning.

Nutritionists can create meal plans and manage ingredients, while cafeteria staff can provide feedback on meal preparation and student response. The system applied object-oriented programming (OOP) principles including abstraction, inheritance, polymorphism, encapsulation, traits, and companion objects.

## Key Features
### Authentication
- Secure Login and Register system with email and password validation.
- Supports two user roles: Nutritionist and Cafeteria Staff.
### Announcement
- Displays important announcements in an interactive dialog upon login.
### Menu Bar
- Easy navigation between Home, Meal Planning, Ingredients Library, Feedback, and Reports.
- Includes About page and secure Logout functions.
### Home Page
- Personalized greeting and quick access cards to main program features.
### Meal Planning
- Organize weekly meals (Monday to Friday) with meal cards.
- Add, edit, delete, and view meals with automatic calorie calculation based on ingredients.
- Export weekly plans as PDF reports using ApachePDFBox.
- Role restriction: Only nutritionists can modify meal plans.
### Ingredients Library
- Store all ingredients with nutritional details (calories, fat, protein, carbohydrate, and vitamin).
- Supports add, edit, delete, sort and search features.
- Validation ensures only positive numeric values and prevents duplicates.
- Role restirction: Only nutritionists can modify ingredients.
### Feedback
- Cafeteria staff can submit feedback on meals.
- Supports add, edit, delete, sort, and search features.
- Automatically records submitter information from login session.
### Reports
- Generate three types of analytical reports:
  1. **Meal Frequency Report** - shows how often each meal is served.
  2. **Ingredient Usage Report** - analyzes ingredient frequency.
  3. **Feedback Summary Report** - summarizes common feedback types.

## Object-Oriented Design
- **Classes**: User, Nutritionist, CafeteriaStaff, Meal, Ingredient, Feedback
- **Trait**: Database - manages Derby connecion and shared database logic.
- **Aggregation**: A meal aggregates multiple ingredients.
- **Association**: Feedback links User and Meal.
- **Polymorphism**: Role-based access - nutritionists and cafeteria staff
- **Companion Object**: Handled data queries and factory creation for each model.

## Technologies Used
- **Programming Language**: Scala 3
- **UI Framework**: ScalaFX/JavaFX
- **Database**: Apache Derby + ScalikeJDBC
- **PDF Export**: Apache PDFBox
- **Tools**: Scene Builder, Intellij IDEA

## Database
- Use ApacheDerby with ScalikeJDBC for database connectivity.
- Real-time data persistence for all CRUD operations.
- Supports dynamic updates without restarting the application.

## Credit
**Developer:** Ong Boon Heng <br>
**Course:** Bachelor of Science (Honours) in Computer Science <br>
**Institution:** Faculty of Engineering and Technology, Sunway University
