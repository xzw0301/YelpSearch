# 🔎 Yelp Faceted Search Application (Java/JavaFX/JDBC)

## Project Overview

This is a data analysis application designed to perform **complex, faceted searches** on a simplified subset of the Yelp Academic Dataset (20,544 businesses, reviews, and users).

The primary focus of this project was to implement an efficient database infrastructure (using **Oracle SQL**) and a dynamic search interface (**JavaFX**) to allow users to filter businesses based on multiple, intersecting criteria (e.g., Categories **AND** Attributes **AND** Review scores).

## 🚀 Key Features

* **Faceted Search:** Query businesses using simultaneous constraints across four independent facets:
  1.  Main Categories (e.g., Restaurants)
  2.  Subcategories (e.g., Mexican)
  3.  Business Attributes (e.g., 'GoodForKids-True')
  4.  Review Criteria (e.g., Stars $\ge 4$, written within a certain date range)
* **Dynamic UI Filtering:** Selection of Main Categories dynamically updates the available Subcategories and Attributes, reducing search space.
* **Complex SQL Logic:** Implements **AND/OR** logic for criteria selection *within* a facet, combined with an implicit **AND** logic *across* all facets using **SQL `INTERSECT`**.
* **Data Normalization:** Converts the nested, array-based Yelp JSON data into a clean relational schema for efficient querying.

## 🛠️ Technology Stack

| Component | Technology | Role |
| :--- | :--- | :--- |
| **Language** | Java 11+ | Core application logic. |
| **UI Framework** | JavaFX / FXML | Desktop graphical interface and MVC presentation layer. |
| **Database** | Oracle SQL (via JDBC) | RDBMS for data persistence and complex query execution. |
| **Build/Dependencies**| Apache Maven | Project management, dependency resolution, and build lifecycle. |
| **Data Processing** | Jackson & `JsonFlattener` | Used for **ETL** (Extract, Transform, Load) to parse complex JSON objects and flatten nested attributes for normalization. |
| **Boilerplate Reduction**| Lombok (`@Data`, `@Builder`) | Used to auto-generate getters, setters, constructors, and builder methods for data model classes. |

## 🏗️ Architecture and Design Highlights

### 1\. Three-Tier Architecture

The project maintains a clear separation of concerns:

* **Presentation Layer:** JavaFX Controllers (`BusinessSearchController.java`) handle user input and display results.
* **Business Logic Layer:** The **Processor** classes (`BusinessProcessor.java`, `CategoryProcessor.java`) contain the complex logic for query building and data transformation.
* **Data Layer:** The Oracle Database, accessed via **JDBC** connections, handles storage and retrieval.

### 2\. Data Normalization Strategy

The raw Yelp JSON (e.g., Categories as a list, Attributes as nested objects) was normalized into a relational schema to support efficient searching:

* The `categories` array was broken down and stored in the **`Category`** table, one row per `BusinessID` and `MainCategory`/`SubCategory` pair.
* The nested `attributes` JSON was **flattened** using `JsonFlattener` and stored in the **`Attribute`** table as a simple `AttributeName` key-value string (e.g., `'AcceptsCreditCards-True'`).

### 3\. The Complex Search Query

The core logic lies in `BusinessProcessor.java`'s `buildSqlWithPlaceHolder()` method. It enforces an implicit **AND** across different facets (Category, Attribute, Review) by combining subqueries using the **SQL `INTERSECT`** operator:

$$\text{Final Business IDs} = (\text{IDs matching Categories}) \cap (\text{IDs matching Attributes}) \cap (\text{IDs matching Reviews})$$

This was critical for ensuring a business met *all* criteria selected by the user.

## ⚙️ Setup and Execution

### Prerequisites
* You need to [install maven](https://maven.apache.org/install.html)
* You need to [download oracle jdbc driver](https://www.oracle.com/database/technologies/appdev/jdbc-downloads.html)
* You need install oracle jdbc drive into maven local repository. E.g
   ```
   mvn install:install-file -Dfile=path/to/your/ojdbc8.jar -DgroupId=com.oracle 
	-DartifactId=ojdbc8 -Dversion=8 -Dpackaging=jar
   ```
* Yelp review file is 739M and exceed the default table space size. You need to change the table space side to more than that. Eg:
   ```
   ALTER TABLESPACE system
   ADD DATAFILE 'data03.dbf' SIZE 1000M
    ``` 
  
1.  **Java JDK 11+**
2.  **Apache Maven**
3.  **Oracle JDBC Driver:** The project requires the Oracle JDBC driver (`ojdbc8.jar`), which must be manually installed into your local Maven repository:
    ```bash
    mvn install:install-file -Dfile=/path/to/ojdbc8.jar -DgroupId=com.oracle -DartifactId=ojdbc8 -Dversion=8 -Dpackaging=jar
    ```

### Database Setup (Oracle)

1.  **Configure Database:** Ensure your Oracle instance is running and you have necessary permissions. As the Yelp data file is large (739MB), the tablespace size may need adjustment:
    ```sql
    ALTER TABLESPACE system ADD DATAFILE 'data03.dbf' SIZE 1000M;
    ```
2.  **Create Schema:** Execute the provided SQL scripts:
    ```sql
    # To drop existing tables
    sqlplus user/password @dropdb.sql
    # To create tables and indexes
    sqlplus user/password @createdb.sql
    ```

### Running the Application

1.  **Populate Database (ETL Process):** This step reads the JSON files, transforms the data, and loads it into the Oracle tables using JDBC batch operations.
    ```bash
    mvn exec:java
    ```
    *The entry point for this process is typically `Populate.java`.*
2.  **Launch UI:** Start the JavaFX application.
    ```bash
    mvn javafx:run
    ```
    *The entry point for the UI is `App.java`.*


