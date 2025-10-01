package edu.scu.xzw;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class BusinessSearchController {
    @FXML //  fx:id="categoriesPane"
    private ScrollPane categoriesPane;

    @FXML //  fx:id="subcategoriesPane"
    private ScrollPane subcategoriesPane;

    @FXML //  fx:id="attributesPane"
    private ScrollPane attributesPane;

    @FXML
    private ChoiceBox<String> categoriesSearchChoice;

    @FXML
    private ChoiceBox<String> subcategoriesSearchChoice;

    @FXML
    private ChoiceBox<String> attributesSearchChoice;

    @FXML
    private TableView resultTable;

    @FXML
    private DatePicker reviewFrom;

    @FXML
    private DatePicker reviewTo;

    @FXML
    private ChoiceBox<String> reviewStarsChoice;

    @FXML
    private ChoiceBox<String> votesChoice;

    @FXML
    private TextField votesValue;

    @FXML
    private Slider reviewStars;

    @FXML
    private TextArea querySql;

    private Set<String> categories = new HashSet<>();

    private Set<String> subcategories = new HashSet<>();

    private Set<String> attributes = new HashSet<>();

    @FXML
    private void searchBusiness() throws SQLException {
        resultTable.getItems().clear();
        boolean andCategories = categoriesSearchChoice.getValue().contains("AND");
        boolean andSubCategories = subcategoriesSearchChoice.getValue().contains("AND");
        boolean andAttributes = attributesSearchChoice.getValue().contains("AND");
        LocalDate from = reviewFrom.getValue();
        
        LocalDate to = reviewTo.getValue();
        int stars = (int)reviewStars.getValue();
        Review.Criteria criteria = new Review.Criteria(Optional.ofNullable(from), Optional.ofNullable(to), Optional.ofNullable(reviewStarsChoice.getValue()), Optional.ofNullable(votesChoice.getValue()), stars, Integer.valueOf(votesValue.getText()));
        String qSql = BusinessProcessor.getInstance(ProcessorFactory.getConnection()).getQuerySql(categories, andCategories, subcategories, andSubCategories, attributes, andAttributes, criteria);
        querySql.setText(qSql);
        List<Business> businesses = 
            BusinessProcessor.getInstance(ProcessorFactory.getConnection()).fetch(
                categories, 
                andCategories, 
                subcategories, 
                andSubCategories, 
                attributes, 
                andAttributes,
                criteria
            );
        for (Business business : businesses) {
            resultTable.getItems().add(business);
        }
    }

    private void viewBusiness(Business business) throws IOException, SQLException {
        //Business business = (Business) resultTable.getSelectionModel().getSelectedItem();

        FXMLLoader loader = CommonUtil.loadFile("BusinessDetail.fxml", "Business Detail");

        BusinessDetailController businessDetailController = (BusinessDetailController) loader.getController();

        businessDetailController.populate(business);
    }

    private void setupResultTable() {
        List<String> businessProperies = Business.getProperties();
        for (String p : businessProperies) {
            TableColumn<Business, Object> tableColumn = new TableColumn<>(p);
            tableColumn.setCellValueFactory(new PropertyValueFactory<Business, Object>(p));
            resultTable.getColumns().add(tableColumn);
        }
        resultTable.setRowFactory( tv -> {
            TableRow<Business> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
                    Business business = row.getItem();
                    try {
                        viewBusiness(business);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            return row ;
        });
    }

    public void setup() {
        setupResultTable();

        VBox r = new VBox();
        Category.MAIN_CATEGORIES.forEach(c -> {
            
            CheckBox checkBox = new CheckBox(c);
            checkBox.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) -> {
                if (newVal) {
                    categories.add(c);
                } else {
                    categories.remove(c);
                }
                if (categories.isEmpty()) {
                    subcategories.clear();
                    subcategoriesPane.setContent(null);
                    return;
                }
                
                try {
                    boolean andCategories = categoriesSearchChoice.getValue().contains("AND");
                    List<Category> results = CategoryProcessor.getInstance(ProcessorFactory.getConnection()).fetch(categories, andCategories);
                    results.sort((a, b) -> a.getSubCategory().compareTo(b.getSubCategory()));
                    subcategories.clear();
                    attributesPane.setContent(null);
                    VBox vb = new VBox();
                    results.forEach(result -> {
                        CheckBox cb = new CheckBox(result.getSubCategory());
                        cb.selectedProperty().addListener((ObservableValue<? extends Boolean> sov, Boolean sOldVal, Boolean sNewVal) -> {
                            if (sNewVal) {
                                subcategories.add(result.getSubCategory());
                            } else {
                                subcategories.remove(result.getSubCategory());
                            }
                            if (subcategories.isEmpty()) {
                                attributesPane.setContent(null);
                                return;
                            }
                            
                            try {
                                boolean aCategories = categoriesSearchChoice.getValue().contains("AND");
                                boolean andSubCategories = subcategoriesSearchChoice.getValue().contains("AND");
                                List<Attribute> attrs = AttributeProcessor.getInstance(ProcessorFactory.getConnection()).fetch(categories, aCategories, subcategories, andSubCategories);
                                
                                VBox avb = new VBox();
                                attrs.forEach(attr -> {
                                    CheckBox acb = new CheckBox(attr.getAttributeName());
                                    acb.selectedProperty().addListener((ObservableValue<? extends Boolean> aov, Boolean aOldVal, Boolean aNewVal) -> {
                                        if (aNewVal) {
                                            attributes.add(attr.getAttributeName());
                                        } else {
                                            attributes.remove(attr.getAttributeName());
                                        }
                                     });
                                    acb.setIndeterminate(false);
                                    avb.getChildren().add(acb);
                                });
                                attributesPane.setContent(avb);
            
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                         });
                        cb.setIndeterminate(false);
                        vb.getChildren().add(cb);
                    });
                    subcategoriesPane.setContent(vb);

                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
             });
            r.getChildren().add(checkBox);
            checkBox.setIndeterminate(false);
        });
        categoriesPane.setContent(r);
    }
}
