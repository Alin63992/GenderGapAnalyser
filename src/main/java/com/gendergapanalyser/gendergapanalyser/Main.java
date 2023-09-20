package com.gendergapanalyser.gendergapanalyser;

import animatefx.animation.FadeIn;
import animatefx.animation.FadeOut;
import animatefx.animation.ZoomIn;
import eu.iamgio.animated.transition.AnimatedSwitcher;
import eu.iamgio.animated.transition.AnimatedThemeSwitcher;
import eu.iamgio.animated.transition.Animation;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.validator.routines.EmailValidator;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;

public class Main extends Application implements Initializable {
    private static Stage currentStage;
    public ToggleButton sourcesToggle;
    public ToggleButton creditsToggle;
    public AnchorPane contentCredits;
    public Hyperlink USDeptOfLaborHyperlink;
    public Hyperlink ERAHyperlink;
    public ImageView appIconCreditsImageView;
    public ImageView appIconImageView;
    public Text updateLabel;
    @FXML
    private AnchorPane contentSources;
    @FXML
    private Hyperlink voidLink;
    @FXML
    private Hyperlink dataSourcesInfo;
    @FXML
    private AnchorPane missingOutgoingCredentialsPrompt;
    @FXML
    private TextField outgoingEmailField;
    @FXML
    private PasswordField outgoingPasswordField;
    @FXML
    private Text invalidOutgoingEmailWarning;
    @FXML
    private Rectangle darkOverlay;
    @FXML
    private AnimatedSwitcher darkOverlayAnimator;
    @FXML
    private AnimatedSwitcher promptAnimator;
    @FXML
    private AnchorPane titleBar;
    @FXML
    private ChoiceBox<String> languagePicker;
    @FXML
    private ChoiceBox<String> currencyPicker;
    @FXML
    private Button lightModeButton;
    @FXML
    private Button darkModeButton;
    @FXML
    private ImageView lightModeButtonGlyph;
    @FXML
    private ImageView darkModeButtonGlyph;
    @FXML
    private AnchorPane predictionPrompt;
    @FXML
    private AnchorPane discardConfirmation;
    @FXML
    private AnchorPane backgroundOperations;
    @FXML
    private TextField predictionField;
    @FXML
    private Text invalidNumberWarning;
    @FXML
    private AnchorPane emailPrompt;
    @FXML
    private AnchorPane dataSources;
    @FXML
    private Label usDeptOfLaborYearRangeLabel;
    @FXML
    private ImageView ERALogoImageView;
    @FXML
    private Label ERALastUpdatedLabel;
    @FXML
    private TextField emailField;
    @FXML
    private Text invalidEmailWarning;
    @FXML
    private Button graphsButton;
    @FXML
    private Button analysisButton;
    @FXML
    private Button predictButton;
    @FXML
    private Button discardPredictionsButton;
    @FXML
    private Button PDFButton;
    @FXML
    private Button mailButton;
    @FXML
    private ImageView loadingCircleImageView;
    protected static DataProcessing processData;
    protected static String displayMode = "Dark";
    protected static String language = "EN";
    protected static boolean changedLanguage = false;
    protected static boolean changedCurrency = false;
    protected static GregorianCalendar exchangeRateLastUpdated = (GregorianCalendar) GregorianCalendar.getInstance();
    protected static double exchangeRateEUR = 1.0;
    protected static double exchangeRateRON = 1.0;
    protected static String currency = "USD";
    protected static final String[] languages = {"English", "Français", "Română"};
    protected static final String[] languagesShort = {"EN", "FR", "RO"};
    protected static final String[] currencies = {"USD", "EUR", "RON"};
    public static double dragX;
    public static double dragY;
    private static final Thread downloadDataset = new Thread(new GetUpdatedDatasetInBackground());
    protected static int predictionValue = 0;
    protected static String email = "";
    protected static String outgoingAccountEmail = "";
    protected static String outgoingAccountPassword = "";
    private boolean fileNotFoundOnGit = false;
    private AnimatedThemeSwitcher switchTheme;


    //Function used to set the currently open window to be used in the future
    public static void setCurrentStage(Stage s) {
        currentStage = s;
        currentStage.setOnCloseRequest(action -> exitAppMain());
    }

    //Function used by the DisplayEvolutionGraph class to get the currently open window
    public static Stage getCurrentStage() {
        return currentStage;
    }

    //Function used to delete the generated graphs and quit the app
    public static void exitAppMain() {
        //Killing the thread that downloads the updated dataset, if it's still running
        if (downloadDataset.isAlive()) {
            downloadDataset.interrupt();
        }

        //Deleting the Graphs folder
        try {
            FileUtils.deleteDirectory(new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs"));
        } catch (IOException ignored) {}

        //Deleting the downloaded dataset file (if it exists)
        try {
            Files.delete(Path.of("src/main/resources/com/gendergapanalyser/gendergapanalyser/0DownloadedDataset.csv"));
        } catch (IOException ignored) {}

        //Deleting the generated PDF report (if it exists)
        try {
            Files.delete(Path.of("src/main/resources/com/gendergapanalyser/gendergapanalyser/Analysis.pdf"));
        } catch (IOException ignored) {}
        //Closing the window
        getCurrentStage().close();
    }

    //Function that gets run when the close button is pressed
    @FXML
    public void exitApp() {
        Main.exitAppMain();
    }

    //Function used to minimize the app
    public static void minimizeWindowMain() {
        getCurrentStage().setIconified(true);
    }

    //Function ran when the minimization button is clicked
    @FXML
    public void minimizeWindow() {
        Main.minimizeWindowMain();
    }

    //Function that toggles the app display mode between light mode and dark mode
    @FXML
    public void toggleDisplayMode() throws IOException {
        displayMode = displayMode.equals("Light") ? "Dark" : "Light";
        BufferedWriter buildUserSettings = new BufferedWriter(new FileWriter("src/main/resources/com/gendergapanalyser/gendergapanalyser/UserSettings.txt"));
        buildUserSettings.write("DisplayMode=" + displayMode + "\nLanguage=" + language + "\nCurrency=" + currency + "\nExchangeRateLastUpdated=" + exchangeRateLastUpdated.get(Calendar.DAY_OF_MONTH) + "." + exchangeRateLastUpdated.get(Calendar.MONTH) + "." + exchangeRateLastUpdated.get(Calendar.YEAR) + "\nExchangeRateToEUR=" + exchangeRateEUR + "\nExchangeRateToRON=" + exchangeRateRON);
        buildUserSettings.close();
        if (processData.predictionsGenerated) processData.createSalaryGraphWithPredictionsForEverybody();
        processData.createSalaryGraphForEverybody();
        getCurrentStage().getScene().getStylesheets().setAll(Objects.requireNonNull(getClass().getResource("Stylesheets/" + displayMode + "Mode.css")).toExternalForm());
        ERALogoImageView.setImage(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/ExchangeRate-API-Logo-" + displayMode + ".png")));
        if (displayMode.equals("Dark")) {
            appIconCreditsImageView.setImage(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/AppIcon-Dark.png")));
            darkModeButtonGlyph.setFitHeight(50);
            lightModeButtonGlyph.setFitHeight(35);
        }
        else {
            appIconCreditsImageView.setImage(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/AppIcon.png")));
            lightModeButtonGlyph.setFitHeight(50);
            darkModeButtonGlyph.setFitHeight(35);
        }
        voidLink.requestFocus();
    }

    //Function used to close the main menu screen and open the graph screen in a new window
    @FXML
    private void goToGraphPage() throws IOException {
        //Preparing a new non-resizable window with a title, that displays the graph screen
        Stage graphStage = new Stage();
        graphStage.initStyle(StageStyle.UNDECORATED);
        graphStage.setTitle(language.equals("EN") ? "Evolution Graph" : language.equals("FR") ? "Graphe d'Évolution" : "Grafic de Evoluție");
        graphStage.setScene(new Scene(new FXMLLoader(getClass().getResource("AppScreens/DisplayEvolutionGraph-" + Main.language + ".fxml")).load()));
        graphStage.getScene().getStylesheets().add(Objects.requireNonNull(getClass().getResource("Stylesheets/" + Main.displayMode + "Mode.css")).toExternalForm());
        graphStage.setResizable(false);
        graphStage.centerOnScreen();
        //Opening the new graphs window
        graphStage.show();
        //Closing the current window (in this case, the main menu window)
        getCurrentStage().close();
        //Setting the graph window as the currently open window
        setCurrentStage(graphStage);
        switchTheme = new AnimatedThemeSwitcher(getCurrentStage().getScene(), new Animation(new FadeOut()).setSpeed(2.5));
        switchTheme.init();
        //Setting the app icon
        // that's going to be shown on the taskbar to the Gender Fluid free icon created by Vitaly Gorbachev,
        // published on the flaticon website
        // (https://www.flaticon.com/free-icon/gender-fluid_3369089?term=gender&related_id=3369089)
        getCurrentStage().getIcons().add(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/AppIcon.png")));
    }

    @FXML
    private void goToAnalysisPage() throws IOException {
        //Preparing a new non-resizable window with a title, that displays the analysis screen
        Stage analysisStage = new Stage();
        analysisStage.initStyle(StageStyle.UNDECORATED);
        analysisStage.setTitle(language.equals("EN") ? "Interpretations" : language.equals("FR") ? "Interprétations" : "Interpretări");
        analysisStage.setScene(new Scene(new FXMLLoader(getClass().getResource("AppScreens/Analysis-" + Main.language + ".fxml")).load()));
        analysisStage.getScene().getStylesheets().add(Objects.requireNonNull(getClass().getResource("Stylesheets/" + Main.displayMode + "Mode.css")).toExternalForm());
        analysisStage.setResizable(false);
        analysisStage.centerOnScreen();
        //Opening the new analysis window
        analysisStage.show();
        //Closing the current window (in this case, the main menu window)
        getCurrentStage().close();
        //Setting the analysis window as the currently open window
        setCurrentStage(analysisStage);
        switchTheme = new AnimatedThemeSwitcher(getCurrentStage().getScene(), new Animation(new FadeOut()).setSpeed(2.5));
        switchTheme.init();
        //Setting the app icon
        // that's going to be shown on the taskbar to the Gender Fluid free icon created by Vitaly Gorbachev,
        // published on the flaticon website
        // (https://www.flaticon.com/free-icon/gender-fluid_3369089?term=gender&related_id=3369089)
        getCurrentStage().getIcons().add(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/AppIcon.png")));
    }

    //Function used to start the routine that creates the PDF that contains the interpretations, graph with all women's salaries, men's salaries and wage gap evolutions and the dataset
    @FXML
    private void generatePDF() throws IOException {
        loadingCircleImageView.setImage(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/loading-" + displayMode + ".gif")));
        promptAnimator.setChild(new Pane(backgroundOperations));
        darkOverlayAnimator.setChild(new Pane(darkOverlay));
        backgroundOperations.setVisible(true);
        darkOverlay.setVisible(true);
        //Trying to open an already generated report PDF.
        //If there isn't one, it was generated in a different language than the current language of the application,
        // or if it was generated without including predictions when predictions exist or vice versa,
        // the report is regenerated.
        try {
            if (processData.predictionsGenerated && processData.PDFGeneratedWithPredictions && !changedLanguage && !changedCurrency || !processData.predictionsGenerated && !processData.PDFGeneratedWithPredictions && !changedLanguage && !changedCurrency) {
                //Locating an existing generated report PDF
                File existingPDF = new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Analysis.pdf");
                //Opening it
                Desktop.getDesktop().open(existingPDF);
                promptAnimator.setChild(null);
                darkOverlayAnimator.setChild(null);
                backgroundOperations.setVisible(false);
                darkOverlay.setVisible(false);
            }
            else throw new IllegalArgumentException();
        }
        catch (IllegalArgumentException e) {
            new Thread(new GeneratePDFInBackground()).start();
        }
    }

    //Function used to increase (increment) the value of the prediction field, if it is a number and if it is in the [1, 100] range
    @FXML
    private void increaseValue() {
        try {
            //Getting the value of the input field. If it's not a number, the function stops here and goes to the catch block below
            int value = Integer.parseInt(predictionField.getText());
            //If the incremented number is smaller than or equal to 100
            if (value + 1 <= 100) {
                //Setting the input field's value to the incremented number
                predictionField.setText(String.valueOf(value + 1));
                //Hiding the error, if shown
                invalidNumberWarning.setVisible(false);
            }
            //If it isn't, we show the error
            else invalidNumberWarning.setVisible(true);
        } catch (NumberFormatException e) {
            //If the value in the input prompt is not a number, we display an error
            invalidNumberWarning.setVisible(true);
        }
    }

    //Function used to decrease (decrement) the value of the prediction field, if it is a number and if it is in the [1, 100] range
    @FXML
    private void decreaseValue() {
        try {
            //Getting the value of the input field. If it's not a number, the function stops here and goes to the catch block below
            int value = Integer.parseInt(predictionField.getText());
            //If the decremented number is bigger than or equal to 0
            if (value - 1 >= 1) {
                //Setting the input field's value to the incremented number
                predictionField.setText(String.valueOf(value - 1));
                //Hiding the error, if shown
                invalidNumberWarning.setVisible(false);
            }
            //If it isn't, we show the error
            else invalidNumberWarning.setVisible(true);
        } catch (NumberFormatException e) {
            //If the value in the input prompt is not a number, we display an error
            invalidNumberWarning.setVisible(true);
        }
    }

    //Function used to hide or show the prompt which asks the user for the period of time they need wage prediction for
    @FXML
    private void togglePredictionPrompt() {
        //If the prompt is visible
        if (predictionPrompt.isVisible()) {
            promptAnimator.setChild(null);
            darkOverlayAnimator.setChild(null);
            //We hide the prompt
            predictionPrompt.setVisible(false);
            darkOverlay.setVisible(false);
            //We set the text in the prediction input field back to the default
            predictionField.setText("1");
            //We hide the invalid number warning
            invalidNumberWarning.setVisible(false);
            //Setting everything but the prompt fields and buttons to be accessible by keyboard
            dataSourcesInfo.setFocusTraversable(true);
            graphsButton.setFocusTraversable(true);
            analysisButton.setFocusTraversable(true);
            predictButton.setFocusTraversable(true);
            PDFButton.setFocusTraversable(true);
            mailButton.setFocusTraversable(true);
            languagePicker.setFocusTraversable(true);
            currencyPicker.setFocusTraversable(true);
        }
        //If the prompt is hidden
        else {
            promptAnimator.setChild(new Pane(predictionPrompt));
            darkOverlayAnimator.setChild(new Pane(darkOverlay));
            //Running the prediction function when the Enter key is pressed
            predictionPrompt.setOnKeyPressed(a -> attemptPrediction());
            //Setting everything but the prompt fields and buttons to not be accessible by keyboard
            dataSourcesInfo.setFocusTraversable(false);
            graphsButton.setFocusTraversable(false);
            analysisButton.setFocusTraversable(false);
            predictButton.setFocusTraversable(false);
            PDFButton.setFocusTraversable(false);
            mailButton.setFocusTraversable(false);
            languagePicker.setFocusTraversable(false);
            currencyPicker.setFocusTraversable(false);
            //Showing the prompt
            predictionPrompt.setVisible(true);
            darkOverlay.setVisible(true);
        }
    }

    //Function used to check whether the value of the prompt input and, if valid, trigger the prediction function in the DataProcessing class
    @FXML
    private void attemptPrediction() {
        try {
            int value = Integer.parseInt(predictionField.getText());
            if (value >= 1 && value <= 100) {
                predictionValue = Integer.parseInt(predictionField.getText());
                loadingCircleImageView.setImage(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/loading-" + displayMode + ".gif")));
                predictionPrompt.setVisible(false);
                promptAnimator.setChild(new Pane(backgroundOperations));
                backgroundOperations.setVisible(true);
                new Thread(new PredictInBackground()).start();
            } else invalidNumberWarning.setVisible(true);
        } catch (NumberFormatException e) {
            invalidNumberWarning.setVisible(true);
        } catch (IOException ignored) {}
    }

    //Function that executes the routine that deletes the generated predictions and then shows a confirmation screen with a button which reloads the main menu page
    @FXML
    private void discardPredictions() {
        processData.discardPredictions();
        //Setting everything but the prompt fields and buttons to not be accessible by keyboard
        dataSourcesInfo.setFocusTraversable(false);
        graphsButton.setFocusTraversable(false);
        analysisButton.setFocusTraversable(false);
        predictButton.setFocusTraversable(false);
        PDFButton.setFocusTraversable(false);
        mailButton.setFocusTraversable(false);
        languagePicker.setFocusTraversable(false);
        currencyPicker.setFocusTraversable(false);
        discardPredictionsButton.setVisible(false);
        promptAnimator.setChild(new Pane(discardConfirmation));
        darkOverlayAnimator.setChild(new Pane(darkOverlay));
        discardConfirmation.setVisible(true);
        darkOverlay.setVisible(true);
    }

    @FXML
    private void closeDiscardConfirmation() {
        dataSourcesInfo.setFocusTraversable(true);
        graphsButton.setFocusTraversable(true);
        analysisButton.setFocusTraversable(true);
        predictButton.setFocusTraversable(true);
        PDFButton.setFocusTraversable(true);
        mailButton.setFocusTraversable(true);
        languagePicker.setFocusTraversable(true);
        currencyPicker.setFocusTraversable(true);
        lightModeButton.setFocusTraversable(true);
        darkModeButton.setFocusTraversable(true);
        promptAnimator.setChild(null);
        darkOverlayAnimator.setChild(null);
        discardConfirmation.setVisible(false);
        darkOverlay.setVisible(false);
    }

    //Function used to hide or show the prompt which asks the user for the period of time they need wage prediction for
    @FXML
    private void toggleEmailPrompt() {
        //If the email prompt or the missing outgoing credentials prompt is visible
        if (emailPrompt.isVisible() || missingOutgoingCredentialsPrompt.isVisible()) {
            promptAnimator.setChild(null);
            darkOverlayAnimator.setChild(null);
            //We hide the prompt
            if (emailPrompt.isVisible()) {
                emailPrompt.setVisible(false);
            }
            else {
                missingOutgoingCredentialsPrompt.setVisible(false);
            }
            darkOverlay.setVisible(false);
            //We hide the invalid number warning
            invalidEmailWarning.setVisible(false);
            invalidOutgoingEmailWarning.setVisible(false);
            //Setting everything but the prompt fields and buttons to be accessible by keyboard
            dataSourcesInfo.setFocusTraversable(true);
            graphsButton.setFocusTraversable(true);
            analysisButton.setFocusTraversable(true);
            predictButton.setFocusTraversable(true);
            PDFButton.setFocusTraversable(true);
            mailButton.setFocusTraversable(true);
            languagePicker.setFocusTraversable(true);
            currencyPicker.setFocusTraversable(true);
        }
        //If the prompts are hidden
        else {
            if (outgoingAccountEmail.isEmpty() || outgoingAccountPassword.isEmpty()) {
                promptAnimator.setChild(new Pane(missingOutgoingCredentialsPrompt));
                darkOverlayAnimator.setChild(new Pane(darkOverlay));
                //Setting everything but the prompt fields and buttons to not be accessible by keyboard
                dataSourcesInfo.setFocusTraversable(false);
                graphsButton.setFocusTraversable(false);
                analysisButton.setFocusTraversable(false);
                predictButton.setFocusTraversable(false);
                PDFButton.setFocusTraversable(false);
                mailButton.setFocusTraversable(false);
                languagePicker.setFocusTraversable(false);
                currencyPicker.setFocusTraversable(false);
                //Showing the prompt
                missingOutgoingCredentialsPrompt.setVisible(true);
                darkOverlay.setVisible(true);
            }
            else {
                promptAnimator.setChild(new Pane(emailPrompt));
                darkOverlayAnimator.setChild(new Pane(darkOverlay));
                //Setting everything but the prompt fields and buttons to be accessible by keyboard
                dataSourcesInfo.setFocusTraversable(true);
                graphsButton.setFocusTraversable(true);
                analysisButton.setFocusTraversable(true);
                predictButton.setFocusTraversable(true);
                PDFButton.setFocusTraversable(true);
                mailButton.setFocusTraversable(true);
                languagePicker.setFocusTraversable(true);
                currencyPicker.setFocusTraversable(true);
                //Showing the prompt
                emailPrompt.setVisible(true);
                darkOverlay.setVisible(true);
                //Setting the value of the email field to the email address saved from a previous attempt
                if (!email.isEmpty()) emailField.setText(email);
            }
        }
    }

    @FXML
    private void attemptSendMail() throws IOException {
        EmailValidator validator = EmailValidator.getInstance();
        if (emailPrompt.isVisible()) {
            if (validator.isValid(emailField.getText())) {
                invalidEmailWarning.setVisible(false);
                email = emailField.getText();
                promptAnimator.setChild(null);
                darkOverlayAnimator.setChild(null);
                emailPrompt.setVisible(false);
                darkOverlay.setVisible(false);
                Alert confirmInclusionOfUserData = new Alert(Alert.AlertType.CONFIRMATION);
                confirmInclusionOfUserData.setTitle(language.equals("EN") ? "Inclusion of validation data" : language.equals("FR") ? "Inclusion des détails de validation" : "Includerea datelor de validare");
                confirmInclusionOfUserData.setHeaderText(language.equals("EN") ? "The data below will be attached to the email containing the report. They are data about this computer and are used to help you match the data in the email and ensure they don't come from another computer, and they won't be stored anywhere other than in the email.\nDo you agree with the inclusion of said data and have the report sent?" : language.equals("FR") ? "Les données ci-dessous vont être attachées au courriel contenant le rapport. Ils sont des données à propos de cet ordinateur, et sont utilisées pour vous aider vérifier que le courriel vient d'ici et pas d'un autre ordinateur.\nÊtes-vous d'accord avec l'inclusion de ces données et avec l'envoi du rapport?" : "Detaliile de mai jos vor fi incluse în mail. Ele sunt folosite pentru a vă ajuta să vă asigurați că mail-ul vine de aici și nu de pe alt calculator.\nSunteți de acord cu includerea acestor date și trimiterea raportului?");
                switch (language) {
                    case "EN" -> confirmInclusionOfUserData.setContentText("Username: " + System.getProperty("user.name") + "\nOperating System: " + System.getProperty("os.name") + "\nTimezone: " + System.getProperty("user.timezone"));
                    case "FR" -> confirmInclusionOfUserData.setContentText("Nom d'utilisateur: " + System.getProperty("user.name") + "\nSystème d'exploitation: " + System.getProperty("os.name") + "\nFuseau horaire: " + System.getProperty("user.timezone"));
                    case "RO" -> confirmInclusionOfUserData.setContentText("Nume de utilizator: " + System.getProperty("user.name") + "\nSistem de Operare: " + System.getProperty("os.name") + "\nFus orar: " + System.getProperty("user.timezone"));
                }
                ButtonType yesButton = new ButtonType(language.equals("EN") ? "Yes" : language.equals("FR") ? "Oui" : "Da", ButtonBar.ButtonData.YES);
                ButtonType noButton = new ButtonType(language.equals("EN") ? "No" : language.equals("FR") ? "Non" : "Nu", ButtonBar.ButtonData.NO);
                confirmInclusionOfUserData.getButtonTypes().clear();
                confirmInclusionOfUserData.getButtonTypes().add(yesButton);
                confirmInclusionOfUserData.getButtonTypes().add(noButton);
                confirmInclusionOfUserData.getDialogPane().setMaxWidth(750);
                confirmInclusionOfUserData.initStyle(StageStyle.UNDECORATED);
                confirmInclusionOfUserData.getDialogPane().getStylesheets().add(Objects.requireNonNull(getClass().getResource("Stylesheets/" + Main.displayMode + "Mode.css")).toExternalForm());
                confirmInclusionOfUserData.getDialogPane().getStyleClass().add("alerts");
                //Setting the alert icon
                // that's going to be shown on the taskbar to the Question free icon created by Roundicons,
                // published on the flaticon website
                // (https://www.flaticon.com/free-icon/question_189665?term=question&page=1&position=11&origin=search&related_id=189665)
                try {
                    ((Stage) confirmInclusionOfUserData.getDialogPane().getScene().getWindow()).getIcons().add(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/alert-confirmation.png")));
                } catch (FileNotFoundException ignored) {
                }
                Optional<ButtonType> confirmationResult = confirmInclusionOfUserData.showAndWait();
                if (confirmationResult.isPresent() && confirmationResult.get() == yesButton) {
                    loadingCircleImageView.setImage(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/loading-" + displayMode + ".gif")));
                    promptAnimator.setChild(new Pane(backgroundOperations));
                    darkOverlayAnimator.setChild(new Pane(darkOverlay));
                    backgroundOperations.setVisible(true);
                    darkOverlay.setVisible(true);
                    new Thread(new SendEmailInBackground()).start();
                }
            } else invalidEmailWarning.setVisible(true);
        }
        else if (missingOutgoingCredentialsPrompt.isVisible()) {
            if (validator.isValid(outgoingEmailField.getText())) {
                invalidOutgoingEmailWarning.setVisible(false);
                email = outgoingAccountEmail = outgoingEmailField.getText();
                outgoingAccountPassword = outgoingPasswordField.getText();
                promptAnimator.setChild(null);
                darkOverlayAnimator.setChild(null);
                missingOutgoingCredentialsPrompt.setVisible(false);
                darkOverlay.setVisible(false);
                Alert confirmInclusionOfUserData = new Alert(Alert.AlertType.CONFIRMATION);
                confirmInclusionOfUserData.setTitle(language.equals("EN") ? "Inclusion of validation data" : language.equals("FR") ? "Inclusion des détails de validation" : "Includerea datelor de validare");
                confirmInclusionOfUserData.setHeaderText(language.equals("EN") ? "The data below will be attached to the email containing the report. They are data about this computer and are used to help you match the data in the email and ensure they don't come from another computer, and they won't be stored anywhere other than in the email.\nDo you agree with the inclusion of said data and have the report sent?" : language.equals("FR") ? "Les données ci-dessous vont être attachées au courriel contenant le rapport. Ils sont des données à propos de cet ordinateur, et sont utilisées pour vous aider vérifier que le courriel vient d'ici et pas d'un autre ordinateur.\nÊtes-vous d'accord avec l'inclusion de ces données et avec l'envoi du rapport?" : "Detaliile de mai jos vor fi incluse în mail. Ele sunt folosite pentru a vă ajuta să vă asigurați că mail-ul vine de aici și nu de pe alt calculator.\nSunteți de acord cu includerea acestor date și trimiterea raportului?");
                switch (language) {
                    case "EN" -> confirmInclusionOfUserData.setContentText("Username: " + System.getProperty("user.name") + "\nOperating System: " + System.getProperty("os.name") + "\nTimezone: " + System.getProperty("user.timezone"));
                    case "FR" -> confirmInclusionOfUserData.setContentText("Nom d'utilisateur: " + System.getProperty("user.name") + "\nSystème d'exploitation: " + System.getProperty("os.name") + "\nFuseau horaire: " + System.getProperty("user.timezone"));
                    case "RO" -> confirmInclusionOfUserData.setContentText("Nume de utilizator: " + System.getProperty("user.name") + "\nSistem de Operare: " + System.getProperty("os.name") + "\nFus orar: " + System.getProperty("user.timezone"));
                }
                ButtonType yesButton = new ButtonType(language.equals("EN") ? "Yes" : language.equals("FR") ? "Oui" : "Da", ButtonBar.ButtonData.YES);
                ButtonType noButton = new ButtonType(language.equals("EN") ? "No" : language.equals("FR") ? "Non" : "Nu", ButtonBar.ButtonData.NO);
                confirmInclusionOfUserData.getButtonTypes().clear();
                confirmInclusionOfUserData.getButtonTypes().add(yesButton);
                confirmInclusionOfUserData.getButtonTypes().add(noButton);
                confirmInclusionOfUserData.getDialogPane().setMaxWidth(750);
                confirmInclusionOfUserData.initStyle(StageStyle.UNDECORATED);
                confirmInclusionOfUserData.getDialogPane().getStylesheets().add(Objects.requireNonNull(getClass().getResource("Stylesheets/" + Main.displayMode + "Mode.css")).toExternalForm());
                confirmInclusionOfUserData.getDialogPane().getStyleClass().add("alerts");
                //Setting the alert icon
                // that's going to be shown on the taskbar to the Question free icon created by Roundicons,
                // published on the flaticon website
                // (https://www.flaticon.com/free-icon/question_189665?term=question&page=1&position=11&origin=search&related_id=189665)
                try {
                    ((Stage) confirmInclusionOfUserData.getDialogPane().getScene().getWindow()).getIcons().add(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/alert-confirmation.png")));
                } catch (FileNotFoundException ignored) {
                }
                Optional<ButtonType> confirmationResult = confirmInclusionOfUserData.showAndWait();
                if (confirmationResult.isPresent() && confirmationResult.get() == yesButton) {
                    loadingCircleImageView.setImage(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/loading-" + displayMode + ".gif")));
                    promptAnimator.setChild(new Pane(backgroundOperations));
                    darkOverlayAnimator.setChild(new Pane(darkOverlay));
                    backgroundOperations.setVisible(true);
                    darkOverlay.setVisible(true);
                    new Thread(new SendEmailInBackground()).start();
                }
            } else invalidOutgoingEmailWarning.setVisible(true);
        }
    }

    @FXML
    private void toggleDataSources() {
        if (dataSources.isVisible()) {
            //Setting everything but the prompt fields and buttons to be accessible by keyboard
            dataSourcesInfo.setFocusTraversable(true);
            graphsButton.setFocusTraversable(true);
            analysisButton.setFocusTraversable(true);
            predictButton.setFocusTraversable(true);
            PDFButton.setFocusTraversable(true);
            mailButton.setFocusTraversable(true);
            languagePicker.setFocusTraversable(true);
            currencyPicker.setFocusTraversable(true);
            dataSources.setVisible(false);
            darkOverlay.setVisible(false);
            promptAnimator.setChild(null);
            darkOverlayAnimator.setChild(null);
        }
        else {
            promptAnimator.setChild(new Pane(dataSources));
            darkOverlayAnimator.setChild(new Pane(darkOverlay));
            //Setting everything but the prompt fields and buttons to not be accessible by keyboard
            dataSourcesInfo.setFocusTraversable(false);
            graphsButton.setFocusTraversable(false);
            analysisButton.setFocusTraversable(false);
            predictButton.setFocusTraversable(false);
            PDFButton.setFocusTraversable(false);
            mailButton.setFocusTraversable(false);
            languagePicker.setFocusTraversable(false);
            currencyPicker.setFocusTraversable(false);
            //Showing the prompt
            dataSources.setVisible(true);
            darkOverlay.setVisible(true);
        }
    }

    @FXML
    private void openAppIconPage() throws URISyntaxException, IOException {
        Desktop.getDesktop().browse(new URI("https://www.flaticon.com/free-icon/gender-fluid_3369089?term=gender&related_id=3369089"));
    }
    @FXML
    private void openMSEmojipediaPage() throws URISyntaxException, IOException {
        Desktop.getDesktop().browse(new URI("https://emojipedia.org/microsoft"));
    }
    @FXML
    private void openProjectGitHubPage() throws URISyntaxException, IOException {
        Desktop.getDesktop().browse(new URI("https://github.com/Alin63992/GenderGapAnalyser"));
    }
    @FXML
    private void openPreloadersWebsite() throws URISyntaxException, IOException {
        Desktop.getDesktop().browse(new URI("https://icons8.com/preloaders/"));
    }

    private boolean checkAndRecover() {
        System.out.println("Beginning application integrity check...");
        //Checking if the resources folder exists
        if (new File("src/main/resources/com/gendergapanalyser/gendergapanalyser").exists()) {
            //String variable where the root of the GitHub link is stored for easier download
            String githubRoot = "https://raw.githubusercontent.com/Alin63992/GenderGapAnalyser/master/src/main/resources/com/gendergapanalyser/gendergapanalyser/";

            //Checking if the App Screens folder exists
            File appScreensFolder = new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens");
            if (appScreensFolder.exists()) {
                //Checking if each file that should exist here does exist, and if not, trying to download it
                if (!Arrays.stream(appScreensFolder.listFiles()).toList().contains(new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/SplashScreen-EN.fxml")) || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/SplashScreen-EN.fxml").exists()) {
                    try {
                        FileUtils.copyURLToFile(URI.create(githubRoot + "AppScreens/SplashScreen-EN.fxml").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/SplashScreen-EN.fxml"), 500, 2000);
                        if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/SplashScreen-EN.fxml").exists()) {
                            if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens").exists())
                                Files.createDirectories(Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens"));
                            Files.copy(Path.of("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/SplashScreen-EN.fxml"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/SplashScreen-EN.fxml"));
                        }
                    } catch (IOException e) {
                        if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                        return false;
                    }
                }
                if (!Arrays.stream(appScreensFolder.listFiles()).toList().contains(new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/SplashScreen-FR.fxml")) || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/SplashScreen-FR.fxml").exists()) {
                    try {
                        FileUtils.copyURLToFile(URI.create(githubRoot + "AppScreens/SplashScreen-FR.fxml").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/SplashScreen-FR.fxml"), 500, 2000);
                        if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/SplashScreen-FR.fxml").exists()) {
                            if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens").exists())
                                Files.createDirectories(Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens"));
                            Files.copy(Path.of("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/SplashScreen-FR.fxml"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/SplashScreen-FR.fxml"));
                        }
                    } catch (IOException e) {
                        if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                        return false;
                    }
                }
                if (!Arrays.stream(appScreensFolder.listFiles()).toList().contains(new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/SplashScreen-RO.fxml")) || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/SplashScreen-RO.fxml").exists()) {
                    try {
                        FileUtils.copyURLToFile(URI.create(githubRoot + "AppScreens/SplashScreen-RO.fxml").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/SplashScreen-RO.fxml"), 500, 2000);
                        if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/SplashScreen-RO.fxml").exists()) {
                            if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens").exists())
                                Files.createDirectories(Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens"));
                            Files.copy(Path.of("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/SplashScreen-RO.fxml"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/SplashScreen-RO.fxml"));
                        }
                    } catch (IOException e) {
                        if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                        return false;
                    }
                }
                if (!Arrays.stream(appScreensFolder.listFiles()).toList().contains(new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/MainMenu-EN.fxml")) || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/MainMenu-EN.fxml").exists()) {
                    try {
                        FileUtils.copyURLToFile(URI.create(githubRoot + "AppScreens/MainMenu-EN.fxml").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/MainMenu-EN.fxml"), 500, 2000);
                        if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/MainMenu-EN.fxml").exists()) {
                            if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens").exists())
                                Files.createDirectories(Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens"));
                            Files.copy(Path.of("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/MainMenu-EN.fxml"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/MainMenu-EN.fxml"));
                        }
                    } catch (IOException e) {
                        if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                        return false;
                    }
                }
                if (!Arrays.stream(appScreensFolder.listFiles()).toList().contains(new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/MainMenu-FR.fxml")) || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/MainMenu-FR.fxml").exists()) {
                    try {
                        FileUtils.copyURLToFile(URI.create(githubRoot + "AppScreens/MainMenu-FR.fxml").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/MainMenu-FR.fxml"), 500, 2000);
                        if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/MainMenu-FR.fxml").exists()) {
                            if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens").exists())
                                Files.createDirectories(Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens"));
                            Files.copy(Path.of("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/MainMenu-FR.fxml"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/MainMenu-FR.fxml"));
                        }
                    } catch (IOException e) {
                        if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                        return false;
                    }
                }
                if (!Arrays.stream(appScreensFolder.listFiles()).toList().contains(new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/MainMenu-RO.fxml")) || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/MainMenu-RO.fxml").exists()) {
                    try {
                        FileUtils.copyURLToFile(URI.create(githubRoot + "AppScreens/MainMenu-RO.fxml").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/MainMenu-RO.fxml"), 500, 2000);
                        if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/MainMenu-RO.fxml").exists()) {
                            if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens").exists())
                                Files.createDirectories(Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens"));
                            Files.copy(Path.of("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/MainMenu-RO.fxml"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/MainMenu-RO.fxml"));
                        }
                    } catch (IOException e) {
                        if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                        return false;
                    }
                }
                if (!Arrays.stream(appScreensFolder.listFiles()).toList().contains(new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/DisplayEvolutionGraph-EN.fxml")) || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/DisplayEvolutionGraph-EN.fxml").exists()) {
                    try {
                        FileUtils.copyURLToFile(URI.create(githubRoot + "AppScreens/DisplayEvolutionGraph-EN.fxml").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/DisplayEvolutionGraph-EN.fxml"), 500, 2000);
                        if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/DisplayEvolutionGraph-EN.fxml").exists()) {
                            if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens").exists())
                                Files.createDirectories(Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens"));
                            Files.copy(Path.of("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/DisplayEvolutionGraph-EN.fxml"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/DisplayEvolutionGraph-EN.fxml"));
                        }
                    } catch (IOException e) {
                        if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                        return false;
                    }
                }
                if (!Arrays.stream(appScreensFolder.listFiles()).toList().contains(new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/DisplayEvolutionGraph-FR.fxml")) || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/DisplayEvolutionGraph-FR.fxml").exists()) {
                    try {
                        FileUtils.copyURLToFile(URI.create(githubRoot + "AppScreens/DisplayEvolutionGraph-FR.fxml").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/DisplayEvolutionGraph-FR.fxml"), 500, 2000);
                        if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/DisplayEvolutionGraph-FR.fxml").exists()) {
                            if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens").exists())
                                Files.createDirectories(Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens"));
                            Files.copy(Path.of("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/DisplayEvolutionGraph-FR.fxml"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/DisplayEvolutionGraph-FR.fxml"));
                        }
                    } catch (IOException e) {
                        if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                        return false;
                    }
                }
                if (!Arrays.stream(appScreensFolder.listFiles()).toList().contains(new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/DisplayEvolutionGraph-RO.fxml")) || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/DisplayEvolutionGraph-RO.fxml").exists()) {
                    try {
                        FileUtils.copyURLToFile(URI.create(githubRoot + "AppScreens/DisplayEvolutionGraph-RO.fxml").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/DisplayEvolutionGraph-RO.fxml"), 500, 2000);
                        if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/DisplayEvolutionGraph-RO.fxml").exists()) {
                            if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens").exists())
                                Files.createDirectories(Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens"));
                            Files.copy(Path.of("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/DisplayEvolutionGraph-RO.fxml"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/DisplayEvolutionGraph-RO.fxml"));
                        }
                    } catch (IOException e) {
                        if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                        return false;
                    }
                }
                if (!Arrays.stream(appScreensFolder.listFiles()).toList().contains(new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/Analysis-EN.fxml")) || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/Analysis-EN.fxml").exists()) {
                    try {
                        FileUtils.copyURLToFile(URI.create(githubRoot + "AppScreens/Analysis-EN.fxml").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/Analysis-EN.fxml"), 500, 2000);
                        if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/Analysis-EN.fxml").exists()) {
                            if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens").exists())
                                Files.createDirectories(Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens"));
                            Files.copy(Path.of("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/Analysis-EN.fxml"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/Analysis-EN.fxml"));
                        }
                    } catch (IOException e) {
                        if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                        return false;
                    }
                }
                if (!Arrays.stream(appScreensFolder.listFiles()).toList().contains(new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/Analysis-FR.fxml")) || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/Analysis-FR.fxml").exists()) {
                    try {
                        FileUtils.copyURLToFile(URI.create(githubRoot + "AppScreens/Analysis-FR.fxml").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/Analysis-FR.fxml"), 500, 2000);
                        if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/Analysis-FR.fxml").exists()) {
                            if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens").exists())
                                Files.createDirectories(Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens"));
                            Files.copy(Path.of("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/Analysis-FR.fxml"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/Analysis-FR.fxml"));
                        }
                    } catch (IOException e) {
                        if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                        return false;
                    }
                }
                if (!Arrays.stream(appScreensFolder.listFiles()).toList().contains(new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/Analysis-RO.fxml")) || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/Analysis-RO.fxml").exists()) {
                    try {
                        FileUtils.copyURLToFile(URI.create(githubRoot + "AppScreens/Analysis-RO.fxml").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/Analysis-RO.fxml"), 500, 2000);
                        if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/Analysis-RO.fxml").exists()) {
                            if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens").exists())
                                Files.createDirectories(Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens"));
                            Files.copy(Path.of("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/Analysis-RO.fxml"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/Analysis-RO.fxml"));
                        }
                    } catch (IOException e) {
                        if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                        return false;
                    }
                }
            } else {
                try {
                    //Creating the App Screens folder and downloading all the files that should be in it
                    Files.createDirectories(Path.of("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens"));
                    Files.createDirectories(Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens"));
                    FileUtils.copyURLToFile(URI.create(githubRoot + "AppScreens/SplashScreen-EN.fxml").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/SplashScreen-EN.fxml"), 500, 2000);
                    if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/SplashScreen-EN.fxml").exists())
                        Files.copy(Path.of("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/SplashScreen-EN.fxml"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/SplashScreen-EN.fxml"));
                    FileUtils.copyURLToFile(URI.create(githubRoot + "AppScreens/SplashScreen-FR.fxml").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/SplashScreen-FR.fxml"), 500, 2000);
                    if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/SplashScreen-FR.fxml").exists())
                        Files.copy(Path.of("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/SplashScreen-FR.fxml"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/SplashScreen-FR.fxml"));
                    FileUtils.copyURLToFile(URI.create(githubRoot + "AppScreens/SplashScreen-RO.fxml").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/SplashScreen-RO.fxml"), 500, 2000);
                    if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/SplashScreen-RO.fxml").exists())
                        Files.copy(Path.of("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/SplashScreen-RO.fxml"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/SplashScreen-RO.fxml"));
                    FileUtils.copyURLToFile(URI.create(githubRoot + "AppScreens/MainMenu-EN.fxml").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/MainMenu-EN.fxml"), 500, 2000);
                    if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/MainMenu-EN.fxml").exists())
                        Files.copy(Path.of("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/MainMenu-EN.fxml"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/MainMenu-EN.fxml"));
                    FileUtils.copyURLToFile(URI.create(githubRoot + "AppScreens/MainMenu-FR.fxml").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/MainMenu-FR.fxml"), 500, 2000);
                    if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/MainMenu-FR.fxml").exists())
                        Files.copy(Path.of("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/MainMenu-FR.fxml"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/MainMenu-FR.fxml"));
                    FileUtils.copyURLToFile(URI.create(githubRoot + "AppScreens/MainMenu-RO.fxml").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/MainMenu-RO.fxml"), 500, 2000);
                    if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/MainMenu-RO.fxml").exists())
                        Files.copy(Path.of("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/MainMenu-RO.fxml"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/MainMenu-RO.fxml"));
                    FileUtils.copyURLToFile(URI.create(githubRoot + "AppScreens/DisplayEvolutionGraph-EN.fxml").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/DisplayEvolutionGraph-EN.fxml"), 500, 2000);
                    if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/DisplayEvolutionGraph-EN.fxml").exists())
                        Files.copy(Path.of("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/DisplayEvolutionGraph-EN.fxml"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/DisplayEvolutionGraph-EN.fxml"));
                    FileUtils.copyURLToFile(URI.create(githubRoot + "AppScreens/DisplayEvolutionGraph-FR.fxml").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/DisplayEvolutionGraph-FR.fxml"), 500, 2000);
                    if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/DisplayEvolutionGraph-FR.fxml").exists())
                        Files.copy(Path.of("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/DisplayEvolutionGraph-FR.fxml"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/DisplayEvolutionGraph-FR.fxml"));
                    FileUtils.copyURLToFile(URI.create(githubRoot + "AppScreens/DisplayEvolutionGraph-RO.fxml").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/DisplayEvolutionGraph-RO.fxml"), 500, 2000);
                    if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/DisplayEvolutionGraph-RO.fxml").exists())
                        Files.copy(Path.of("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/DisplayEvolutionGraph-RO.fxml"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/DisplayEvolutionGraph-RO.fxml"));
                    FileUtils.copyURLToFile(URI.create(githubRoot + "AppScreens/Analysis-EN.fxml").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/Analysis-EN.fxml"), 500, 2000);
                    if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/Analysis-EN.fxml").exists())
                        Files.copy(Path.of("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/Analysis-EN.fxml"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/Analysis-EN.fxml"));
                    FileUtils.copyURLToFile(URI.create(githubRoot + "AppScreens/Analysis-FR.fxml").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/Analysis-FR.fxml"), 500, 2000);
                    if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/Analysis-FR.fxml").exists())
                        Files.copy(Path.of("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/Analysis-FR.fxml"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/Analysis-FR.fxml"));
                    FileUtils.copyURLToFile(URI.create(githubRoot + "AppScreens/Analysis-RO.fxml").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/Analysis-RO.fxml"), 500, 2000);
                    if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/Analysis-RO.fxml").exists())
                        Files.copy(Path.of("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/Analysis-RO.fxml"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/Analysis-RO.fxml"));
                } catch (IOException e) {
                    if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                    return false;
                }
            }

            //Checking if the Glyphs folder exists
            File glyphsFolder = new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs");
            if (glyphsFolder.exists()) {
                //Checking if the Emojis folder exists
                File emojisFolder = new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis");
                if (emojisFolder.exists()) {
                    //Checking if each file that should exist here does exist, and if not, trying to download it
                    if (!Arrays.stream(emojisFolder.listFiles()).toList().contains(new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Black_Rightwards_Arrow.png"))) {
                        try {
                            FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Emojis/Black_Rightwards_Arrow.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Black_Rightwards_Arrow.png"), 500, 2000);
                        } catch (IOException e) {
                            if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                            return false;
                        }
                    }
                    if (!Arrays.stream(emojisFolder.listFiles()).toList().contains(new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Black_Sun_with_Rays.png"))) {
                        try {
                            FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Emojis/Black_Sun_with_Rays.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Black_Sun_with_Rays.png"), 500, 2000);
                        } catch (IOException e) {
                            if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                            return false;
                        }
                    }
                    if (!Arrays.stream(emojisFolder.listFiles()).toList().contains(new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Calendar.png"))) {
                        try {
                            FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Emojis/Calendar.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Calendar.png"), 500, 2000);
                        } catch (IOException e) {
                            if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                            return false;
                        }
                    }
                    if (!Arrays.stream(emojisFolder.listFiles()).toList().contains(new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Chart_with_Upwards_Trend.png"))) {
                        try {
                            FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Emojis/Chart_with_Upwards_Trend.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Chart_with_Upwards_Trend.png"), 500, 2000);
                        } catch (IOException e) {
                            if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                            return false;
                        }
                    }
                    if (!Arrays.stream(emojisFolder.listFiles()).toList().contains(new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Crescent_Moon.png"))) {
                        try {
                            FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Emojis/Crescent_Moon.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Crescent_Moon.png"), 500, 2000);
                        } catch (IOException e) {
                            if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                            return false;
                        }
                    }
                    if (!Arrays.stream(emojisFolder.listFiles()).toList().contains(new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Crystal_Ball.png"))) {
                        try {
                            FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Emojis/Crystal_Ball.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Crystal_Ball.png"), 500, 2000);
                        } catch (IOException e) {
                            if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                            return false;
                        }
                    }
                    if (!Arrays.stream(emojisFolder.listFiles()).toList().contains(new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/E-Mail_Symbol.png"))) {
                        try {
                            FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Emojis/E-Mail_Symbol.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/E-Mail_Symbol.png"), 500, 2000);
                        } catch (IOException e) {
                            if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                            return false;
                        }
                    }
                    if (!Arrays.stream(emojisFolder.listFiles()).toList().contains(new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Female_Sign.png"))) {
                        try {
                            FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Emojis/Female_Sign.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Female_Sign.png"), 500, 2000);
                        } catch (IOException e) {
                            if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                            return false;
                        }
                    }
                    if (!Arrays.stream(emojisFolder.listFiles()).toList().contains(new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Globe_with_Meridians.png"))) {
                        try {
                            FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Emojis/Globe_with_Meridians.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Globe_with_Meridians.png"), 500, 2000);
                        } catch (IOException e) {
                            if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                            return false;
                        }
                    }
                    if (!Arrays.stream(emojisFolder.listFiles()).toList().contains(new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Information_Source.png"))) {
                        try {
                            FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Emojis/Information_Source.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Information_Source.png"), 500, 2000);
                        } catch (IOException e) {
                            if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                            return false;
                        }
                    }
                    if (!Arrays.stream(emojisFolder.listFiles()).toList().contains(new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Leftwards_Black_Arrow.png"))) {
                        try {
                            FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Emojis/Leftwards_Black_Arrow.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Leftwards_Black_Arrow.png"), 500, 2000);
                        } catch (IOException e) {
                            if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                            return false;
                        }
                    }
                    if (!Arrays.stream(emojisFolder.listFiles()).toList().contains(new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Male_Sign.png"))) {
                        try {
                            FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Emojis/Male_Sign.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Male_Sign.png"), 500, 2000);
                        } catch (IOException e) {
                            if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                            return false;
                        }
                    }
                    if (!Arrays.stream(emojisFolder.listFiles()).toList().contains(new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Memo.png"))) {
                        try {
                            FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Emojis/Memo.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Memo.png"), 500, 2000);
                        } catch (IOException e) {
                            if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                            return false;
                        }
                    }
                    if (!Arrays.stream(emojisFolder.listFiles()).toList().contains(new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Page_Facing_Up.png"))) {
                        try {
                            FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Emojis/Page_Facing_Up.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Page_Facing_Up.png"), 500, 2000);
                        } catch (IOException e) {
                            if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                            return false;
                        }
                    }
                } else {
                    try {
                        //Creating the Glyphs/Emojis folder and downloading all the files that should be in it
                        Files.createDirectories(Path.of("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/"));
                        FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Emojis/Black_Rightwards_Arrow.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Black_Rightwards_Arrow.png"), 500, 2000);
                        FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Emojis/Black_Sun_with_Rays.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Black_Sun_with_Rays.png"), 500, 2000);
                        FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Emojis/Calendar.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Calendar.png"), 500, 2000);
                        FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Emojis/Chart_with_Upwards_Trend.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Chart_with_Upwards_Trend.png"), 500, 2000);
                        FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Emojis/Crescent_Moon.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Crescent_Moon.png"), 500, 2000);
                        FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Emojis/Crystal_Ball.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Crystal_Ball.png"), 500, 2000);
                        FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Emojis/E-Mail_Symbol.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/E-Mail_Symbol.png"), 500, 2000);
                        FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Emojis/Female_Sign.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Female_Sign.png"), 500, 2000);
                        FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Emojis/Globe_with_Meridians.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Globe_with_Meridians.png"), 500, 2000);
                        FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Emojis/Information_Source.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Information_Source.png"), 500, 2000);
                        FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Emojis/Leftwards_Black_Arrow.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Leftwards_Black_Arrow.png"), 500, 2000);
                        FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Emojis/Male_Sign.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Male_Sign.png"), 500, 2000);
                        FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Emojis/Memo.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Memo.png"), 500, 2000);
                        FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Emojis/Page_Facing_Up.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Page_Facing_Up.png"), 500, 2000);
                    } catch (IOException e) {
                        if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                        return false;
                    }
                }

                //Checking if the Miscellaneous folder exists
                File miscellaneousFolder = new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous");
                if (miscellaneousFolder.exists()) {
                    //Checking if each file that should exist here does exist, and if not, trying to download it
                    if (!Arrays.stream(miscellaneousFolder.listFiles()).toList().contains(new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/alert-confirmation.png"))) {
                        try {
                            FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Miscellaneous/alert-confirmation.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/alert-confirmation.png"), 500, 2000);
                        } catch (IOException e) {
                            if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                            return false;
                        }
                    }
                    if (!Arrays.stream(miscellaneousFolder.listFiles()).toList().contains(new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/alert-error.png"))) {
                        try {
                            FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Miscellaneous/alert-error.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/alert-error.png"), 500, 2000);
                        } catch (IOException e) {
                            if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                            return false;
                        }
                    }
                    if (!Arrays.stream(miscellaneousFolder.listFiles()).toList().contains(new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/alert-information.png"))) {
                        try {
                            FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Miscellaneous/alert-information.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/alert-information.png"), 500, 2000);
                        } catch (IOException e) {
                            if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                            return false;
                        }
                    }
                    if (!Arrays.stream(miscellaneousFolder.listFiles()).toList().contains(new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/AppIcon.png"))) {
                        try {
                            FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Miscellaneous/AppIcon.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/AppIcon.png"), 500, 2000);
                        } catch (IOException e) {
                            if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                            return false;
                        }
                    }
                    if (!Arrays.stream(miscellaneousFolder.listFiles()).toList().contains(new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/AppIcon-Dark.png"))) {
                        try {
                            FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Miscellaneous/AppIcon-Dark.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/AppIcon-Dark.png"), 500, 2000);
                        } catch (IOException e) {
                            if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                            return false;
                        }
                    }
                    if (!Arrays.stream(miscellaneousFolder.listFiles()).toList().contains(new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/ExchangeRate-API-Logo-Dark.png"))) {
                        try {
                            FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Miscellaneous/ExchangeRate-API-Logo-Dark.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/ExchangeRate-API-Logo-Dark.png"), 500, 2000);
                        } catch (IOException e) {
                            if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                            return false;
                        }
                    }
                    if (!Arrays.stream(miscellaneousFolder.listFiles()).toList().contains(new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/ExchangeRate-API-Logo-Light.png"))) {
                        try {
                            FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Miscellaneous/ExchangeRate-API-Logo-Light.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/ExchangeRate-API-Logo-Light.png"), 500, 2000);
                        } catch (IOException e) {
                            if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                            return false;
                        }
                    }
                    if (!Arrays.stream(miscellaneousFolder.listFiles()).toList().contains(new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/loading-Dark.gif"))) {
                        try {
                            FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Miscellaneous/loading-Dark.gif").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/loading-Dark.gif"), 500, 2000);
                        } catch (IOException e) {
                            if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                            return false;
                        }
                    }
                    if (!Arrays.stream(miscellaneousFolder.listFiles()).toList().contains(new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/loading-Light.gif"))) {
                        try {
                            FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Miscellaneous/loading-Light.gif").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/loading-Light.gif"), 500, 2000);
                        } catch (IOException e) {
                            if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                            return false;
                        }
                    }
                    if (!Arrays.stream(miscellaneousFolder.listFiles()).toList().contains(new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/US-Dept-of-Labor-Logo.png"))) {
                        try {
                            FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Miscellaneous/US-Dept-of-Labor-Logo.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/US-Dept-of-Labor-Logo.png"), 500, 2000);
                        } catch (IOException e) {
                            if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                            return false;
                        }
                    }
                } else {
                    try {
                        //Creating the Glyphs/Miscellaneous folder and downloading all the files that should be in it
                        Files.createDirectories(Path.of("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous"));
                        FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Miscellaneous/alert-confirmation.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/alert-confirmation.png"), 500, 2000);
                        FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Miscellaneous/alert-error.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/alert-error.png"), 500, 2000);
                        FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Miscellaneous/alert-information.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/alert-information.png"), 500, 2000);
                        FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Miscellaneous/AppIcon.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/AppIcon.png"), 500, 2000);
                        FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Miscellaneous/AppIcon-Dark.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/AppIcon-Dark.png"), 500, 2000);
                        FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Miscellaneous/ExchangeRate-API-Logo-Dark.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/ExchangeRate-API-Logo-Dark.png"), 500, 2000);
                        FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Miscellaneous/ExchangeRate-API-Logo-Light.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/ExchangeRate-API-Logo-Light.png"), 500, 2000);
                        FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Miscellaneous/loading-Dark.gif").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/loading-Dark.gif"), 500, 2000);
                        FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Miscellaneous/loading-Light.gif").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/loading-Light.gif"), 500, 2000);
                        FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Miscellaneous/US-Dept--of-Labor-Logo.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/US-Dept--of-Labor-Logo.png"), 500, 2000);
                    } catch (IOException e) {
                        if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                        return false;
                    }
                }
            } else {
                try {
                    //Creating the Glyphs and the Miscellaneous folder and downloading all the files that should be in it
                    Files.createDirectories(Path.of("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous"));
                    FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Miscellaneous/alert-confirmation.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/alert-confirmation.png"), 500, 2000);
                    FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Miscellaneous/alert-error.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/alert-error.png"), 500, 2000);
                    FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Miscellaneous/alert-information.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/alert-information.png"), 500, 2000);
                    FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Miscellaneous/AppIcon.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/AppIcon.png"), 500, 2000);
                    FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Miscellaneous/AppIcon-Dark.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/AppIcon-Dark.png"), 500, 2000);
                    FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Miscellaneous/ExchangeRate-API-Logo-Dark.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/ExchangeRate-API-Logo-Dark.png"), 500, 2000);
                    FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Miscellaneous/ExchangeRate-API-Logo-Light.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/ExchangeRate-API-Logo-Light.png"), 500, 2000);
                    FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Miscellaneous/loading-Dark.gif").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/loading-Dark.gif"), 500, 2000);
                    FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Miscellaneous/loading-Light.gif").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/loading-Light.gif"), 500, 2000);
                    FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Miscellaneous/US-Dept--of-Labor-Logo.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/US-Dept--of-Labor-Logo.png"), 500, 2000);
                    //Creating the Glyphs/Emojis folder and downloading the PNGs that are in it
                    Files.createDirectories(Path.of("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/"));
                    FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Emojis/Black_Rightwards_Arrow.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Black_Rightwards_Arrow.png"), 500, 2000);
                    FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Emojis/Black_Sun_with_Rays.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Black_Sun_with_Rays.png"), 500, 2000);
                    FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Emojis/Calendar.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Calendar.png"), 500, 2000);
                    FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Emojis/Chart_with_Upwards_Trend.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Chart_with_Upwards_Trend.png"), 500, 2000);
                    FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Emojis/Crescent_Moon.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Crescent_Moon.png"), 500, 2000);
                    FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Emojis/Crystal_Ball.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Crystal_Ball.png"), 500, 2000);
                    FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Emojis/E-Mail_Symbol.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/E-Mail_Symbol.png"), 500, 2000);
                    FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Emojis/Female_Sign.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Female_Sign.png"), 500, 2000);
                    FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Emojis/Globe_with_Meridians.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Globe_with_Meridians.png"), 500, 2000);
                    FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Emojis/Information_Source.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Information_Source.png"), 500, 2000);
                    FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Emojis/Leftwards_Black_Arrow.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Leftwards_Black_Arrow.png"), 500, 2000);
                    FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Emojis/Male_Sign.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Male_Sign.png"), 500, 2000);
                    FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Emojis/Memo.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Memo.png"), 500, 2000);
                    FileUtils.copyURLToFile(URI.create(githubRoot + "Glyphs/Emojis/Page_Facing_Up.png").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Emojis/Page_Facing_Up.png"), 500, 2000);
                } catch (IOException e) {
                    if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                    return false;
                }
            }

            //Checking if the Stylesheets folder exists
            File stylesheetsFolder = new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Stylesheets");
            if (stylesheetsFolder.exists()) {
                //Checking if each file that should exist here does exist, and if not, trying to download it
                if (!Arrays.stream(stylesheetsFolder.listFiles()).toList().contains(new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Stylesheets/DarkMode.css")) || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/Stylesheets/DarkMode.css").exists()) {
                    try {
                        FileUtils.copyURLToFile(URI.create(githubRoot + "Stylesheets/DarkMode.css").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Stylesheets/DarkMode.css"), 500, 2000);
                        if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/Stylesheets/DarkMode.css").exists()) {
                            if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/Stylesheets").exists())
                                Files.createDirectories(Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/Stylesheets"));
                            Files.copy(Path.of("src/main/resources/com/gendergapanalyser/gendergapanalyser/Stylesheets/DarkMode.css"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/Stylesheets/DarkMode.css"));
                        }
                    } catch (IOException e) {
                        if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                        return false;
                    }
                }
                if (!Arrays.stream(stylesheetsFolder.listFiles()).toList().contains(new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Stylesheets/LightMode.css")) || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/Stylesheets/LightMode.css").exists()) {
                    try {
                        FileUtils.copyURLToFile(URI.create(githubRoot + "Stylesheets/LightMode.css").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Stylesheets/LightMode.css"), 500, 2000);
                        if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/Stylesheets/LightMode.css").exists()) {
                            if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/Stylesheets/").exists())
                                Files.createDirectories(Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/Stylesheets"));
                            Files.copy(Path.of("src/main/resources/com/gendergapanalyser/gendergapanalyser/Stylesheets/LightMode.css"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/Stylesheets/LightMode.css"));
                        }
                    } catch (IOException e) {
                        if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                        return false;
                    }
                }
            } else {
                try {
                    //Creating the Stylesheets folder and downloading all the files that should be in it
                    Files.createDirectories(Path.of("src/main/resources/com/gendergapanalyser/gendergapanalyser/Stylesheets"));
                    Files.createDirectories(Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/Stylesheets"));
                    FileUtils.copyURLToFile(URI.create(githubRoot + "Stylesheets/DarkMode.css").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Stylesheets/DarkMode.css"), 500, 2000);
                    if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/Stylesheets/DarkMode.css").exists())
                        Files.copy(Path.of("src/main/resources/com/gendergapanalyser/gendergapanalyser/Stylesheets/DarkMode.css"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/Stylesheets/DarkMode.css"));
                    FileUtils.copyURLToFile(URI.create(githubRoot + "Stylesheets/LightMode.css").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Stylesheets/LightMode.css"), 500, 2000);
                    if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/Stylesheets/LightMode.css").exists())
                        Files.copy(Path.of("src/main/resources/com/gendergapanalyser/gendergapanalyser/Stylesheets/LightMode.css"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/Stylesheets/LightMode.css"));
                } catch (IOException e) {
                    if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                    return false;
                }
            }

            //Checking if the fallback dataset CSV file exists, and if not, trying to download it
            if (!new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/FallbackDataset.csv").exists()) {
                try {
                    FileUtils.copyURLToFile(URI.create(githubRoot + "FallbackDataset.csv").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/FallbackDataset.csv"), 500, 10000);
                } catch (IOException e) {
                    if (e instanceof FileNotFoundException) fileNotFoundOnGit = true;
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void start(Stage primaryStage) {
        Runnable appLoad = () -> {
            try {
                if (checkAndRecover()) {
                    System.out.println("Application integrity check complete. Application is healthy, and it can start.");
                    //Trying to download the dataset file from the U.S. Department of Labor server
                    downloadDataset.start();

                    //Checking  if a day has passed since last downloading exchange rates
                    exchangeRateLastUpdated.add(GregorianCalendar.DAY_OF_MONTH, 1);
                    //If a day did pass
                    if (exchangeRateLastUpdated.get(GregorianCalendar.DAY_OF_MONTH) < LocalDate.now().getDayOfMonth() || exchangeRateLastUpdated.get(GregorianCalendar.MONTH) <= LocalDate.now().getMonthValue() || exchangeRateLastUpdated.get(GregorianCalendar.YEAR) <= LocalDate.now().getYear()) {
                        //Preparing to connect to the ExchangeRate-API to obtain new exchange rates
                        HttpURLConnection connection = (HttpURLConnection) new URI("https://v6.exchangerate-api.com/v6/9a9fc15f7944c0cb9bf532a8/latest/USD").toURL().openConnection();
                        connection.setConnectTimeout(500);
                        connection.setReadTimeout(1000);
                        connection.addRequestProperty("User-Agent", "Mozilla/5.0");
                        //Attempting to connect (hoping that the computer is connected to the internet)
                        try {
                            connection.connect();
                            //Saving the JSON response
                            BufferedReader br = new BufferedReader(new InputStreamReader((InputStream) (connection.getResponseCode() == 200 ? connection.getContent() : connection.getErrorStream())));
                            ArrayList<String> json = new ArrayList<>();
                            String output;
                            while ((output = br.readLine()) != null)
                                json.add(output);
                            if (json.size() > 1 && json.get(1).contains("\"result\":\"success\"")) {
                                for (String jsonPart : json) {
                                    if (jsonPart.contains("\"EUR\""))
                                        exchangeRateEUR = Double.parseDouble(jsonPart.split(":")[1].substring(0, jsonPart.split(":")[1].length() - 1));
                                    else if (jsonPart.contains("\"RON\"")) {
                                        exchangeRateRON = Double.parseDouble(jsonPart.split(":")[1].substring(0, jsonPart.split(":")[1].length() - 1));
                                        break;
                                    }
                                }
                                //Setting the current date as the date of last update
                                exchangeRateLastUpdated.set(GregorianCalendar.DAY_OF_MONTH, LocalDate.now().getDayOfMonth());
                                exchangeRateLastUpdated.set(GregorianCalendar.MONTH, LocalDate.now().getMonthValue());
                                exchangeRateLastUpdated.set(GregorianCalendar.YEAR, LocalDate.now().getYear());

                                //Rebuilding the user settings file with the new currency values
                                BufferedWriter buildUserSettings = new BufferedWriter(new FileWriter("src/main/resources/com/gendergapanalyser/gendergapanalyser/UserSettings.txt"));
                                buildUserSettings.write("DisplayMode=" + displayMode + "\nLanguage=" + language + "\nCurrency=" + currency + "\nExchangeRateLastUpdated=" + exchangeRateLastUpdated.get(Calendar.DAY_OF_MONTH) + "." + exchangeRateLastUpdated.get(Calendar.MONTH) + "." + exchangeRateLastUpdated.get(Calendar.YEAR) + "\nExchangeRateToEUR=" + exchangeRateEUR + "\nExchangeRateToRON=" + exchangeRateRON);
                                buildUserSettings.close();
                            }
                        } catch (IOException ignored) {}
                    } else {
                        //Reverting the change made to the date so that the application does not use the wrong date
                        exchangeRateLastUpdated.add(GregorianCalendar.DAY_OF_MONTH, -1);
                    }

                    //Preparing the dataset and creating the plots
                    processData = new DataProcessing();
                    processData.prepareData();

                    //Switching to the main menu page because the app loading is done
                    Platform.runLater(() -> {
                        //Setting the window title
                        getCurrentStage().setTitle(language.equals("EN") ? "Main Menu" : language.equals("FR") ? "Menu Principal" : "Meniu Principal");
                        AnimatedSwitcher as = new AnimatedSwitcher();
                        as.setIn(new Animation(new ZoomIn()).setSpeed(1.3));
                        Scene scene = new Scene(new Pane(as));
                        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("Stylesheets/" + Main.displayMode + "Mode.css")).toExternalForm());
                        as.of(Main.getCurrentStage().getScene().getRoot());
                        try {
                            as.setChild(new FXMLLoader(getClass().getResource("AppScreens/MainMenu-" + Main.language + ".fxml")).load());
                        } catch (IOException ignored) {
                        }
                        getCurrentStage().setScene(scene);
                        switchTheme = new AnimatedThemeSwitcher(getCurrentStage().getScene(), new Animation(new FadeOut()).setSpeed(2.5));
                        switchTheme.init();
                        getCurrentStage().centerOnScreen();
                    });
                }
                else {
                    System.out.println("Application integrity check complete. Application is missing essential files! Startup aborted!");
                    Platform.runLater(() -> {
                        Alert applicationError = new Alert(Alert.AlertType.ERROR);
                        applicationError.setTitle("Application Error");
                        applicationError.setHeaderText("Severe Application Error!");
                        getCurrentStage().close();
                        if (!new File("src/main/resources/com/gendergapanalyser/gendergapanalyser").exists() || fileNotFoundOnGit) {
                            ButtonType goToGitHub = new ButtonType("Go to GitHub", ButtonBar.ButtonData.LEFT);
                            applicationError.setContentText("The application cannot start because one or more files required for it to run are missing and online recovery is impossible because " + (fileNotFoundOnGit ? "the missing files were not found on GitHub" : "the application's internal folder structure is broken") + ".\nPlease click the \"Go to GitHub\" button below to download the app again and get back on track.");
                            applicationError.getButtonTypes().add(goToGitHub);
                            applicationError.getDialogPane().setMaxWidth(750);
                            Optional<ButtonType> choice = applicationError.showAndWait();
                            if (choice.isPresent() && choice.get() == goToGitHub) {
                                try {
                                    openProjectGitHubPage();
                                } catch (IOException | URISyntaxException ignored) {
                                }
                            }
                        }
                        else {
                            applicationError.setContentText("The application cannot start because one or more files required for it to run are missing and they couldn't be downloaded right now.\nPlease check that you're connected to the internet or wait for a few minutes and start the application again.");
                            applicationError.show();
                        }
                    });
                }
            } catch (IOException | URISyntaxException ignored) {}
        };
        try {
            //Loading user settings (display mode and app language) from the UserSettings.txt file
            try {
                BufferedReader loadUserSettings = new BufferedReader(new FileReader("src/main/resources/com/gendergapanalyser/gendergapanalyser/UserSettings.txt"));
                String setting;
                while ((setting = loadUserSettings.readLine()) != null) {
                    String[] settingParts = setting.split("=");
                    switch (settingParts[0]) {
                        case "DisplayMode" -> displayMode = settingParts[1];
                        case "Language" -> language = settingParts[1];
                        case "Currency" -> currency = settingParts[1];
                        case "ExchangeRateLastUpdated" -> {
                            exchangeRateLastUpdated.set(GregorianCalendar.DAY_OF_MONTH, Integer.parseInt(settingParts[1].split("\\.")[0]));
                            exchangeRateLastUpdated.set(GregorianCalendar.MONTH, Integer.parseInt(settingParts[1].split("\\.")[1]));
                            exchangeRateLastUpdated.set(GregorianCalendar.YEAR, Integer.parseInt(settingParts[1].split("\\.")[2]));
                        }
                        case "ExchangeRateToEUR" -> exchangeRateEUR = Double.parseDouble(settingParts[1]);
                        case "ExchangeRateToRON" -> exchangeRateRON = Double.parseDouble(settingParts[1]);
                    }
                }
                loadUserSettings.close();
            } catch (IOException e) {
                //Setting the exchange rate last updated date 2 days back of the current date because, before updating,
                // a day is added to the last updated day,
                // so that way,
                // the date is still behind the current day and the exchange rates are downloaded
                exchangeRateLastUpdated.add(GregorianCalendar.DAY_OF_MONTH, -2);
                BufferedWriter buildUserSettings = new BufferedWriter(new FileWriter("src/main/resources/com/gendergapanalyser/gendergapanalyser/UserSettings.txt"));
                buildUserSettings.write("DisplayMode=" + displayMode + "\nLanguage=" + language + "\nCurrency=" + currency + "\nExchangeRateLastUpdated=" + exchangeRateLastUpdated.get(Calendar.DAY_OF_MONTH) + "." + exchangeRateLastUpdated.get(Calendar.MONTH) + "." + exchangeRateLastUpdated.get(Calendar.YEAR) + "\nExchangeRateToEUR=" + exchangeRateEUR + "\nExchangeRateToRON=" + exchangeRateRON);
                buildUserSettings.close();
            }

            //Setting the primary stage so that other controllers can use it to display what they need displayed
            setCurrentStage(primaryStage);
            getCurrentStage().initStyle(StageStyle.UNDECORATED);

            //Setting the window title
            getCurrentStage().setTitle("Gender Gap Analyser");

            //Setting the splash screen to be shown on the application window
            getCurrentStage().setScene(new Scene(new FXMLLoader(getClass().getResource("AppScreens/SplashScreen-" + language + ".fxml")).load()));
            getCurrentStage().getScene().getStylesheets().add(Objects.requireNonNull(getClass().getResource("Stylesheets/" + displayMode + "Mode.css")).toExternalForm());

            //Setting the app icon that's going to be shown on the taskbar to the Gender Fluid free icon created by Vitaly Gorbachev, published on the flaticon website (https://www.flaticon.com/free-icon/gender-fluid_3369089?term=gender&related_id=3369089)
            getCurrentStage().getIcons().add(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/AppIcon.png")));
            getCurrentStage().centerOnScreen();

            //Setting the window to be not resizable
            getCurrentStage().setResizable(false);

            //Opening the window
            getCurrentStage().show();
            new Thread(appLoad).start();
        }
        catch (IllegalStateException e) {
            //An FXML file was not found
            if (new File("src/main/resources/com/gendergapanalyser/gendergapanalyser").exists()) {
                try {
                    //String variable where the root of the GitHub link is stored for easier download
                    String githubRoot = "https://raw.githubusercontent.com/Alin63992/GenderGapAnalyser/master/src/main/resources/com/gendergapanalyser/gendergapanalyser/";
                    //Creating the App Screens folder and downloading all the files that should be in it
                    Files.createDirectories(Path.of("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens"));
                    Files.createDirectories(Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens"));
                    if (!new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/SplashScreen-EN.fxml").exists() || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/SplashScreen-EN.fxml").exists()) {
                        FileUtils.copyURLToFile(URI.create(githubRoot + "AppScreens/SplashScreen-EN.fxml").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/SplashScreen-EN.fxml"), 500, 2000);
                        if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/SplashScreen-EN.fxml").exists())
                            Files.copy(Path.of("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/SplashScreen-EN.fxml"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/SplashScreen-EN.fxml"));
                    }
                    if (!new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/SplashScreen-FR.fxml").exists() || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/SplashScreen-FR.fxml").exists()) {
                        FileUtils.copyURLToFile(URI.create(githubRoot + "AppScreens/SplashScreen-FR.fxml").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/SplashScreen-FR.fxml"), 500, 2000);
                        if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/SplashScreen-FR.fxml").exists())
                            Files.copy(Path.of("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/SplashScreen-FR.fxml"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/SplashScreen-FR.fxml"));
                    }
                    if (!new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/SplashScreen-RO.fxml").exists() || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/SplashScreen-RO.fxml").exists()) {
                        FileUtils.copyURLToFile(URI.create(githubRoot + "AppScreens/SplashScreen-RO.fxml").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/SplashScreen-RO.fxml"), 500, 2000);
                        if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/SplashScreen-RO.fxml").exists())
                            Files.copy(Path.of("src/main/resources/com/gendergapanalyser/gendergapanalyser/AppScreens/SplashScreen-RO.fxml"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/AppScreens/SplashScreen-RO.fxml"));
                    }

                    //Setting the splash screen to be shown on the application window
                    getCurrentStage().setScene(new Scene(new FXMLLoader(getClass().getResource("AppScreens/SplashScreen-" + language + ".fxml")).load()));
                    getCurrentStage().getScene().getStylesheets().add(Objects.requireNonNull(getClass().getResource("Stylesheets/" + displayMode + "Mode.css")).toExternalForm());

                    //Setting the app icon that's going to be shown on the taskbar to the Gender Fluid free icon created by Vitaly Gorbachev, published on the flaticon website (https://www.flaticon.com/free-icon/gender-fluid_3369089?term=gender&related_id=3369089)
                    getCurrentStage().getIcons().add(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/AppIcon.png")));
                    getCurrentStage().centerOnScreen();

                    //Setting the window to be not resizable
                    getCurrentStage().setResizable(false);

                    //Opening the window
                    getCurrentStage().show();
                    new Thread(appLoad).start();
                }
                catch (IOException f) {
                    Alert applicationError = new Alert(Alert.AlertType.ERROR);
                    applicationError.setTitle("Application Error");
                    applicationError.setHeaderText("Severe Application Error!");
                    getCurrentStage().close();
                    if (!new File("src/main/resources/com/gendergapanalyser/gendergapanalyser").exists() || f instanceof FileNotFoundException) {
                        ButtonType goToGitHub = new ButtonType("Go to GitHub", ButtonBar.ButtonData.LEFT);
                        applicationError.setContentText("The application cannot start because one or more files required for it to run are missing and online recovery is impossible because " + (fileNotFoundOnGit ? "the missing files were not found on GitHub" : "the application's internal folder structure is broken") + ".\nPlease click the \"Go to GitHub\" button below to download the app again and get back on track.");
                        applicationError.getButtonTypes().add(goToGitHub);
                        applicationError.getDialogPane().setMaxWidth(750);
                        Optional<ButtonType> choice = applicationError.showAndWait();
                        if (choice.isPresent() && choice.get() == goToGitHub) {
                            try {
                                openProjectGitHubPage();
                            } catch (IOException | URISyntaxException ignored) {
                            }
                        }
                    }
                    else {
                        applicationError.setContentText("The application cannot start because one or more files required for it to run are missing and they couldn't be downloaded right now.\nPlease check that you're connected to the internet or wait for a few minutes and start the application again.");
                        applicationError.show();
                    }
                }
            }
            else {
                Alert applicationError = new Alert(Alert.AlertType.ERROR);
                applicationError.setTitle("Application Error");
                applicationError.setHeaderText("Severe Application Error!");
                ButtonType goToGitHub = new ButtonType("Go to GitHub", ButtonBar.ButtonData.LEFT);
                applicationError.setContentText("The application cannot start because one or more files required for it to run are missing and online recovery is impossible because the application's internal folder structure is broken.\nPlease click the \"Go to GitHub\" button below to download the app again and get back on track.");
                applicationError.getButtonTypes().add(goToGitHub);
                applicationError.getDialogPane().setMaxWidth(750);
                getCurrentStage().close();
                Optional<ButtonType> choice = applicationError.showAndWait();
                if (choice.isPresent() && choice.get() == goToGitHub) {
                    try {
                        openProjectGitHubPage();
                    } catch (IOException | URISyntaxException ignored) {}
                }
            }
        }
        catch (NullPointerException e) {
            //A CSS file was not found
            if (new File("src/main/resources/com/gendergapanalyser/gendergapanalyser").exists()) {
                try {
                    //String variable where the root of the GitHub link is stored for easier download
                    String githubRoot = "https://raw.githubusercontent.com/Alin63992/GenderGapAnalyser/master/src/main/resources/com/gendergapanalyser/gendergapanalyser/";
                    //Creating the App Screens folder and downloading all the files that should be in it
                    Files.createDirectories(Path.of("src/main/resources/com/gendergapanalyser/gendergapanalyser/Stylesheets"));
                    Files.createDirectories(Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/Stylesheets"));
                    if (!new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Stylesheets/DarkMode.css").exists() || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/Stylesheets/DarkMode.css").exists()) {
                        FileUtils.copyURLToFile(URI.create(githubRoot + "Stylesheets/DarkMode.css").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Stylesheets/DarkMode.css"), 500, 2000);
                        if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/Stylesheets/DarkMode.css").exists())
                            Files.copy(Path.of("src/main/resources/com/gendergapanalyser/gendergapanalyser/Stylesheets/DarkMode.css"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/Stylesheets/DarkMode.css"));
                    }
                    if (!new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Stylesheets/LightMode.css").exists() || !new File("target/classes/com/gendergapanalyser/gendergapanalyser/Stylesheets/LightMode.css").exists()) {
                        FileUtils.copyURLToFile(URI.create(githubRoot + "Stylesheets/LightMode.css").toURL(), new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Stylesheets/LightMode.css"), 500, 2000);
                        if (!new File("target/classes/com/gendergapanalyser/gendergapanalyser/Stylesheets/LightMode.css").exists())
                            Files.copy(Path.of("src/main/resources/com/gendergapanalyser/gendergapanalyser/Stylesheets/LightMode.css"), Path.of("target/classes/com/gendergapanalyser/gendergapanalyser/Stylesheets/LightMode.css"));
                    }

                    //Setting the splash screen to be shown on the application window
                    getCurrentStage().setScene(new Scene(new FXMLLoader(getClass().getResource("AppScreens/SplashScreen-" + language + ".fxml")).load()));
                    getCurrentStage().getScene().getStylesheets().add(Objects.requireNonNull(getClass().getResource("Stylesheets/" + displayMode + "Mode.css")).toExternalForm());

                    //Setting the app icon that's going to be shown on the taskbar to the Gender Fluid free icon created by Vitaly Gorbachev, published on the flaticon website (https://www.flaticon.com/free-icon/gender-fluid_3369089?term=gender&related_id=3369089)
                    getCurrentStage().getIcons().add(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/AppIcon.png")));
                    getCurrentStage().centerOnScreen();

                    //Setting the window to be not resizable
                    getCurrentStage().setResizable(false);

                    //Opening the window
                    getCurrentStage().show();
                    new Thread(appLoad).start();
                }
                catch (IOException f) {
                    Alert applicationError = new Alert(Alert.AlertType.ERROR);
                    applicationError.setTitle("Application Error");
                    applicationError.setHeaderText("Severe Application Error!");
                    getCurrentStage().close();
                    if (!new File("src/main/resources/com/gendergapanalyser/gendergapanalyser").exists() || f instanceof FileNotFoundException) {
                        ButtonType goToGitHub = new ButtonType("Go to GitHub", ButtonBar.ButtonData.LEFT);
                        applicationError.setContentText("The application cannot start because one or more files required for it to run are missing and online recovery is impossible because " + (fileNotFoundOnGit ? "the missing files were not found on GitHub" : "the application's internal folder structure is broken") + ".\nPlease click the \"Go to GitHub\" button below to download the app again and get back on track.");
                        applicationError.getButtonTypes().add(goToGitHub);
                        applicationError.getDialogPane().setMaxWidth(750);
                        Optional<ButtonType> choice = applicationError.showAndWait();
                        if (choice.isPresent() && choice.get() == goToGitHub) {
                            try {
                                openProjectGitHubPage();
                            } catch (IOException | URISyntaxException ignored) {
                            }
                        }
                    }
                    else {
                        applicationError.setContentText("The application cannot start because one or more files required for it to run are missing and they couldn't be downloaded right now.\nPlease check that you're connected to the internet or wait for a few minutes and start the application again.");
                        applicationError.show();
                    }
                }
            }
            else {
                Alert applicationError = new Alert(Alert.AlertType.ERROR);
                applicationError.setTitle("Application Error");
                applicationError.setHeaderText("Severe Application Error!");
                ButtonType goToGitHub = new ButtonType("Go to GitHub", ButtonBar.ButtonData.LEFT);
                applicationError.setContentText("The application cannot start because one or more files required for it to run are missing and online recovery is impossible because the application's internal folder structure is broken.\nPlease click the \"Go to GitHub\" button below to download the app again and get back on track.");
                applicationError.getButtonTypes().add(goToGitHub);
                applicationError.getDialogPane().setMaxWidth(750);
                getCurrentStage().close();
                Optional<ButtonType> choice = applicationError.showAndWait();
                if (choice.isPresent() && choice.get() == goToGitHub) {
                    try {
                        openProjectGitHubPage();
                    } catch (IOException | URISyntaxException ignored) {}
                }
            }
        }
        catch (IOException ignored) {}
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (getCurrentStage().getTitle().equals("Gender Gap Analyser")) {
            try {
                if (displayMode.equals("Dark"))
                    appIconImageView.setImage(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/AppIcon-Dark.png")));
                loadingCircleImageView.setImage(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/loading-" + Main.displayMode + ".gif")));
            } catch (FileNotFoundException ignored) {}
        }
        else {
            promptAnimator.setIn(new Animation(new ZoomIn()).setSpeed(3));
            darkOverlayAnimator.setIn(new Animation(new FadeIn()).setSpeed(3));

            //Making the window movable when dragging the embedded title bar
            titleBar.setOnMousePressed(event -> {
                dragX = getCurrentStage().getX() - event.getScreenX();
                dragY = getCurrentStage().getY() - event.getScreenY();
            });
            titleBar.setOnMouseDragged(event -> {
                getCurrentStage().setX(event.getScreenX() + dragX);
                getCurrentStage().setY(event.getScreenY() + dragY);
            });

            //Setting up the language picker
            languagePicker.setItems(FXCollections.observableArrayList(languages));
            switch (language) {
                case "EN" -> {
                    languagePicker.setValue(languages[0]);
                    currencyPicker.setValue(currencies[0]);
                }
                case "FR" -> {
                    languagePicker.setValue(languages[1]);
                    currencyPicker.setValue(currencies[1]);
                }
                case "RO" -> {
                    languagePicker.setValue(languages[2]);
                    currencyPicker.setValue(currencies[2]);
                }
            }
            //When selecting another language from the language picker...
            languagePicker.getSelectionModel().selectedIndexProperty().addListener(((observable, oldValue, newValue) -> {
                //Updating the language with the newly picked one and the currency to the one associated with the language
                language = languagesShort[newValue.intValue()];
                currency = currencies[newValue.intValue()];
                //Setting the boolean variable used by DataProcessing.createPDF method to true so that the method generates a new PDF document in a new language
                changedLanguage = true;
                //Setting the boolean variable used by DataProcessing.createPDF method to true so that the method generates a new PDF document with the new currency
                changedCurrency = true;
                Runnable rebuildResources = () -> {
                    try {
                        BufferedWriter buildUserSettings = new BufferedWriter(new FileWriter("src/main/resources/com/gendergapanalyser/gendergapanalyser/UserSettings.txt"));
                        buildUserSettings.write("DisplayMode=" + displayMode + "\nLanguage=" + language + "\nCurrency=" + currency + "\nExchangeRateLastUpdated=" + exchangeRateLastUpdated.get(Calendar.DAY_OF_MONTH) + "." + exchangeRateLastUpdated.get(Calendar.MONTH) + "." + exchangeRateLastUpdated.get(Calendar.YEAR) + "\nExchangeRateToEUR=" + exchangeRateEUR + "\nExchangeRateToRON=" + exchangeRateRON);
                        buildUserSettings.close();
                        //Creating the usable dataset again so that it uses the new currency
                        processData.prepareData();
                        //Recreating predictions graphs so that they use the newly set currency
                        if (processData.predictionsGenerated) {
                            processData.predictEvolutions(predictionValue);
                            processData.createSalaryGraphWithPredictionsForEverybody();
                        }
                        processData.createSalaryGraphForEverybody();
                    } catch (IOException ignored) {
                    }
                    //Recreating analyses in the new currency
                    processData.performAnalysis();
                    Platform.runLater(() -> {
                        try {
                            getCurrentStage().setScene(new Scene(new FXMLLoader(getClass().getResource("AppScreens/MainMenu-" + languagesShort[newValue.intValue()] + ".fxml")).load()));
                        } catch (IOException ignored) {
                        }
                        getCurrentStage().getScene().getStylesheets().add(Objects.requireNonNull(getClass().getResource("Stylesheets/" + displayMode + "Mode.css")).toExternalForm());
                        //Changing the title of the current stage
                        getCurrentStage().setTitle(language.equals("EN") ? "Evolution Graph" : language.equals("FR") ? "Graphe d'Évolution" : "Grafic de Evoluție");
                        switchTheme = new AnimatedThemeSwitcher(getCurrentStage().getScene(), new Animation(new FadeOut()).setSpeed(2.5));
                        switchTheme.init();
                    });
                };
                promptAnimator.setChild(new Pane(backgroundOperations));
                darkOverlayAnimator.setChild(new Pane(darkOverlay));
                try {
                    loadingCircleImageView.setImage(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/loading-" + displayMode + ".gif")));
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
                backgroundOperations.setVisible(true);
                darkOverlay.setVisible(true);
                new Thread(rebuildResources).start();
            }));

            //Setting up the currency picker
            currencyPicker.setItems(FXCollections.observableArrayList(currencies));
            switch (currency) {
                case "USD" -> currencyPicker.setValue(currencies[0]);
                case "EUR" -> currencyPicker.setValue(currencies[1]);
                case "RON" -> currencyPicker.setValue(currencies[2]);
            }
            //When selecting another language from the currency picker...
            currencyPicker.getSelectionModel().selectedIndexProperty().addListener(((observable, oldValue, newValue) -> {
                //Updating the currency with the newly picked one
                currency = currencies[newValue.intValue()];
                //Setting the boolean variable used by DataProcessing.createPDF method to true so that the method generates a new PDF document with the new currency
                changedCurrency = true;
                Runnable rebuildResources = () -> {
                    try {
                        BufferedWriter buildUserSettings = new BufferedWriter(new FileWriter("src/main/resources/com/gendergapanalyser/gendergapanalyser/UserSettings.txt"));
                        buildUserSettings.write("DisplayMode=" + displayMode + "\nLanguage=" + language + "\nCurrency=" + currency + "\nExchangeRateLastUpdated=" + exchangeRateLastUpdated.get(Calendar.DAY_OF_MONTH) + "." + exchangeRateLastUpdated.get(Calendar.MONTH) + "." + exchangeRateLastUpdated.get(Calendar.YEAR) + "\nExchangeRateToEUR=" + exchangeRateEUR + "\nExchangeRateToRON=" + exchangeRateRON);
                        buildUserSettings.close();
                        //Creating the usable dataset again so that it uses the new currency
                        processData.prepareData();
                        //Recreating predictions graphs so that they use the newly set currency
                        if (processData.predictionsGenerated) {
                            processData.predictEvolutions(predictionValue);
                            processData.createSalaryGraphWithPredictionsForEverybody();
                        }
                        processData.createSalaryGraphForEverybody();
                    } catch (IOException ignored) {
                    }
                    //Recreating analyses in the new currency
                    processData.performAnalysis();
                    Platform.runLater(() -> {
                        try {
                            getCurrentStage().setScene(new Scene(new FXMLLoader(getClass().getResource("AppScreens/MainMenu-" + language + ".fxml")).load()));
                        } catch (IOException ignored) {
                        }
                        getCurrentStage().getScene().getStylesheets().add(Objects.requireNonNull(getClass().getResource("Stylesheets/" + displayMode + "Mode.css")).toExternalForm());
                        //Changing the title of the current stage
                        getCurrentStage().setTitle(language.equals("EN") ? "Evolution Graph" : language.equals("FR") ? "Graphe d'Évolution" : "Grafic de Evoluție");
                        switchTheme = new AnimatedThemeSwitcher(getCurrentStage().getScene(), new Animation(new FadeOut()).setSpeed(2.5));
                        switchTheme.init();
                    });
                };
                promptAnimator.setChild(new Pane(backgroundOperations));
                darkOverlayAnimator.setChild(new Pane(darkOverlay));
                try {
                    loadingCircleImageView.setImage(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/loading-" + displayMode + ".gif")));
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
                backgroundOperations.setVisible(true);
                darkOverlay.setVisible(true);
                new Thread(rebuildResources).start();
            }));

            //Setting up the theme toggle
            if (displayMode.equals("Dark")) {
                darkModeButtonGlyph.setFitHeight(50);
                lightModeButtonGlyph.setFitHeight(35);
            } else {
                lightModeButtonGlyph.setFitHeight(50);
                darkModeButtonGlyph.setFitHeight(35);
            }

            //Setting up the information prompt
            sourcesToggle.setOnAction(e -> {
                if (contentCredits.isVisible()) {
                    contentCredits.setVisible(false);
                    contentSources.setVisible(true);
                } else sourcesToggle.setSelected(true);
            });
            creditsToggle.setOnAction(e -> {
                if (contentSources.isVisible()) {
                    contentSources.setVisible(false);
                    contentCredits.setVisible(true);
                } else creditsToggle.setSelected(true);
            });
            Tooltip USDeptOfLaborHyperlinkDescription = new Tooltip(language.equals("EN") ? "Opens the United States Department of Labor website in your default browser." : language.equals("FR") ? "Ouvre le site web du Département du Travail des États Unis dans votre navigateur." : "Deschide site-ul web al Departamentului de Muncă al Statelor Unite în navigatorul dumneavoastră.");
            USDeptOfLaborHyperlinkDescription.setFont(new Font("Calibri", 13));
            USDeptOfLaborHyperlinkDescription.setShowDelay(Duration.millis(200));
            USDeptOfLaborHyperlink.setOnAction(a -> {
                try {
                    Desktop.getDesktop().browse(new URI("https://www.dol.gov/agencies/wb/data/earnings/median-annual-sex-race-hispanic-ethnicity"));
                } catch (IOException | URISyntaxException ignored) {
                }
            });
            USDeptOfLaborHyperlink.setTooltip(USDeptOfLaborHyperlinkDescription);
            usDeptOfLaborYearRangeLabel.setText(usDeptOfLaborYearRangeLabel.getText() + processData.dataset[0][1] + " - " + processData.dataset[processData.dataset.length - 1][1]);
            try {
                ERALogoImageView.setImage(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/ExchangeRate-API-Logo-" + displayMode + ".png")));
            } catch (FileNotFoundException ignored) {
            }
            Tooltip ERAHyperlinkDescription = new Tooltip(language.equals("EN") ? "Opens the ExchangeRates-API website in your default browser." : language.equals("FR") ? "Ouvre le site web d'ExchangeRates-API dans votre navigateur." : "Deschide site-ul web al ExchangeRates-API în navigatorul dumneavoastră.");
            ERAHyperlinkDescription.setFont(new Font("Calibri", 13));
            ERAHyperlinkDescription.setShowDelay(Duration.millis(200));
            ERAHyperlink.setOnAction(a -> {
                try {
                    Desktop.getDesktop().browse(new URI("https://www.exchangerate-api.com/"));
                } catch (IOException | URISyntaxException ignored) {
                }
            });
            ERAHyperlink.setTooltip(ERAHyperlinkDescription);
            ERALastUpdatedLabel.setText(ERALastUpdatedLabel.getText() + (exchangeRateLastUpdated.get(GregorianCalendar.DAY_OF_MONTH) < 10 ? "0" + exchangeRateLastUpdated.get(GregorianCalendar.DAY_OF_MONTH) : exchangeRateLastUpdated.get(GregorianCalendar.DAY_OF_MONTH)) + (exchangeRateLastUpdated.get(GregorianCalendar.MONTH) < 10 ? ".0" : ".") + exchangeRateLastUpdated.get(GregorianCalendar.MONTH) + "." + exchangeRateLastUpdated.get(GregorianCalendar.YEAR));
            if (displayMode.equals("Light")) {
                try {
                    appIconCreditsImageView.setImage(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/AppIcon.png")));
                } catch (FileNotFoundException ignored) {
                }
            } else {
                try {
                    appIconCreditsImageView.setImage(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/AppIcon-Dark.png")));
                } catch (FileNotFoundException ignored) {
                }
            }

            //Displaying the discard predictions button if the user generated predictions
            discardPredictionsButton.setVisible(processData.predictionsGenerated);
        }
    }

    //Launch time!
    public static void main(String[] args) {
        launch();
    }
}