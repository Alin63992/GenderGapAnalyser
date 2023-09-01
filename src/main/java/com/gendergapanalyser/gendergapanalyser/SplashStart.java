package com.gendergapanalyser.gendergapanalyser;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ResourceBundle;

public class SplashStart implements Initializable {
    @FXML
    private Label splashLabel;
    @FXML
    private ImageView appIconImageView;
    @FXML
    private ImageView loadingCircleImageView;

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
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            switch (Main.language) {
                case "EN" -> splashLabel.setText("Gender Gap Analyser");
                case "FR" -> splashLabel.setText("Analyseur de l'Égalité Entre les Genres");
                case "RO" -> splashLabel.setText("Analizator al Egalității între Genuri");
            }
            if (Main.displayMode.equals("Dark"))
                appIconImageView.setImage(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/AppIcon-Dark.png")));
            loadingCircleImageView.setImage(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/loading-" + Main.displayMode + ".gif")));
        } catch (FileNotFoundException ignored) {}
    }
}
