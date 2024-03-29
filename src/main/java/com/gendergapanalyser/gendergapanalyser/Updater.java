package com.gendergapanalyser.gendergapanalyser;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Updater {
    //Variable that holds the date this update will have been published
    protected static final GregorianCalendar appCurrentUpdateDate = new GregorianCalendar(2024, Calendar.JANUARY, 18);
    //Variable that holds the number of revisions that this update will have been published
    private static final int dailyRevision = 3;
    //Array that holds the update details
    private static final String[] updateDetails = {"", ""};

    public static String getCurrentAppVersion() {
        return appCurrentUpdateDate.get(GregorianCalendar.DAY_OF_MONTH) + "." + (appCurrentUpdateDate.get(GregorianCalendar.MONTH) + 1) + "." + appCurrentUpdateDate.get(GregorianCalendar.YEAR) + "." + dailyRevision;
    }

    public static String[] checkForUpdate() {
        try {
            //Preparing to connect to the GitHub API to get new update info
            HttpURLConnection connection = (HttpURLConnection) new URI("https://api.github.com/repos/Alin63992/GenderGapAnalyser/releases/latest").toURL().openConnection();
            connection.setConnectTimeout(500);
            connection.setReadTimeout(1000);
            connection.addRequestProperty("User-Agent", "Mozilla/5.0");

            //Connecting to the GitHub API
            connection.connect();

            //Saving the JSON response
            BufferedReader br = new BufferedReader(new InputStreamReader((InputStream) (connection.getResponseCode() == 200 ? connection.getContent() : connection.getErrorStream())));
            String output;
            StringBuilder json = new StringBuilder();
            while ((output = br.readLine()) != null)
                json.append(output);

            //Parsing the JSON response
            JSONObject obj = (JSONObject) JSONValue.parse(json.toString());

            //Extracting update details from the JSON response and saving them to the update array
            //If a crash happened during the update, and the update didn't finish applying, the details get saved anyway
            if (Main.updateInProgress.exists()) {
                updateDetails[0] = obj.get("tag_name").toString();
                updateDetails[1] = obj.get("body").toString();
            }
            //If the version available on GitHub is different from the version currently installed
            if (!obj.get("tag_name").toString().equals(getCurrentAppVersion())) {
                //If the version available on GitHub is slightly higher than the version currently installed
                if (Integer.parseInt(obj.get("tag_name").toString().split("\\.")[0]) > appCurrentUpdateDate.get(GregorianCalendar.DAY_OF_MONTH) || Integer.parseInt(obj.get("tag_name").toString().split("\\.")[1]) - 1 > appCurrentUpdateDate.get(GregorianCalendar.MONTH) || Integer.parseInt(obj.get("tag_name").toString().split("\\.")[2]) > appCurrentUpdateDate.get(GregorianCalendar.YEAR) || Integer.parseInt(obj.get("tag_name").toString().split("\\.")[3]) > dailyRevision) {
                    updateDetails[0] = obj.get("tag_name").toString();
                    updateDetails[1] = obj.get("body").toString();
                }
            }
        }
        //If there's something wrong with the API response
        catch (NullPointerException e) {
            return new String[]{"", ""};
        }
        catch (IOException | URISyntaxException ignored) {}
        return updateDetails;
    }


    public static void performUpdate() throws IOException {
        Runnable applyUpdate = () -> {
            //Array of files copied from the update archive into the app directory
            ArrayList<String> copiedFiles = new ArrayList<>();

            //Setting the app's current state as being in the updating stage, in the correct update stage,
            // to display the correct message on the splash screen.
            Main.appState = "UpdateStage-Update";

            //Backing up the currently installed app files into the GenderGapAnalyser-Backup folder, if a backup doesn't exist already
            String backupFolder = "GenderGapAnalyser-Backup";
            if (!new File(backupFolder).exists()) {
                try (Stream<Path> fileList = Files.walk(Path.of(""))){
                    //Traversing all the app files in the app directory
                    fileList.forEach(path -> {
                        //Ignoring the .git, GenderGapAnalyser-Backup and target folders since they don't get modified during the update
                        if (!path.startsWith(".git") && !path.startsWith(backupFolder) && !path.startsWith("target") && !path.endsWith(".updateInProgress") && !path.endsWith(".rollbackinprogress") && !path.endsWith(".cleanupinprogress")) {
                            try {
                                //If the item we're on is a folder, we create it within the backup folder (ignoring the blank entry that's found first)
                                if (path.toFile().isDirectory()) {
                                    Files.createDirectories(Path.of(backupFolder + (!path.toString().isEmpty() ? File.separator + path : "")));
                                }
                                //If it's a file, we copy it in its folder within the backup folder
                                else {
                                    Files.copy(path, Path.of(backupFolder + File.separator + path));
                                }
                            } catch (IOException ignored) {
                            }
                        }
                    });
                } catch (IOException ignored) {}
            }

            //Preparing the update
            File downloadedUpdateArchive = new File("GenderGapAnalyser-Update.zip");
            BufferedWriter writeUpdateLog = null;
            BufferedWriter writeRemainingFiles = null;
            ZipFile archivedUpdate = null;
            final String[] latestUpdate = Main.updateDetails[0].isEmpty() ? checkForUpdate() : Main.updateDetails;
            try {
                //Resuming update, if a crash happened during the update
                //If the update info log file exists
                if (new File("UpdateInfo.txt").exists()) {
                    //Preparing the update log that already exists
                    BufferedReader br = new BufferedReader(new FileReader("UpdateInfo.txt"));
                    String line;
                    //If the partially copied update version is the same as the newest update available
                    if (br.readLine().split("=")[1].equals(latestUpdate[0])) {
                        //Preparing the copiedFiles archive
                        while ((line = br.readLine()) != null) {
                            copiedFiles.add(line);
                        }
                        br.close();
                    }
                    //If it isn't
                    else {
                        //Deleting the downloaded update archive if it exists
                        if (downloadedUpdateArchive.exists())
                            downloadedUpdateArchive.delete();
                        //Recreating the update info log file to contain the newest version
                        br.close();
                        writeUpdateLog = new BufferedWriter(new FileWriter("UpdateInfo.txt"));
                        writeUpdateLog.write("Version=" + latestUpdate[0]);
                        writeUpdateLog.newLine();
                        writeUpdateLog.close();
                    }
                }
                else {
                    //Creating the update info log file
                    writeUpdateLog = new BufferedWriter(new FileWriter("UpdateInfo.txt"));
                    writeUpdateLog.write("Version=" + latestUpdate[0]);
                    writeUpdateLog.newLine();
                    writeUpdateLog.close();
                }

                //If the update archive doesn't exist
                if (!downloadedUpdateArchive.exists()) {
                    //Downloading the GitHub archive with the app files
                    FileUtils.copyURLToFile(URI.create("https://codeload.github.com/Alin63992/GenderGapAnalyser/zip/refs/tags/" + latestUpdate[0]).toURL(), downloadedUpdateArchive, 2000, 15000);
                }

                //Opening the update ZIP archive
                archivedUpdate = new ZipFile(downloadedUpdateArchive);

                //Setting the destination directory in which the update will be unzipped and the contents of the update are copied
                String destinationFolder = new File(".").getCanonicalPath();
                //Opening the update info log file
                writeUpdateLog = new BufferedWriter(new FileWriter("UpdateInfo.txt", new File("UpdateInfo.txt").exists()));

                //Unzipping the update archive to reveal the GenderGapAnalyser-main folder
                Enumeration<? extends ZipEntry> enu = archivedUpdate.entries();
                //Traversing the archive's contents
                while (enu.hasMoreElements()) {
                    boolean fileIsNew = false;

                    //Selecting each entry of the update archive
                    ZipEntry entry = enu.nextElement();
                    File zipEntryToBeExtracted = new File(destinationFolder, entry.getName().substring(("GenderGapAnalyser-" + latestUpdate[0]).length() + 1)); //the substring excludes "GenderGapAnalyser-<new version number>/" from the path so that everything copies to the main app folders and not to the subfolder of the zip archive created by GitHub which contains all the new app files
                    //Checking if the zip entry isn't empty
                    // (the entry's path isn't the root folder of the app itself), or if the entry's path
                    // starts with the root folder of the app (to not be vulnerable to zip-slipping - learn more at
                    // https://snyk.io/research/zip-slip-vulnerability)
                    if (!entry.getName().startsWith(".idea") && !entry.getName().startsWith(".git") && !zipEntryToBeExtracted.getCanonicalPath().equals(destinationFolder) && zipEntryToBeExtracted.getCanonicalPath().startsWith(destinationFolder)) {
                        //Checking if the file wasn't already copied or the folder already created
                        if (!copiedFiles.contains(zipEntryToBeExtracted.getCanonicalPath())) {
                            //Checking if the zip entry is a folder or a file
                            if (entry.isDirectory()) {
                                //Creating the folder
                                Files.createDirectories(Path.of(zipEntryToBeExtracted.getCanonicalPath()));
                            } else {
                                //Checking if a file with the same name exists already in the app files directory
                                if (!new File(zipEntryToBeExtracted.getCanonicalPath()).exists())
                                    //Categorizing the file as new, if not
                                    fileIsNew = true;
                                //Copying the file to the app files
                                try {
                                    Files.copy(archivedUpdate.getInputStream(entry), Path.of(zipEntryToBeExtracted.getCanonicalPath()), StandardCopyOption.REPLACE_EXISTING);
                                }
                                //If a file with the same file is found but couldn't be replaced because it's open in the splash screen
                                catch (FileSystemException e) {
                                    //Copying the new file as a temporary file with a ".copy" extension
                                    Files.copy(archivedUpdate.getInputStream(entry), Path.of(zipEntryToBeExtracted.getCanonicalPath() + ".copy"));
                                    fileIsNew = true;
                                    if (writeRemainingFiles == null) {
                                        writeRemainingFiles = new BufferedWriter(new FileWriter(Main.filesInUseToBeReplacedAtNextStart.getName()));
                                    }
                                    writeRemainingFiles.write(Path.of(zipEntryToBeExtracted.getCanonicalPath()).toString());
                                    writeRemainingFiles.newLine();
                                }
                            }
                            //Adding the new folder or the newly copied file to the copiedFiles array and the update log
                            copiedFiles.add(fileIsNew ? "New:" + zipEntryToBeExtracted.getName() : zipEntryToBeExtracted.getCanonicalPath());
                            if (fileIsNew)
                                writeUpdateLog.write("New:");
                            writeUpdateLog.write(zipEntryToBeExtracted.getCanonicalPath());
                            writeUpdateLog.newLine();
                            //For every 5 operations made, we flush the writer's buffer to the file so that the update
                            // log is always up to date on the progress.
                            if (copiedFiles.size() % 5 == 0)
                                writeUpdateLog.flush();
                        }
                    }
                }

                //Closing the zip update archive
                archivedUpdate.close();

                //Finishing the update log and the files remaining to be written, if any
                writeUpdateLog.close();
                if (writeRemainingFiles != null)
                    writeRemainingFiles.close();
            }
            catch (IOException e) {
                try {
                    //Setting the screen to inform the user that an error occurred and a rollback is in progress
                    Main.appState = "UpdateStage-Rollback";

                    //Closing the update zip archive
                    if (archivedUpdate != null) {
                        archivedUpdate.close();
                    }

                    //Closing the stream that writes to the update info log file, in case an error occurs
                    // before the file is closed for writing
                    if (writeUpdateLog != null) {
                        writeUpdateLog.close();
                    }
                    //Closing the stream that writes to the file containing files in use that remain to be replaced,
                    // in case an error occurs before the file is closed for writing
                    if (writeRemainingFiles != null) {
                        writeRemainingFiles.close();
                    }

                    //Traversing the array which holds the new/modified files
                    for (String file: copiedFiles) {
                        //Deleting every file or directory newly added to the existing files, that's specific to the new update, to save storage
                        if (file.startsWith("New:")) {
                            File filePathWithoutNewPrefix = new File(file.substring(4));
                            if (filePathWithoutNewPrefix.isDirectory())
                                FileUtils.deleteDirectory(filePathWithoutNewPrefix);
                            else filePathWithoutNewPrefix.delete();
                        }
                        else {
                            //Copying any file (not folder) that was overwritten during the update from the backup folder back to its original location
                            if (!new File(file).isDirectory()) {
                                String destinationFolder = new File(".").getCanonicalPath();
                                File filePathIncludingBackupFolder = new File(file.replace(destinationFolder, destinationFolder + File.separator + backupFolder));
                                Files.copy(filePathIncludingBackupFolder.toPath(), new File(file).toPath(), StandardCopyOption.REPLACE_EXISTING);
                            }
                        }
                    }

                    //Deleting the temporary files that would replace the files in use at the next start. They're not needed any more since the update failed
                    if (Main.filesInUseToBeReplacedAtNextStart.exists()) {
                        BufferedReader readRemainingFiles = new BufferedReader(new FileReader(Main.filesInUseToBeReplacedAtNextStart.getName()));
                        String line;
                        while ((line = readRemainingFiles.readLine()) != null) {
                            new File(line + ".copy").delete();
                        }
                        readRemainingFiles.close();

                        //Also deleting the temp files log
                        Main.filesInUseToBeReplacedAtNextStart.delete();
                    }
                } catch (IOException ignored) {}
            }

            //Cleaning up
            try {
                //Keeping the rollback status if the app is in the update rollback stage, for error display
                if (!Main.appState.equals("UpdateStage-Rollback"))
                    Main.appState = "UpdateStage-Cleanup";

                //Deleting the downloaded update archive, the backup folder and the update log
                downloadedUpdateArchive.delete();
                FileUtils.deleteDirectory(new File(backupFolder));
                new File("UpdateInfo.txt").delete();

                //Interrupting all running threads
                Main.interruptThreads = true;
            } catch (IOException ignored) {}

            //Finishing the update
            Platform.runLater(() -> {
                Alert updateDone;
                if (!Main.appState.equals("UpdateStage-Rollback")) {
                    updateDone = new Alert(Alert.AlertType.INFORMATION);
                    if (Main.language.equals("EN")) {
                        updateDone.setHeaderText("Done updating!");
                        updateDone.setContentText("The application now has the most recent updates installed!\nBefore you can use the new features and experience the new bug fixes and improvements, the app will close, and you'll need to run it again from your IDE.\nEnjoy the app!");
                    } else if (Main.language.equals("FR")) {
                        updateDone.setHeaderText("Actualisation finie !");
                        updateDone.setContentText("L'application maintenant à installé les plus récents mises à jour !\nAvant que vous pouvez utiliser les nouveaux fonctionnalités et découvrir les améliorations, l'application va se fermer, et vous nécessitez la rélancer depuis votre IDE.\nSavourez l'application !");
                    } else {
                        updateDone.setHeaderText("Actualizare finalizată!");
                        updateDone.setContentText("Aplicația are acum instalate cele mai recente actualizări!\nÎnainte să puteți utiliza noile funcționalități și descoperi îmbunătățirile, aplicația se va închide, și este necesar să o relansați din IDE-ul dumneavoastră.\nBucurați-vă de aplicație!");
                    }
                }
                else {
                    updateDone = new Alert(Alert.AlertType.ERROR);
                    if (Main.language.equals("EN")) {
                        updateDone.setHeaderText("Done rolling back changes!");
                        updateDone.setContentText("The application encountered an error while updating and it rolled back the changes, so you can still use the version you had installed before!\nIn order to use it again, the app will close, and you'll need to run it again from your IDE.\nEnjoy the app!");
                    } else if (Main.language.equals("FR")) {
                        updateDone.setHeaderText("Annulation des changements finie !");
                        updateDone.setContentText("L'application à rencontré une erreur pendant la mise à jour et a annulé les changements, et donc vous pouvez utiliser la version qui était installée avant !\nPour pouvoir l'utiliser du nouveau, l'application va se fermer, et vous nécessitez la rélancer depuis votre IDE.\nSavourez l'application !");
                    } else {
                        updateDone.setHeaderText("Anularea modificărilor finalizată!");
                        updateDone.setContentText("Aplicația a întâlnit o eroare în timpul actualizării și a anulat modificările, deci puteți folosi versiunea care era instalată înainte!\nPentru a o putea folosi din nou, aplicația se va închide, și este necesar să o relansați din IDE-ul dumneavoastră.\nBucurați-vă de aplicație!");
                    }
                }
                updateDone.setTitle(updateDone.getHeaderText());
                updateDone.getDialogPane().setMaxWidth(750);
                try {
                    ((Stage)updateDone.getDialogPane().getScene().getWindow()).getIcons().add(new Image(new FileInputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Glyphs/Miscellaneous/alert-" + (updateDone.getAlertType().equals(Alert.AlertType.ERROR) ? "error.png" : "information.png"))));
                } catch (IOException ignored) {}
                Main.getCurrentStage().close();
                if (Main.updateInProgress.exists())
                    Main.updateInProgress.delete();
                updateDone.show();
            });
        };
        if (!Main.updateInProgress.exists()) {
            Main.cleanUp();
            Main.updateInProgress.createNewFile();
        }
        //Setting the screen to inform the user that an update is in progress
        Main.getCurrentStage().setTitle("Gender Gap Analyser");
        Scene splash = new Scene(new FXMLLoader(Updater.class.getResource("AppScreens/SplashScreen-" + Main.language + ".fxml")).load());
        splash.getStylesheets().setAll(Objects.requireNonNull(Updater.class.getResource("Stylesheets/" + Main.displayMode + "Mode.css")).toExternalForm());
        Main.getCurrentStage().setScene(splash);
        new Thread(applyUpdate).start();
    }
}
