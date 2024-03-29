package com.gendergapanalyser.gendergapanalyser;

import animatefx.animation.*;
import eu.iamgio.animated.transition.AnimatedSwitcher;
import eu.iamgio.animated.transition.AnimatedThemeSwitcher;
import eu.iamgio.animated.transition.Animation;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import javafx.util.Duration;
import org.controlsfx.control.RangeSlider;

import java.io.*;
import java.net.URL;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.Objects;
import java.util.ResourceBundle;

public class DisplayEvolutionGraph implements Initializable {
    @FXML
    private Hyperlink voidLink;
    @FXML
    private AnchorPane titleBar;
    @FXML
    private ChoiceBox<String> languagePicker;
    @FXML
    private ChoiceBox<String> currencyPicker;
    @FXML
    private ImageView lightModeButtonGlyph;
    @FXML
    private ImageView darkModeButtonGlyph;
    @FXML
    private ChoiceBox<String> changeGraph;
    @FXML
    private ImageView chartImageView;
    @FXML
    private VBox salariesView;
    @FXML
    private VBox payGapsView;
    @FXML
    private TextField minimumRangeInput;
    @FXML
    private TextField maximumRangeInput;
    @FXML
    private RangeSlider rangeSlider;
    @FXML
    private ToggleButton showPayGapToggle;
    @FXML
    private ToggleButton includePredictionsToggle;
    @FXML
    private HBox predictionControls;
    @FXML
    private Rectangle darkOverlay;
    @FXML
    private ImageView loadingCircleImageView;
    @FXML
    private AnchorPane backgroundOperations;
    @FXML
    private AnimatedSwitcher darkOverlayAnimator;
    @FXML
    private AnimatedSwitcher promptAnimator;
    private AnimatedThemeSwitcher switchTheme;
    private final String[] choices_EN = {"Men's and women's wages", "Men's wages", "Women's wages", "Gender wage gap"};
    private final String[] choices_FR = {"Salaires des femmes et d'hommes", "Salaires d'hommes", "Salaires des femmes", "Différence de la paye"};
    private final String[] choices_RO = {"Salariile femeilor si bărbaților", "Salariile bărbaților", "Salariile femeilor", "Diferența între salarii"};

    //Function triggered when the user wants to go back to the main menu
    @FXML
    private void backToMainMenu() throws IOException {
        //Creating a new window for the main menu, since the main menu is a different-sized page than the current page and the switching should be smooth and pleasant for the user experience
        Stage mainMenu = new Stage();
        mainMenu.initStyle(StageStyle.UNDECORATED);
        //Setting the new window's title
        mainMenu.setTitle(Main.language.equals("EN") ? "Main Menu" : Main.language.equals("FR") ? "Menu Principal" : "Meniu Principal");
        //Setting the main menu page to be shown on the new window
        mainMenu.setScene(new Scene(new FXMLLoader(getClass().getResource("AppScreens/MainMenu-" + Main.language + ".fxml")).load()));
        mainMenu.getScene().getStylesheets().setAll(Objects.requireNonNull(getClass().getResource("Stylesheets/" + Main.displayMode + "Mode.css")).toExternalForm());
        //Making the new window not resizeable so that the user doesn't change the size of the window and the elements of the page won't look out of place
        mainMenu.setResizable(false);
        mainMenu.centerOnScreen();
        //Opening the new window
        mainMenu.show();
        //Closing the interpretations' window, which at this point is still considered as the currently open window
        Main.getCurrentStage().close();
        //Setting the new main menu window as the currently open window
        Main.setCurrentStage(mainMenu);
        switchTheme = new AnimatedThemeSwitcher(Main.getCurrentStage().getScene(), new Animation(new FadeOut()).setSpeed(2.5));
        switchTheme.init();
        //Setting the app icon
        // that's going to be shown on the taskbar to the Gender Fluid free icon created by Vitaly Gorbachev,
        // published on the flaticon website
        // (https://www.flaticon.com/free-icon/gender-fluid_3369089?term=gender&related_id=3369089)
        Main.getCurrentStage().getIcons().add(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/AppIcon.png")));
    }

    //Function triggered when the user wants to display the graph page
    @FXML
    private void goToAnalysisPage() throws IOException {
        AnimatedSwitcher as = new AnimatedSwitcher(new Animation(new SlideInRight()).setSpeed(3), new Animation(new FadeOutLeft()));
        Scene scene = new Scene(new Pane(as));
        as.of(Main.getCurrentStage().getScene().getRoot());
        as.setChild(new FXMLLoader(getClass().getResource("AppScreens/Analysis-" + Main.language + ".fxml")).load());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("Stylesheets/" + Main.displayMode + "Mode.css")).toExternalForm());
        //Changing the title of the current stage
        Main.getCurrentStage().setTitle(Main.language.equals("EN") ? "Interpretations" : Main.language.equals("FR") ? "Interprétations" : "Interpretări");
        //Displaying the graph page on the current stage
        Main.getCurrentStage().setScene(scene);
        switchTheme = new AnimatedThemeSwitcher(Main.getCurrentStage().getScene(), new Animation(new FadeOut()).setSpeed(2.5));
        switchTheme.init();
    }

    //Function that executes the app closing routine
    @FXML
    private void exitApp() {
        Main.exitAppMain();
    }

    //Function ran when the minimize button is clicked
    @FXML
    public void minimizeWindow() {
        Main.minimizeWindowMain();
    }

    //Function that toggles the app display mode between light mode and dark mode, and changes the graph
    @FXML
    public void toggleDisplayMode() {
        Main.displayMode = Main.displayMode.equals("Light") ? "Dark" : "Light";
        try {
            BufferedWriter buildUserSettings = new BufferedWriter(new FileWriter("src/main/resources/com/gendergapanalyser/gendergapanalyser/Properties.txt"));
            buildUserSettings.write("DisplayMode=" + Main.displayMode + "\nLanguage=" + Main.language + "\nCurrency=" + Main.currency + "\nExchangeRateLastUpdated=" + Main.exchangeRateLastUpdated.get(GregorianCalendar.DAY_OF_MONTH) + "." + Main.exchangeRateLastUpdated.get(GregorianCalendar.MONTH) + "." + Main.exchangeRateLastUpdated.get(GregorianCalendar.YEAR) + "\nExchangeRateToEUR=" + Main.exchangeRateEUR + "\nExchangeRateToRON=" + Main.exchangeRateRON);
            buildUserSettings.close();
            Main.processData.createSalaryGraphForEverybody();
            if (Main.processData.predictionsGenerated) {
                Main.processData.createSalaryGraphWithPredictionsForEverybody();
                if (!minimumRangeInput.getText().equals(Main.processData.datasetWithPredictions[0][1]) || !maximumRangeInput.getText().equals(Main.processData.datasetWithPredictions[Main.processData.datasetWithPredictions.length - 1][1]))
                    Main.processData.createSalaryGraphWithinRangeWithPredictionsForEverybody(Integer.parseInt(minimumRangeInput.getText()), Integer.parseInt(maximumRangeInput.getText()));
            }
            if (!minimumRangeInput.getText().equals(Main.processData.dataset[0][1]) || !maximumRangeInput.getText().equals(Main.processData.dataset[Main.processData.dataset.length - 1][1]))
                Main.processData.createSalaryGraphWithinRangeForEverybody(Integer.parseInt(minimumRangeInput.getText()), Integer.parseInt(maximumRangeInput.getText()));
        }
        catch (IOException ignored) {}
        Main.getCurrentStage().getScene().getStylesheets().setAll(Objects.requireNonNull(getClass().getResource("Stylesheets/" + Main.displayMode + "Mode.css")).toExternalForm());
        if (Main.displayMode.equals("Dark")) {
            darkModeButtonGlyph.setFitHeight(50);
            lightModeButtonGlyph.setFitHeight(35);
        }
        else {
            lightModeButtonGlyph.setFitHeight(50);
            darkModeButtonGlyph.setFitHeight(35);
        }
        voidLink.requestFocus();
        processRange();
    }

    //Function that executes when the user toggles the include pay gap checkbox that gets enabled when the user selects the men's and women's evolution graph and that displays the appropriate graph based on what the user chose from the include pay gap and prediction checkboxes
    @FXML
    private void togglePayGap() {
        //Boolean used to check if the image from the ImageView was set by the processRange() function if the requested graph or the Graphs folder can't be found
        boolean alternatePath = false;
        FileInputStream image = null;
        if (showPayGapToggle.isSelected() && !includePredictionsToggle.isSelected()) {
            try {
                image = new FileInputStream(minimumRangeInput.getText().equals(Main.processData.dataset[0][1]) && maximumRangeInput.getText().equals(Main.processData.dataset[Main.processData.dataset.length - 1][1]) ? "src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/all_genders-wageGap.png" : "src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/all_genders-wageGap-range.png");
            } catch (FileNotFoundException e) {
                try {
                    Main.processData.createSalaryGraphForEverybody();
                    if (Main.processData.predictionsGenerated)
                        Main.processData.createSalaryGraphWithPredictionsForEverybody();
                    processRange();
                    alternatePath = true;
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            showPayGapToggle.setText(Main.language.equals("EN") ? "Include pay gap" : Main.language.equals("FR") ? "Inclure l'évolution du différence de la paye" : "Include evoluția diferenței de salariu");
        }
        else if (showPayGapToggle.isSelected() && includePredictionsToggle.isSelected()) {
            try {
                image = new FileInputStream(minimumRangeInput.getText().equals(Main.processData.datasetWithPredictions[0][1]) && maximumRangeInput.getText().equals(Main.processData.datasetWithPredictions[Main.processData.datasetWithPredictions.length - 1][1]) ? "src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/all_genders-wageGap-prediction.png" : "src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/all_genders-wageGap-prediction-range.png");
            } catch (FileNotFoundException e) {
                try {
                    Main.processData.createSalaryGraphForEverybody();
                    Main.processData.createSalaryGraphWithPredictionsForEverybody();
                    processRange();
                    alternatePath = true;
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            showPayGapToggle.setText(Main.language.equals("EN") ? "Include pay gap" : Main.language.equals("FR") ? "Inclure l'évolution du différence de la paye" : "Include evoluția diferenței de salariu");
        }
        else if (!showPayGapToggle.isSelected() && includePredictionsToggle.isSelected()) {
            try {
                image = new FileInputStream(minimumRangeInput.getText().equals(Main.processData.datasetWithPredictions[0][1]) && maximumRangeInput.getText().equals(Main.processData.datasetWithPredictions[Main.processData.datasetWithPredictions.length - 1][1]) ? "src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/all_genders-prediction.png" : "src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/all_genders-prediction-range.png");
            } catch (FileNotFoundException e) {
                try {
                    Main.processData.createSalaryGraphForEverybody();
                    Main.processData.createSalaryGraphWithPredictionsForEverybody();
                    processRange();
                    alternatePath = true;
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            showPayGapToggle.setText(Main.language.equals("EN") ? "Exclude pay gap" : Main.language.equals("FR") ? "Exclure l'évolution du différence de la paye" : "Exclude evoluția diferenței de salariu");
        }
        else {
            try {
                image = new FileInputStream(minimumRangeInput.getText().equals(Main.processData.dataset[0][1]) && maximumRangeInput.getText().equals(Main.processData.dataset[Main.processData.dataset.length - 1][1]) ? "src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/all_genders.png" : "src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/all_genders-range.png");
            } catch (FileNotFoundException e) {
                try {
                    Main.processData.createSalaryGraphForEverybody();
                    if (Main.processData.predictionsGenerated)
                        Main.processData.createSalaryGraphWithPredictionsForEverybody();
                    processRange();
                    alternatePath = true;
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            showPayGapToggle.setText(Main.language.equals("EN") ? "Exclude pay gap" : Main.language.equals("FR") ? "Exclure l'évolution du différence de la paye" : "Exclude evoluția diferenței de salariu");
        }
        if (!alternatePath) {
            try {
                chartImageView.setImage(new Image(image));
                image.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    //Function ran when the user toggles the include predictions checkbox
    @FXML
    private void togglePredictions() {
        //Boolean used to check if the image from the ImageView was set by the processRange() function if the requested graph or the Graphs folder can't be found
        boolean alternatePath = false;
        //Getting the index of the currently selected graph in the graph choice box
        int selectedGraph = changeGraph.getSelectionModel().getSelectedIndex();
        FileInputStream image = null;
        //Preparing the salary table containing 3 columns for the genders, year and salary, then setting the contents of the dataset into its respective column in the table, and then clearing the salaries container and setting the table
        ObservableList data = FXCollections.observableArrayList();
        if (includePredictionsToggle.isSelected())
            data.addAll(Arrays.asList(Main.processData.datasetWithPredictions));
        else
            data.addAll(Arrays.asList(Main.processData.dataset));
        TableView<String> salaries = new TableView<>();
        TableColumn gender = new TableColumn<>(Main.language.equals("EN") ? "Gender" : Main.language.equals("FR") ? "Genre" : "Gen");
        TableColumn year = new TableColumn<>(Main.language.equals("EN") ? "Year" : "An");
        TableColumn salary = new TableColumn<>(Main.language.equals("EN") ? "Salary" : Main.language.equals("FR") ? "Salaire" : "Salariu");
        gender.setCellValueFactory((Callback<TableColumn.CellDataFeatures<String[], String>, ObservableValue>) param -> {
            switch (Main.language) {
                case "FR" -> {
                    switch (param.getValue()[0]) {
                        case "Women" -> {
                            return new SimpleStringProperty("Femmes");
                        }
                        case "Men" -> {
                            return new SimpleStringProperty("Hommes");
                        }
                    }
                }
                case "RO" -> {
                    switch (param.getValue()[0]) {
                        case "Women" -> {
                            return new SimpleStringProperty("Femei");
                        }
                        case "Men" -> {
                            return new SimpleStringProperty("Bărbați");
                        }
                    }
                }
            }
            return new SimpleStringProperty(param.getValue()[0]);
        });
        year.setCellValueFactory((Callback<TableColumn.CellDataFeatures<String[], String>, ObservableValue>) param -> new SimpleStringProperty(param.getValue()[1]));
        salary.setCellValueFactory((Callback<TableColumn.CellDataFeatures<String[], String>, ObservableValue>) param -> new SimpleStringProperty(Main.processData.formatSalary(param.getValue()[2])));
        gender.setPrefWidth(salariesView.getPrefWidth() / 3 - 6);
        year.setPrefWidth(salariesView.getPrefWidth() / 3 - 6);
        salary.setPrefWidth(salariesView.getPrefWidth() / 3 - 6);
        salaries.setPrefHeight(salariesView.getPrefHeight());
        salaries.getColumns().addAll(gender, year, salary);
        salaries.setItems(data);
        Callback existingCellFactorySalaries = salary.getCellFactory();
        salary.setCellFactory(c -> {
            TableCell cell = (TableCell) existingCellFactorySalaries.call(c);
            Tooltip t = new Tooltip();
            t.setFont(new Font("Calibri", 13));
            t.setShowDelay(Duration.millis(200));
            t.textProperty().bind(cell.itemProperty().asString());
            cell.setTooltip(t);
            return cell;
        });
        salariesView.getChildren().clear();
        salariesView.getChildren().add(salaries);

        //Preparing the pay gaps table the same way as the salary table
        ObservableList payGapsList = FXCollections.observableArrayList();
        if (includePredictionsToggle.isSelected())
            payGapsList.addAll(Arrays.asList(Main.processData.genderPayGapWithPredictions));
        else
            payGapsList.addAll(Arrays.asList(Main.processData.genderPayGap));
        TableView<String> gaps = new TableView<>();
        TableColumn yearGapTable = new TableColumn<>(Main.language.equals("EN") ? "Year" : "An");
        TableColumn payGap = new TableColumn<>(Main.language.equals("EN") ? "Gap" : Main.language.equals("FR") ? "Différence" : "Diferență");
        yearGapTable.setCellValueFactory((Callback<TableColumn.CellDataFeatures<String[], String>, ObservableValue>) param -> new SimpleStringProperty(param.getValue()[0]));
        payGap.setCellValueFactory((Callback<TableColumn.CellDataFeatures<String[], String>, ObservableValue>) param -> new SimpleStringProperty(Main.processData.formatSalary(param.getValue()[1])));
        yearGapTable.setPrefWidth(payGapsView.getPrefWidth() / 2 - 8);
        payGap.setPrefWidth(payGapsView.getPrefWidth() / 2 - 8);
        gaps.setPrefHeight(payGapsView.getPrefHeight());
        gaps.getColumns().addAll(yearGapTable, payGap);
        gaps.setItems(payGapsList);
        Callback existingCellFactoryPayGaps = payGap.getCellFactory();
        payGap.setCellFactory(c -> {
            TableCell cell = (TableCell) existingCellFactoryPayGaps.call(c);
            Tooltip t = new Tooltip();
            t.setFont(new Font("Calibri", 13));
            t.setShowDelay(Duration.millis(200));
            t.textProperty().bind(cell.itemProperty().asString());
            cell.setTooltip(t);
            return cell;
        });
        payGapsView.getChildren().clear();
        payGapsView.getChildren().add(gaps);

        if (includePredictionsToggle.isSelected()) {
            //Resetting the range selector and fields with the minimum and maximum value of the datasetWithPrediction array
            minimumRangeInput.setText(Main.processData.datasetWithPredictions[0][1]);
            rangeSlider.setMin(Double.parseDouble(Main.processData.datasetWithPredictions[0][1]));
            rangeSlider.setLowValue(Double.parseDouble(Main.processData.datasetWithPredictions[0][1]));
            rangeSlider.setMax(Double.parseDouble(Main.processData.datasetWithPredictions[Main.processData.datasetWithPredictions.length - 1][1]));
            rangeSlider.setHighValue(Double.parseDouble(Main.processData.datasetWithPredictions[Main.processData.datasetWithPredictions.length - 1][1]));
            maximumRangeInput.setText(Main.processData.datasetWithPredictions[Main.processData.datasetWithPredictions.length - 1][1]);

            //If the user chose the men's and women's evolution and checked the include pay gap checkbox, we show the all genders graph that includes the pay gap and predictions
            if (selectedGraph == 0 && showPayGapToggle.isSelected()) {
                try {
                    image = new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/all_genders-wageGap-prediction.png");
                }
                catch (FileNotFoundException e) {
                    try {
                        Main.processData.createSalaryGraphForEverybody();
                        Main.processData.createSalaryGraphWithPredictionsForEverybody();
                        processRange();
                        alternatePath = true;
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
            //If the user chose the men's and women's evolution and didn't check the include pay gap checkbox, we show the all genders graph that includes the predictions
            else if (selectedGraph == 0 && !showPayGapToggle.isSelected()) {
                try {
                    image = new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/all_genders-prediction.png");
                }
                catch (FileNotFoundException e) {
                    try {
                        Main.processData.createSalaryGraphForEverybody();
                        Main.processData.createSalaryGraphWithPredictionsForEverybody();
                        processRange();
                        alternatePath = true;
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
            //If the user chose the men's evolution graph, we show the men's graph with the predictions
            else if (selectedGraph == 1) {
                try {
                    image = new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/men-prediction.png");
                }
                catch (FileNotFoundException e) {
                    try {
                        Main.processData.createSalaryGraphForEverybody();
                        Main.processData.createSalaryGraphWithPredictionsForEverybody();
                        processRange();
                        alternatePath = true;
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
            //We do the same as the case above for the women's and wage gap's evolution graphs
            else if (selectedGraph == 2) {
                try {
                    image = new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/women-prediction.png");
                }
                catch (FileNotFoundException e) {
                    try {
                        Main.processData.createSalaryGraphForEverybody();
                        Main.processData.createSalaryGraphWithPredictionsForEverybody();
                        processRange();
                        alternatePath = true;
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
            else if (selectedGraph == 3) {
                try {
                    image = new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/wageGap-prediction.png");
                }
                catch (FileNotFoundException e) {
                    try {
                        Main.processData.createSalaryGraphForEverybody();
                        Main.processData.createSalaryGraphWithPredictionsForEverybody();
                        processRange();
                        alternatePath = true;
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
            includePredictionsToggle.setText(Main.language.equals("EN") ? "Include predictions" : Main.language.equals("FR") ? "Inclure les prédictions" : "Include predicțiile");
        }
        else {
            //Resetting the range selector and fields with the minimum and maximum value of the datasetWithPrediction array
            minimumRangeInput.setText(Main.processData.dataset[0][1]);
            rangeSlider.setMin(Double.parseDouble(Main.processData.dataset[0][1]));
            rangeSlider.setLowValue(Double.parseDouble(Main.processData.dataset[0][1]));
            rangeSlider.setMax(Double.parseDouble(Main.processData.dataset[Main.processData.dataset.length - 1][1]));
            rangeSlider.setHighValue(Double.parseDouble(Main.processData.dataset[Main.processData.dataset.length - 1][1]));
            maximumRangeInput.setText(Main.processData.dataset[Main.processData.dataset.length - 1][1]);

            //We do the same as in the case of predictions being included, but we remove the predictions
            if (selectedGraph == 0 && showPayGapToggle.isSelected()) {
                try {
                    image = new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/all_genders-wageGap.png");
                }
                catch (FileNotFoundException e) {
                    try {
                        Main.processData.createSalaryGraphForEverybody();
                        if (Main.processData.predictionsGenerated)
                            Main.processData.createSalaryGraphWithPredictionsForEverybody();
                        processRange();
                        alternatePath = true;
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
            else if (selectedGraph == 0 && !showPayGapToggle.isSelected()) {
                try {
                    image = new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/all_genders.png");
                }
                catch (FileNotFoundException e) {
                    try {
                        Main.processData.createSalaryGraphForEverybody();
                        if (Main.processData.predictionsGenerated)
                            Main.processData.createSalaryGraphWithPredictionsForEverybody();
                        processRange();
                        alternatePath = true;
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
            else if (selectedGraph == 1) {
                try {
                    image = new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/men.png");
                }
                catch (FileNotFoundException e) {
                    try {
                        Main.processData.createSalaryGraphForEverybody();
                        if (Main.processData.predictionsGenerated)
                            Main.processData.createSalaryGraphWithPredictionsForEverybody();
                        processRange();
                        alternatePath = true;
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
            else if (selectedGraph == 2) {
                try {
                    image = new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/women.png");
                }
                catch (FileNotFoundException e) {
                    try {
                        Main.processData.createSalaryGraphForEverybody();
                        if (Main.processData.predictionsGenerated)
                            Main.processData.createSalaryGraphWithPredictionsForEverybody();
                        processRange();
                        alternatePath = true;
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
            else if (selectedGraph == 3) {
                try {
                    image = new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/wageGap.png");
                }
                catch (FileNotFoundException e) {
                    try {
                        Main.processData.createSalaryGraphForEverybody();
                        if (Main.processData.predictionsGenerated)
                            Main.processData.createSalaryGraphWithPredictionsForEverybody();
                        processRange();
                        alternatePath = true;
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
            includePredictionsToggle.setText(Main.language.equals("EN") ? "Exclude predictions" : Main.language.equals("FR") ? "Exclure les prédictions" : "Exclude predicțiile");
        }
        if (!alternatePath) {
            try {
                chartImageView.setImage(new Image(image));
                image.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    //Function that executes the routine that deletes the generated predictions and then reloads the graph page with the data from the dataset
    @FXML
    private void discardPredictions() throws IOException {
        Main.processData.discardPredictions();
        Main.getCurrentStage().setScene(new Scene(new FXMLLoader(getClass().getResource("AppScreens/DisplayEvolutionGraph-" + Main.language + ".fxml")).load()));
        Main.getCurrentStage().getScene().getStylesheets().setAll(Objects.requireNonNull(getClass().getResource("Stylesheets/" + Main.displayMode + "Mode.css")).toExternalForm());
    }

    @FXML
    private void processRange() {
        try {
            if (Integer.parseInt(minimumRangeInput.getText()) < Integer.parseInt(Main.processData.dataset[0][1])) {
                minimumRangeInput.setText(Main.processData.dataset[0][1]);
                rangeSlider.setLowValue(Double.parseDouble(minimumRangeInput.getText()));
            }
            if (Integer.parseInt(maximumRangeInput.getText()) > Integer.parseInt(Main.processData.predictionsGenerated ? Main.processData.datasetWithPredictions[Main.processData.datasetWithPredictions.length - 1][1] : Main.processData.dataset[Main.processData.dataset.length - 1][1])) {
                maximumRangeInput.setText(includePredictionsToggle.isSelected() ? Main.processData.datasetWithPredictions[Main.processData.datasetWithPredictions.length - 1][1] : Main.processData.dataset[Main.processData.dataset.length - 1][1]);
                rangeSlider.setHighValue(Double.parseDouble(maximumRangeInput.getText()));
            }
            if (Integer.parseInt(minimumRangeInput.getText()) < (int) rangeSlider.getHighValue())
                rangeSlider.setLowValue(Double.parseDouble(minimumRangeInput.getText()));
            else {
                minimumRangeInput.setText(String.valueOf((int) rangeSlider.getHighValue() - 1));
                rangeSlider.setLowValue(rangeSlider.getHighValue() - 1);
            }
            if (Integer.parseInt(maximumRangeInput.getText()) > (int) rangeSlider.getLowValue())
                rangeSlider.setHighValue(Double.parseDouble(maximumRangeInput.getText()));
            else {
                maximumRangeInput.setText(String.valueOf((int) rangeSlider.getLowValue() + 1));
                rangeSlider.setHighValue(rangeSlider.getLowValue() + 1);
            }

            if (!minimumRangeInput.getText().equals(Main.processData.dataset[0][1]) || !maximumRangeInput.getText().equals(Main.processData.predictionsGenerated ? Main.processData.datasetWithPredictions[Main.processData.datasetWithPredictions.length - 1][1] : Main.processData.dataset[Main.processData.dataset.length - 1][1])) {
                if (Main.processData.predictionsGenerated && includePredictionsToggle.isSelected())
                    Main.processData.createSalaryGraphWithinRangeWithPredictionsForEverybody(Integer.parseInt(minimumRangeInput.getText()), Integer.parseInt(maximumRangeInput.getText()));
                else
                    Main.processData.createSalaryGraphWithinRangeForEverybody(Integer.parseInt(minimumRangeInput.getText()), Integer.parseInt(maximumRangeInput.getText()));
            }
        }
        catch (NumberFormatException e) {
            minimumRangeInput.setText(String.valueOf((int) rangeSlider.getLowValue()));
            maximumRangeInput.setText(String.valueOf((int) rangeSlider.getHighValue()));
            processRange();
        }

        //Updating the chart view with the updated graphs
        FileInputStream graph = null;
        if (changeGraph.getSelectionModel().getSelectedIndex() == 0 && showPayGapToggle.isSelected()) {
            if (includePredictionsToggle.isSelected()) {
                try {
                    graph = new FileInputStream(minimumRangeInput.getText().equals(Main.processData.datasetWithPredictions[0][1]) && maximumRangeInput.getText().equals(Main.processData.datasetWithPredictions[Main.processData.datasetWithPredictions.length - 1][1]) ? "src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/all_genders-wageGap-prediction.png" : "src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/all_genders-wageGap-prediction-range.png");
                } catch (FileNotFoundException e) {
                    try {
                        Main.processData.createSalaryGraphForEverybody();
                        Main.processData.createSalaryGraphWithPredictionsForEverybody();
                        processRange();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
            else {
                try {
                    graph = new FileInputStream(minimumRangeInput.getText().equals(Main.processData.dataset[0][1]) && maximumRangeInput.getText().equals(Main.processData.dataset[Main.processData.dataset.length - 1][1]) ? "src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/all_genders-wageGap.png" : "src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/all_genders-wageGap-range.png");
                } catch (FileNotFoundException e) {
                    try {
                        Main.processData.createSalaryGraphForEverybody();
                        if (Main.processData.predictionsGenerated)
                            Main.processData.createSalaryGraphWithPredictionsForEverybody();
                        processRange();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        }
        else if (changeGraph.getSelectionModel().getSelectedIndex() == 0 && !showPayGapToggle.isSelected()) {
            if (includePredictionsToggle.isSelected()) {
                try {
                    graph = new FileInputStream(minimumRangeInput.getText().equals(Main.processData.datasetWithPredictions[0][1]) && maximumRangeInput.getText().equals(Main.processData.datasetWithPredictions[Main.processData.datasetWithPredictions.length - 1][1]) ? "src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/all_genders-prediction.png" : "src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/all_genders-prediction-range.png");
                } catch (FileNotFoundException e) {
                    try {
                        Main.processData.createSalaryGraphForEverybody();
                        Main.processData.createSalaryGraphWithPredictionsForEverybody();
                        processRange();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
            else {
                try {
                    graph = new FileInputStream(minimumRangeInput.getText().equals(Main.processData.dataset[0][1]) && maximumRangeInput.getText().equals(Main.processData.dataset[Main.processData.dataset.length - 1][1]) ? "src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/all_genders.png" : "src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/all_genders-range.png");
                } catch (FileNotFoundException e) {
                    try {
                        Main.processData.createSalaryGraphForEverybody();
                        if (Main.processData.predictionsGenerated)
                            Main.processData.createSalaryGraphWithPredictionsForEverybody();
                        processRange();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        }
        else if (changeGraph.getSelectionModel().getSelectedIndex() == 1) {
            if (includePredictionsToggle.isSelected()) {
                try {
                    graph = new FileInputStream(minimumRangeInput.getText().equals(Main.processData.datasetWithPredictions[0][1]) && maximumRangeInput.getText().equals(Main.processData.datasetWithPredictions[Main.processData.datasetWithPredictions.length - 1][1]) ? "src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/men-prediction.png" : "src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/men-prediction-range.png");
                } catch (FileNotFoundException e) {
                    try {
                        Main.processData.createSalaryGraphForEverybody();
                        Main.processData.createSalaryGraphWithPredictionsForEverybody();
                        processRange();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
            else {
                try {
                    graph = new FileInputStream(minimumRangeInput.getText().equals(Main.processData.dataset[0][1]) && maximumRangeInput.getText().equals(Main.processData.dataset[Main.processData.dataset.length - 1][1]) ? "src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/men.png" : "src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/men-range.png");
                } catch (FileNotFoundException e) {
                    try {
                        Main.processData.createSalaryGraphForEverybody();
                        if (Main.processData.predictionsGenerated)
                            Main.processData.createSalaryGraphWithPredictionsForEverybody();
                        processRange();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        }
        else if (changeGraph.getSelectionModel().getSelectedIndex() == 2) {
            if (includePredictionsToggle.isSelected()) {
                try {
                    graph = new FileInputStream(minimumRangeInput.getText().equals(Main.processData.datasetWithPredictions[0][1]) && maximumRangeInput.getText().equals(Main.processData.datasetWithPredictions[Main.processData.datasetWithPredictions.length - 1][1]) ? "src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/women-prediction.png" : "src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/women-prediction-range.png");
                    }
                catch (FileNotFoundException e) {
                    try {
                        Main.processData.createSalaryGraphForEverybody();
                        Main.processData.createSalaryGraphWithPredictionsForEverybody();
                        processRange();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
            else {
                try {
                    graph = new FileInputStream(minimumRangeInput.getText().equals(Main.processData.dataset[0][1]) && maximumRangeInput.getText().equals(Main.processData.dataset[Main.processData.dataset.length - 1][1]) ? "src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/women.png" : "src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/women-range.png");
                } catch (FileNotFoundException e) {
                    try {
                        Main.processData.createSalaryGraphForEverybody();
                        if (Main.processData.predictionsGenerated)
                            Main.processData.createSalaryGraphWithPredictionsForEverybody();
                        processRange();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        }
        else if (changeGraph.getSelectionModel().getSelectedIndex() == 3) {
            if (includePredictionsToggle.isSelected()) {
                try {
                    graph = new FileInputStream(minimumRangeInput.getText().equals(Main.processData.datasetWithPredictions[0][1]) && maximumRangeInput.getText().equals(Main.processData.datasetWithPredictions[Main.processData.datasetWithPredictions.length - 1][1]) ? "src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/wageGap-prediction.png" : "src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/wageGap-prediction-range.png");
                } catch (FileNotFoundException e) {
                    try {
                        Main.processData.createSalaryGraphForEverybody();
                        Main.processData.createSalaryGraphWithPredictionsForEverybody();
                        processRange();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
            else {
                try {
                    graph = new FileInputStream(minimumRangeInput.getText().equals(Main.processData.dataset[0][1]) && maximumRangeInput.getText().equals(Main.processData.dataset[Main.processData.dataset.length - 1][1]) ? "src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/wageGap.png" : "src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/wageGap-range.png");
                } catch (FileNotFoundException e) {
                    try {
                        Main.processData.createSalaryGraphForEverybody();
                        if (Main.processData.predictionsGenerated)
                            Main.processData.createSalaryGraphWithPredictionsForEverybody();
                        processRange();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        }
        chartImageView.setImage(new Image(graph));
        try {
            graph.close();
        } catch (IOException ignored) {}
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        promptAnimator.setIn(new Animation(new ZoomIn()).setSpeed(3));
        promptAnimator.setOut(new Animation(new ZoomOut()).setSpeed(10));
        darkOverlayAnimator.setIn(new Animation(new FadeIn()).setSpeed(3));
        darkOverlayAnimator.setOut(new Animation(new ZoomOut()).setSpeed(10));
        titleBar.setOnMousePressed(event -> {
            Main.dragX = Main.getCurrentStage().getX() - event.getScreenX();
            Main.dragY = Main.getCurrentStage().getY() - event.getScreenY();
        });
        titleBar.setOnMouseDragged(event -> {
            Main.getCurrentStage().setX(event.getScreenX() + Main.dragX);
            Main.getCurrentStage().setY(event.getScreenY() + Main.dragY);
        });
        //Setting up the language picker
        languagePicker.setItems(FXCollections.observableArrayList(Main.languages));
        switch (Main.language) {
            case "EN" -> languagePicker.setValue(Main.languages[0]);
            case "FR" -> languagePicker.setValue(Main.languages[1]);
            case "RO" -> languagePicker.setValue(Main.languages[2]);
        }
        //When selecting another language from the language picker...
        languagePicker.getSelectionModel().selectedIndexProperty().addListener(((observable, oldValue, newValue) -> {
            //Updating the language with the newly picked one
            Main.language = Main.languagesShort[newValue.intValue()];
            //Setting the boolean variable used by DataProcessing.createPDF method to true
            // so that the method generates a new PDF document in a new language
            Main.changedLanguage = true;
            //Checking if the currency has been updated (default is -1.0)
            if (Main.exchangeRateEUR != -1.0 && Main.exchangeRateRON != -1.0) {
                //Updating the currency with the one associated with the language
                Main.currency = Main.currencies[newValue.intValue()];
                //Setting the boolean variable used by DataProcessing.createPDF method to true
                // so that the method generates a new PDF document with the new currency
                Main.changedCurrency = true;
            }
            else
                currencyPicker.setValue(Main.currencies[0]);

            Runnable rebuildResources = () -> {
                try {
                    BufferedWriter buildUserSettings = new BufferedWriter(new FileWriter("src/main/resources/com/gendergapanalyser/gendergapanalyser/Properties.txt"));
                    buildUserSettings.write("DisplayMode=" + Main.displayMode + "\nLanguage=" + Main.language + "\nCurrency=" + Main.currency + "\nExchangeRateLastUpdated=" + Main.exchangeRateLastUpdated.get(GregorianCalendar.DAY_OF_MONTH) + "." + Main.exchangeRateLastUpdated.get(GregorianCalendar.MONTH) + "." + Main.exchangeRateLastUpdated.get(GregorianCalendar.YEAR) + "\nExchangeRateToEUR=" + Main.exchangeRateEUR + "\nExchangeRateToRON=" + Main.exchangeRateRON);
                    buildUserSettings.close();
                    //Checking if the currencies have ever been updated
                    if (Main.exchangeRateEUR != -1.0 && Main.exchangeRateRON != -1.0) {
                        //Creating the usable dataset,
                        // graphs and interpretations again so that it uses the new language and currency
                        Main.processData.prepareData();
                    }
                    else {
                        //Just regenerating the graphs and interpretations to use the new language
                        Main.processData.createSalaryGraphForEverybody();
                        Main.processData.performAnalysis();
                    }
                    //Recreating predictions and prediction graphs so that they use the newly set language
                    if (Main.processData.predictionsGenerated) {
                        Main.processData.predictEvolutions(Main.predictionValue);
                        Main.processData.createSalaryGraphWithPredictionsForEverybody();
                    }
                } catch (IOException ignored) {}

                Platform.runLater(() -> {
                    try {
                        Scene graphScene = new Scene(new FXMLLoader(getClass().getResource("AppScreens/DisplayEvolutionGraph-" + Main.language + ".fxml")).load());
                        graphScene.getStylesheets().setAll(Objects.requireNonNull(getClass().getResource("Stylesheets/" + Main.displayMode + "Mode.css")).toExternalForm());
                        Main.getCurrentStage().setScene(graphScene);
                    } catch (IOException ignored) {}
                    //Changing the title of the current stage
                    Main.getCurrentStage().setTitle(Main.language.equals("EN") ? "Evolution Graph" : Main.language.equals("FR") ? "Graphe d'Évolution" : "Grafic de Evoluție");
                    switchTheme = new AnimatedThemeSwitcher(Main.getCurrentStage().getScene(), new Animation(new FadeOut()).setSpeed(2.5));
                    switchTheme.init();
                });
            };
            promptAnimator.setChild(new Pane(backgroundOperations));
            darkOverlayAnimator.setChild(new Pane(darkOverlay));
            try {
                loadingCircleImageView.setImage(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/loading-" + Main.displayMode + ".gif")));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            backgroundOperations.setVisible(true);
            darkOverlay.setVisible(true);
            new Thread(rebuildResources).start();
        }));

        //Setting up the currency picker
        currencyPicker.setItems(FXCollections.observableArrayList(Main.currencies));
        switch (Main.currency) {
            case "USD" -> currencyPicker.setValue(Main.currencies[0]);
            case "EUR" -> currencyPicker.setValue(Main.currencies[1]);
            case "RON" -> currencyPicker.setValue(Main.currencies[2]);
        }
        //Checking if the currencies were ever updated or if they are set to their default -1.0 value
        if (Main.exchangeRateEUR != -1.0 && Main.exchangeRateRON != -1.0) {
            //When selecting another language from the currency picker...
            currencyPicker.getSelectionModel().selectedIndexProperty().addListener(((observable, oldValue, newValue) -> {
                //Updating the currency with the newly picked one
                Main.currency = Main.currencies[newValue.intValue()];
                //Setting the boolean variable used by DataProcessing.createPDF method to true so that the method generates a new PDF document with the new currency
                Main.changedCurrency = true;
                Runnable rebuildResources = () -> {
                    try {
                        BufferedWriter buildUserSettings = new BufferedWriter(new FileWriter("src/main/resources/com/gendergapanalyser/gendergapanalyser/Properties.txt"));
                        buildUserSettings.write("DisplayMode=" + Main.displayMode + "\nLanguage=" + Main.language + "\nCurrency=" + Main.currency + "\nExchangeRateLastUpdated=" + Main.exchangeRateLastUpdated.get(GregorianCalendar.DAY_OF_MONTH) + "." + Main.exchangeRateLastUpdated.get(GregorianCalendar.MONTH) + "." + Main.exchangeRateLastUpdated.get(GregorianCalendar.YEAR) + "\nExchangeRateToEUR=" + Main.exchangeRateEUR + "\nExchangeRateToRON=" + Main.exchangeRateRON);
                        buildUserSettings.close();
                        //Creating the usable dataset, graphs and interpretations again so that it uses the new currency
                        Main.processData.prepareData();
                        //Recreating predictions and prediction graphs so that they use the newly set currency
                        if (Main.processData.predictionsGenerated) {
                            Main.processData.predictEvolutions(Main.predictionValue);
                            Main.processData.createSalaryGraphWithPredictionsForEverybody();
                        }
                    } catch (IOException ignored) {
                    }

                    Platform.runLater(() -> {
                        try {
                            Scene graphScene = new Scene(new FXMLLoader(getClass().getResource("AppScreens/DisplayEvolutionGraph-" + Main.language + ".fxml")).load());
                            graphScene.getStylesheets().setAll(Objects.requireNonNull(getClass().getResource("Stylesheets/" + Main.displayMode + "Mode.css")).toExternalForm());
                            Main.getCurrentStage().setScene(graphScene);
                        } catch (IOException ignored) {
                        }
                        switchTheme = new AnimatedThemeSwitcher(Main.getCurrentStage().getScene(), new Animation(new FadeOut()).setSpeed(2.5));
                        switchTheme.init();
                    });
                };
                promptAnimator.setChild(new Pane(backgroundOperations));
                darkOverlayAnimator.setChild(new Pane(darkOverlay));
                try {
                    loadingCircleImageView.setImage(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/loading-" + Main.displayMode + ".gif")));
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
                backgroundOperations.setVisible(true);
                darkOverlay.setVisible(true);
                new Thread(rebuildResources).start();
            }));
        }
        else currencyPicker.setDisable(true);

        //Setting up the theme toggle
        if (Main.displayMode.equals("Dark")) {
            darkModeButtonGlyph.setFitHeight(50);
            lightModeButtonGlyph.setFitHeight(35);
        }
        else {
            lightModeButtonGlyph.setFitHeight(50);
            darkModeButtonGlyph.setFitHeight(35);
        }

        //Preparing the chart display
        try {
            FileInputStream initialImageInput = new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/all_genders.png");
            chartImageView.setImage(new Image(initialImageInput));
            initialImageInput.close();
        } catch (IOException e) {
            try {
                Main.processData.createSalaryGraphForEverybody();
                if (Main.processData.predictionsGenerated)
                    Main.processData.createSalaryGraphWithPredictionsForEverybody();
                FileInputStream initialImageInput = new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/all_genders.png");
                chartImageView.setImage(new Image(initialImageInput));
                initialImageInput.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        //Preparing the salary table containing 3 columns for the genders, year and salary, then setting the contents of the dataset into its respective column in the table, then setting the table to its container
        ObservableList data = FXCollections.observableArrayList();
        if (Main.processData.predictionsGenerated)
            data.addAll(Arrays.asList(Main.processData.datasetWithPredictions));
        else
            data.addAll(Arrays.asList(Main.processData.dataset));
        TableView<String> salaries = new TableView<>();
        TableColumn gender = new TableColumn<>(Main.language.equals("EN") ? "Gender" : Main.language.equals("FR") ? "Genre" : "Gen");
        TableColumn year = new TableColumn<>(Main.language.equals("EN") ? "Year" : "An");
        TableColumn salary = new TableColumn<>(Main.language.equals("EN") ? "Salary" : Main.language.equals("FR") ? "Salaire" : "Salariu");
        gender.setCellValueFactory((Callback<TableColumn.CellDataFeatures<String[], String>, ObservableValue>) param -> {
            switch (Main.language) {
                case "FR" -> {
                    switch (param.getValue()[0]) {
                        case "Women" -> {
                            return new SimpleStringProperty("Femmes");
                        }
                        case "Men" -> {
                            return new SimpleStringProperty("Hommes");
                        }
                    }
                }
                case "RO" -> {
                    switch (param.getValue()[0]) {
                        case "Women" -> {
                            return new SimpleStringProperty("Femei");
                        }
                        case "Men" -> {
                            return new SimpleStringProperty("Bărbați");
                        }
                    }
                }
            }
            return new SimpleStringProperty(param.getValue()[0]);
        });
        year.setCellValueFactory((Callback<TableColumn.CellDataFeatures<String[], String>, ObservableValue>) param -> new SimpleStringProperty(param.getValue()[1]));
        salary.setCellValueFactory((Callback<TableColumn.CellDataFeatures<String[], String>, ObservableValue>) param -> new SimpleStringProperty(Main.processData.formatSalary(param.getValue()[2])));
        gender.setPrefWidth(salariesView.getPrefWidth() / 3 - 6);
        year.setPrefWidth(salariesView.getPrefWidth() / 3 - 6);
        salary.setPrefWidth(salariesView.getPrefWidth() / 3 - 6);
        salaries.setPrefHeight(salariesView.getPrefHeight());
        salaries.getColumns().addAll(gender, year, salary);
        salaries.setItems(data);
        Callback existingCellFactorySalaries = salary.getCellFactory();
        salary.setCellFactory(c -> {
            TableCell cell = (TableCell) existingCellFactorySalaries.call(c);
            Tooltip t = new Tooltip();
            t.setFont(new Font("Calibri", 13));
            t.setShowDelay(Duration.millis(200));
            t.textProperty().bind(cell.itemProperty().asString());
            cell.setTooltip(t);
            return cell;
        });
        salariesView.getChildren().add(salaries);

        //Preparing the pay gaps table the same way as the salary table
        ObservableList payGapsList = FXCollections.observableArrayList();
        if (Main.processData.predictionsGenerated)
            payGapsList.addAll(Arrays.asList(Main.processData.genderPayGapWithPredictions));
        else
            payGapsList.addAll(Arrays.asList(Main.processData.genderPayGap));
        TableView<String> gaps = new TableView<>();
        TableColumn yearGapTable = new TableColumn<>(Main.language.equals("EN") ? "Year" : "An");
        TableColumn payGap = new TableColumn<>(Main.language.equals("EN") ? "Gap" : Main.language.equals("FR") ? "Différence" : "Diferență");
        yearGapTable.setCellValueFactory((Callback<TableColumn.CellDataFeatures<String[], String>, ObservableValue>) param -> new SimpleStringProperty(param.getValue()[0]));
        payGap.setCellValueFactory((Callback<TableColumn.CellDataFeatures<String[], String>, ObservableValue>) param -> new SimpleStringProperty(Main.processData.formatSalary(param.getValue()[1])));
        yearGapTable.setPrefWidth(payGapsView.getPrefWidth() / 2 - 8);
        payGap.setPrefWidth(payGapsView.getPrefWidth() / 2 - 8);
        gaps.setPrefHeight(payGapsView.getPrefHeight());
        gaps.getColumns().addAll(yearGapTable, payGap);
        gaps.setItems(payGapsList);
        Callback existingCellFactoryPayGaps = payGap.getCellFactory();
        payGap.setCellFactory(c -> {
            TableCell cell = (TableCell) existingCellFactoryPayGaps.call(c);
            Tooltip t = new Tooltip();
            t.setFont(new Font("Calibri", 13));
            t.setShowDelay(Duration.millis(200));
            t.textProperty().bind(cell.itemProperty().asString());
            cell.setTooltip(t);
            return cell;
        });
        payGapsView.getChildren().add(gaps);

        //Populating the graph choice box and setting specific changes based on the current choice
        switch (Main.language) {
            case "EN" -> {
                changeGraph.setItems(FXCollections.observableArrayList(choices_EN));
                //Setting the current selected value of the graph choice box as the first item of the choices array (Men's and women's wages)
                changeGraph.setValue(choices_EN[0]);
            }
            case "FR" -> {
                changeGraph.setItems(FXCollections.observableArrayList(choices_FR));
                //Setting the current selected value of the graph choice box as the first item of the choices array (Men's and women's wages)
                changeGraph.setValue(choices_FR[0]);
            }
            case "RO" -> {
                changeGraph.setItems(FXCollections.observableArrayList(choices_RO));
                //Setting the current selected value of the graph choice box as the first item of the choices array (Men's and women's wages)
                changeGraph.setValue(choices_RO[0]);
            }
        }

        //If the user changes their graph choice
        changeGraph.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            FileInputStream imageInput = null;
            //Boolean used to check if the image from the ImageView was set by the processRange() function if the requested graph or the Graphs folder can't be found
            boolean alternatePath = false;
            //If the user chooses the first option
            if (newValue.intValue() == 0) {
                //Making the include pay gap checkbox selectable
                showPayGapToggle.setDisable(false);
                //If the user chose to include predictions, we select the all genders graph that includes predictions
                if (includePredictionsToggle.isSelected()) {
                    try {
                        imageInput = new FileInputStream(minimumRangeInput.getText().equals(Main.processData.datasetWithPredictions[0][1]) && maximumRangeInput.getText().equals(Main.processData.datasetWithPredictions[Main.processData.datasetWithPredictions.length - 1][1]) ? "src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/all_genders-prediction.png" : "src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/all_genders-prediction-range.png");
                    } catch (FileNotFoundException e) {
                        try {
                            Main.processData.createSalaryGraphForEverybody();
                            Main.processData.createSalaryGraphWithPredictionsForEverybody();
                            processRange();
                            alternatePath = true;
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
                //If not, we select the all genders graph that doesn't include predictions
                else {
                    try {
                        imageInput = new FileInputStream(minimumRangeInput.getText().equals(Main.processData.dataset[0][1]) && maximumRangeInput.getText().equals(Main.processData.dataset[Main.processData.dataset.length - 1][1]) ? "src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/all_genders.png" : "src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/all_genders-range.png");
                    } catch (FileNotFoundException e) {
                        try {
                            Main.processData.createSalaryGraphForEverybody();
                            if (Main.processData.predictionsGenerated)
                                Main.processData.createSalaryGraphWithPredictionsForEverybody();
                            processRange();
                            alternatePath = true;
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
            }
            //If the user chooses the second option
            else if (newValue.intValue() == 1) {
                //We deselect and disable the include pay gap checkbox
                showPayGapToggle.setSelected(false);
                showPayGapToggle.setDisable(true);
                //If the user chose to include predictions, we select the men graph that includes predictions
                if (includePredictionsToggle.isSelected()) {
                    try {
                        imageInput = new FileInputStream(minimumRangeInput.getText().equals(Main.processData.datasetWithPredictions[0][1]) && maximumRangeInput.getText().equals(Main.processData.datasetWithPredictions[Main.processData.datasetWithPredictions.length - 1][1]) ? "src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/men-prediction.png" : "src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/men-prediction-range.png");
                    } catch (FileNotFoundException e) {
                        try {
                            Main.processData.createSalaryGraphForEverybody();
                            Main.processData.createSalaryGraphWithPredictionsForEverybody();
                            processRange();
                            alternatePath = true;
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
                //If not, we select the men graph that doesn't include predictions
                else {
                    try {
                        imageInput = new FileInputStream(minimumRangeInput.getText().equals(Main.processData.dataset[0][1]) && maximumRangeInput.getText().equals(Main.processData.dataset[Main.processData.dataset.length - 1][1]) ? "src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/men.png" : "src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/men-range.png");
                    } catch (FileNotFoundException e) {
                        try {
                            Main.processData.createSalaryGraphForEverybody();
                            if (Main.processData.predictionsGenerated)
                                Main.processData.createSalaryGraphWithPredictionsForEverybody();
                            processRange();
                            alternatePath = true;
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
            }
            //We do the same settings as above for the other 2 values
            else if (newValue.intValue() == 2) {
                showPayGapToggle.setSelected(false);
                showPayGapToggle.setDisable(true);
                if (includePredictionsToggle.isSelected()) {
                    try {
                        imageInput = new FileInputStream(minimumRangeInput.getText().equals(Main.processData.datasetWithPredictions[0][1]) && maximumRangeInput.getText().equals(Main.processData.datasetWithPredictions[Main.processData.datasetWithPredictions.length - 1][1]) ? "src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/women-prediction.png" : "src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/women-prediction-range.png");
                    } catch (FileNotFoundException e) {
                        try {
                            Main.processData.createSalaryGraphForEverybody();
                            Main.processData.createSalaryGraphWithPredictionsForEverybody();
                            processRange();
                            alternatePath = true;
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
                else {
                    try {
                        imageInput = new FileInputStream(minimumRangeInput.getText().equals(Main.processData.dataset[0][1]) && maximumRangeInput.getText().equals(Main.processData.dataset[Main.processData.dataset.length - 1][1]) ? "src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/women.png" : "src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/women-range.png");
                    } catch (FileNotFoundException e) {
                        try {
                            Main.processData.createSalaryGraphForEverybody();
                            if (Main.processData.predictionsGenerated)
                                Main.processData.createSalaryGraphWithPredictionsForEverybody();
                            processRange();
                            alternatePath = true;
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
            }
            else if (newValue.intValue() == 3) {
                showPayGapToggle.setSelected(false);
                showPayGapToggle.setDisable(true);
                if (includePredictionsToggle.isSelected()) {
                    try {
                        imageInput = new FileInputStream(minimumRangeInput.getText().equals(Main.processData.datasetWithPredictions[0][1]) && maximumRangeInput.getText().equals(Main.processData.datasetWithPredictions[Main.processData.datasetWithPredictions.length - 1][1]) ? "src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/wageGap-prediction.png" : "src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/wageGap-prediction-range.png");
                    } catch (FileNotFoundException e) {
                        try {
                            Main.processData.createSalaryGraphForEverybody();
                            Main.processData.createSalaryGraphWithPredictionsForEverybody();
                            processRange();
                            alternatePath = true;
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
                else {
                    try {
                        imageInput = new FileInputStream(minimumRangeInput.getText().equals(Main.processData.dataset[0][1]) && maximumRangeInput.getText().equals(Main.processData.dataset[Main.processData.dataset.length - 1][1]) ? "src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/wageGap.png" : "src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/wageGap-range.png");
                    } catch (FileNotFoundException e) {
                        try {
                            Main.processData.createSalaryGraphForEverybody();
                            if (Main.processData.predictionsGenerated)
                                Main.processData.createSalaryGraphWithPredictionsForEverybody();
                            processRange();
                            alternatePath = true;
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
            }
            //We set the chart container to show the graph the user chose
            if (!alternatePath) {
                chartImageView.setImage(new Image(imageInput));
                try {
                    imageInput.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        //Setting up the range slider and the minimum/maximum text fields that reflect the edges of the year range to be included in the graph
        minimumRangeInput.setText(Main.processData.dataset[0][1]);
        maximumRangeInput.setText(Main.processData.predictionsGenerated ? Main.processData.datasetWithPredictions[Main.processData.datasetWithPredictions.length - 1][1] : Main.processData.dataset[Main.processData.dataset.length - 1][1]);
        rangeSlider.setBlockIncrement(1.0);
        rangeSlider.setMin(Double.parseDouble(Main.processData.dataset[0][1]));
        rangeSlider.setMax(Double.parseDouble(Main.processData.predictionsGenerated ? Main.processData.datasetWithPredictions[Main.processData.datasetWithPredictions.length - 1][1] : Main.processData.dataset[Main.processData.dataset.length - 1][1]));
        rangeSlider.setLowValue(Double.parseDouble(Main.processData.dataset[0][1]));
        rangeSlider.setHighValue(Double.parseDouble(Main.processData.predictionsGenerated ? Main.processData.datasetWithPredictions[Main.processData.datasetWithPredictions.length - 1][1] : Main.processData.dataset[Main.processData.dataset.length - 1][1]));

        //Setting the range slider so that the minimum edge button doesn't go higher than the maximum one and vice versa
        rangeSlider.lowValueProperty().addListener(((observable, oldValue, newValue) -> {
            if (rangeSlider.getLowValue() < rangeSlider.getHighValue())
                minimumRangeInput.setText(String.valueOf((int) rangeSlider.getLowValue()));
            else {
                rangeSlider.setLowValue((int) rangeSlider.getHighValue() - 1);
                minimumRangeInput.setText(String.valueOf((int) rangeSlider.getHighValue() - 1));
            }
        }));
        rangeSlider.highValueProperty().addListener(((observable) -> {
            if (rangeSlider.getHighValue() > rangeSlider.getLowValue())
                maximumRangeInput.setText(String.valueOf((int) rangeSlider.getHighValue()));
            else {
                rangeSlider.setHighValue((int) rangeSlider.getLowValue() + 1);
                maximumRangeInput.setText(String.valueOf((int) rangeSlider.getLowValue() + 1));
            }
        }));

        //If the user generated predictions, we enable the prediction controls. If not, we disable them. We also select the include predictions checkbox and execute the togglePredictions function
        predictionControls.setDisable(!Main.processData.predictionsGenerated);
        if (Main.processData.predictionsGenerated) {
            includePredictionsToggle.setSelected(true);
            togglePredictions();
        }
    }
}
