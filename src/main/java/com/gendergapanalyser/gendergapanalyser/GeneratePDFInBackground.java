package com.gendergapanalyser.gendergapanalyser;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class GeneratePDFInBackground implements Runnable {
    @Override
    public void run() {
        try {
            //Generating a PDF report
            Main.processData.createPDF();
        } catch (IOException ignored) {}

        //If this thread is interrupted from the outside, we stop it
        if (Main.interruptThreads) {
            try {
                Files.delete(Path.of("src/main/resources/com/gendergapanalyser/gendergapanalyser/Analysis.pdf"));
            } catch (IOException ignored) {}
            return;
        }

        try {
            //Locating the generated report PDF
            File existingPDF = new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Analysis.pdf");
            //Opening it
            Desktop.getDesktop().open(existingPDF);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //Reloading the main menu screen so the wait screen is removed and the menu is usable again
        Platform.runLater(() -> {
            try {
                Scene mainScene = new Scene(new FXMLLoader(getClass().getResource("AppScreens/MainMenu-" + Main.language + ".fxml")).load());
                mainScene.getStylesheets().setAll(Objects.requireNonNull(getClass().getResource("Stylesheets/" + Main.displayMode + "Mode.css")).toExternalForm());
                Main.getCurrentStage().setScene(mainScene);
            } catch (IOException ignored) {}
        });
    }
}
