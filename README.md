Gender Gap Analyser
===================
A program written in Java for my bachelor's degree thesis that downloads a dataset of men's and women's salaries from the United States, from the U.S. Department of Labor's website, and that analyses the salaries, creates graphs and predictions using data mining and simple regression for up to 100 years, generates a PDF report that contains an evolution graph, the interpretations of the evolutions and a table with all the data used, and can mail the PDF report to the user's email address.  
The app is available in English, French and Romanian, and the statistics used by it come in the United States Dollar, but the app can convert them to Euro and Romanian New Leu using exchange rates provided by [ExchangeRate API](https://www.exchangerate-api.com/), based on the user's language or currency choice.  
It uses JavaFX to have a GUI that's not cluttered and that's easy and pleasant to use.  
**Coordinating teacher:** Iuliana Marin

### Problem: the prediction is linear (the results of prediction are linear and ascending, with little to no difference in salary from year to year)

### Changelog

* 23.09.2023 - Happy new year update!
  - **Revision 3**: removed the integrity check messages from the console that appeared during the app integrity check.
  - **Revision 2**: updated the fallback dataset to be the latest one provided by the US Department of Labor, which also includes statistics for 2022.
  - Git specific: renamed branch "master" to "main". Please update your development environments to use the new branch name by using the following commands:
  ```
  git branch -m master main
  git fetch origin
  git branch -u origin/main main
  git remote set-head origin -a
  ```
  - GitHub specific: Started using GitHub Releases for checking for and providing updates.
  - The app can now update itself using GitHub's Releases function. From now on, each new update is going to be published with its own release.  
    At every app start, the app is going to check for a new release, and if found, it's going to display a red update link in the top bar of the main menu, which when pressed, will display a prompt containing the new app version and the changelog displayed in Markdown, thanks to the [JPro Platform](https://github.com/JPro-one/jpro-platform), and it will offer the options to update, open the project's GitHub page to download the source code manually, or not update.
  - Rolled back a change made in the update published on the 15.09.2023 and added an animation speed for closing the main menu prompts, even though they just disappear upon closing, to make the main menu buttons accessible right after the prompts are closed.
  - Increased visibility of the scroll pane's scroll bars' thumbs in dark mode and changed their colour in light mode.
  - Made the main menu scroll panes white in light mode, so they have the same colour as the rest of the prompt.
  - Renamed UserSettings.txt to Properties.txt to reflect the fact that the file holds settings and other app properties (exchange rates).
  - Set the font family of the interpretations on the analysis page to be the same as the one throughout the app (Calibri).
  - Set the cursor to be an open hand when hovering over the top bar of any window and a closed hand when the user clicks and holds the title bar to move it someplace else, to indicate that that area can be sed to move the app window. Also, made the splash screen draggable by the same area.
  - Fixed an issue where the title of the window wouldn't reflect the page contained in it upon changing the app language from that window. Also, removed the instruction that set the title of the window when changing the currency.
  - Updated the iTextPDF dependency to iTextPDF 7. This update, besides the new fixes and improvement brought by the iTextPDF team, also fixes an improper Restriction of XML External Entity Reference vulnerability. So now the app is even more secure. Also removed the BouncyCastle dependency needed for the old version of iTextPDF, and which had an [LDAP injection vulnerability](https://www.cve.org/CVERecord?id=CVE-2023-33201). Also, now only the needed iTextPDF dependencies are imported instead of importing the whole package.
  - Thanks to the iTextPDF update, the **DataProcessing.createPDF()** method was partially rewritten to accommodate the new iTextPDF requirements, and the PDF now looks cleaner and more professional.
  - Extracted the file cleanup part of **Main.exitAppMain()** into the Main.cleanUp() method, because the same routine is used by the Updater class when updating the app. This was done to avoid code duplicates.
  - Extracted the toggling of the focus traversable property of the main menu items (except the theme toggles) from all the Main prompt toggling functions into the new **Main.setFocusTraversableForMainMenuItems()** function. It takes as parameter the new boolean value of the property if the main menu links and buttons should be focusable by keyboard or not.
  - The main menu no longer gets reloaded when switching currencies since no sums are shown on the main menu screen.
  - When switching languages/currencies, opening the PDF report and whatever else that needs to reload the current screen, the scene isn't loaded as it's being set as the window's scene and then the stylesheet being set after, but the scene is now loaded into a **Scene** variable, then the stylesheet gets set, then the loaded scene with the stylesheet gets set as the window's current screen. This was done to avoid a split second of flashing or graphical glitch that was happening after setting the window's current scene but before the stylesheet got set.
  - Added an error at the beginning of the program that informs the user that the exchange rates couldn't be updated, if that happens. Also, the currency picker is disabled if the program was first launched without an internet connection and the exchange rates couldn't be updated (if they're set to their default -1.0 value).
  - Fixed the wrong exchange rate update date being saved in the **Properties.txt** file when first launching the program without an internet connection.
  - Performance improvement: The dataset now doesn't get prepared again any more when changing the language if the exchange rates have never been updated, but only the graphs and interpretations. Also, the graphs and interpretations are no longer regenerated twice when changing the currency or the language and currency since the methods that do the regenerating are called in the end of **DataProcessing.prepareData()** and not called again when the change happens.
  - Now the app checks if the resources folder exists and has the right structure before it initially loads the app properties and not in the **Main.checkAndRecover()** method any more, since loading the preferences requires the folder to be present and its structure to be sound.
  - The dark and light versions of the app icon and the loading circle are obtained before the app window with the splash screen appears, since they are both used by the splash screen, and not during the dedicated app recovery mechanism any more.
  - Restructured the recovery function: the app now checks outside the **Main.checkAndRecovery()** function if all the required resource folders exist (AppScreens, Glyphs/Emojis, Glyphs/Miscellaneous and Stylesheets) exist, and creates them if they don't. Then, the app checks outside the recovery function if the dark+light mode app icons, stylesheets and the three splash screen FXML files exist in their respective folders, and downloads them if they don't. This was made so that all the files required for the good function and look of the splash screen which also displays the status of the app (recovery/update in progress). Then, the app continues the check and recovery in the special function.
  - Added a message on the splash screen that asks the user to not close the window when an update is in progress.
  - The exchange rates obtained through the API are now selected and saved by parsing the JSON response using the [Google JSON.Simple](https://mvnrepository.com/artifact/com.googlecode.json-simple/json-simple) library, so it should be faster now. 
  - The tooltips of the source website hyperlinks in the About pane are now embedded into the FXML files themselves and are no longer created and attached in the **Main** class.  
  - Since some threads couldn't be interrupted using the usual [Java Thread.interrupt()](https://docs.oracle.com/javase/8/docs/api/java/lang/Thread.html#interrupt--) method, now an **interruptThreads** boolean variable is used, which is set to true when exiting the app. So, in every thread, now the individual cleanup and stopping is checked through this boolean, and not based through the individual thread's interrupted state using the [Java Thread.isInterrupted()](https://docs.oracle.com/javase/8/docs/api/java/lang/Thread.html#isInterrupted--) method.
  - Rearranged the variables in **Main** to be easier to find and read.
  - Removed the HTML elements from the README and made it completely Markdown.
  - Updated the README to also mention the linear predictions problem.

* 20.09.2023
  - Fixed online recovery by copying the downloaded resource that was missing to the "target" folder too. This only happens for downloaded FXML and CSS files, since the rest are obtained through an absolute path to the "resources" folder. Also, the app also checks for the resources in the "target" folder.
  - Moved the app integrity check & recovery procedure to its own function called **checkAndRecover()**.
  - Removed setting the visibility of the recovery label on the splash screen as it was throwing NullPointerException, will look into it soon.
  - Added credits for the rainbow spinning ball animation on the credits page.
  - Added a link to the project's GitHub page on the credits page.
  - Now the app window closes when a severe application error occurs.

* 15.09.2023
  - **Added earlier but forgotten to be mentioned in changelogs:** On the graphs screen, now the salaries and the pay gaps all have tooltips that specify the full sum, so that if the sum doesn't fit inside its cell (in the case of RON, for example) then the user can see the sum inside the tooltip.
  - Fixed the digits being slightly cut off or only 3 digits being displayed (if using typing a number using wide digits like 9999) in the inputs for the limit years of the range of years to be included in the graph on the graph screen by slightly increasing the width of the inputs, so that 4 digits fully fit into the input box now.
  - Fixed the graph recovery not happening when toggling the inclusion of the pay gap or the predictions (if applicable) on the graphs screen.
  - Fixed the background not getting darker when opening a prompt on the main menu after discarding generated predictions from it.
  - Removed some instructions that weren't actually helping with anything (e.g. setting the dark overlay in **Main.attemptPrediction()** as an animator child and making it visible while it was already visible from **Main.togglePredictionPrompt()**).
  - Made the icons of the buttons on the main menu smaller for French and Romanian, so they match the ones for English.
  - Modified the data sources popup to be more spacious and introduced icons in it, and also added a section into the same prompt to credit the creators of the icons and pictures used throughout the program and the person who came up with the idea for this app and the reason why it was developed.
  - Changed the name of all the emojis used in the app to use the official Unicode Character Database names.
  - Reorganized the Resources folder:
    - the Glyphs folder still contains the Emojis folder, and the rest of its items are now contained into the new Miscellaneous folder;
    - the FXML files are now contained into the new AppScreens folder.
  - Split the SplashScreen.fxml file into three files dependent on the app language (like for the other three screens).
  - Improved the app integrity check: the app now checks for each file specifically by path and name, not just by extension. Also, for the files that couldn't be found, the app now attempts to download them from GitHub.
  - Removed the SplashStart class and made Main the controller of the splash screen.

* 01.09.2023
  - Discovered that the folder where evolution graphs are stored, which is empty when the app is not running, wasn't being uploaded to Git, thus preventing the app from starting. Now, if the folder doesn't exist, it is created in the **DataProcessing.createSalaryGraphForEverybody()** function, since it's the first graph drawing function to be called when the app first starts, thus assuring that the folder exists.
  - Considering the above fix, now the Graphs folder isn't emptied out upon exit any more, but deleted as a whole instead.
  - The application is now more resilient if something happens to one or all of the graphs or the folder where they're saved. If that happens, the app recreates the folder and the graphs that were drawn up until the point of failure, plus the needed graphs requested by the user. That way, the app is now able to recover from this kind of failure.
  - Fixed a bug that was preventing analyses from being written when a currency other than the USD was being used and predictions were created.
  - Fixed a mistake that allowed the salary column in the statistics table on the evolution graphs screen to change according to the salary's length and not stay fixed to a third of the VBox parent and shorten the salary when predictions were created, thus displaying a horizontal scroll bar below the table and creating visual glitches.
  - Replaced checks of empty strings using **equals("")** with **isEmpty()**.
  - Added an application integrity check that checks if the application has all the files needed for it to run, and if not, then an error is shown in the default look and the app start is cancelled.
  - The app now ships without a UserSettings file, and it's generated with the default settings if it doesn't exist (the app will be in English, in dark mode, and will be using USD as the currency). The exchange rates will be updated when the application starts.
  - Removed the unused Google GSON Maven dependency.

* 24.08.2023 - big update day!
  - Fixed the bug that was preventing the app from starting.
  - Added a new splash screen with the app icon, the app title in English/French/Romanian (according to the user's preferred language) and a spinning wheel animation. The splash screen follows the user's preferred theme. The app now appears on the screen faster while it processes the dataset and prepares everything in the background, so it won't take long to display after starting it on slower systems, and there's something displayed while the data and resources are being prepared.
  - Two new currencies are available in the app: the Euro (EUR) and the Romanian New Leu (RON). The user can choose in which one of the 3 available currencies to view th graphs, stats and interpretations, and the app will either use the original values - which come in the United States Dollar (USD) - or convert the USD values in the currency the user chose in the picker. The exchange rates are provided by <a href="https://www.exchangerate-api.com/">ExchangeRate-API</a>. When the user changes the app language, the currency also changes to the currency tied to that language (English - USD; French - EUR; Romanian - RON), and can be easily changed through the picker.
  - Added a link on the main menu screen that opens a popup describing the sources of data (statistics and exchange rates). Each has a link that opens the webpage of the source in the OS' default browser, and the latest data obtained from that source.
  - Fixed the theme switching animation not playing after the app reloads the main menu or switches back to it when the new dataset finishes downloading and was processed.
  - Made the theme toggles accessible when a popup is displayed on the screen.
  - Made the title of the PDF report to display the earliest and latest year of statistics taken from the dataset, so the year 1960 (lower limit of salary statistics available on 24.08.2023) isn't hardcoded anymore. That way, if a dataset with statistics before 1960 is found, the title of the PDF doesn't still display 1960.
  - Fixed sending the PDF report through e-mail when the app does contain internally the e-mail address and password the e-mail should be sent from. Now, the app won't ask for the destination e-mail when first attempting to send it, and then saying that there is no source credentials and asking the user for their e-mail address and password when trying to send the e-mail again afterwards. Instead, it only asks for the destination e-mail address at each send attempt.
  - A wait popup is displayed when changing the currency or the language of the app, since the dataset is reprocessed with the new currency and the graphs and predictions are regenerated. That way, the app doesn't freeze on slower systems.
  - The app is now more colourful! Replaced in-code emojis with pictures of the actual emojis (used by Microsoft across its products), obtained with <a href="https://emojipedia.org/">Emojipedia</a>. That way, the icons of the buttons on the main screen have colour. Also, the arrows in the navigation links are now full emojis and look better, the theme toggle has colourful icons and the colour of the button doesn't change anymore, but the icons of the buttons each get bigger to indicate the current theme. On the interpretations screen, the slim emojis were replaced by full emojis for men and women, and the gender gap icon is now the app's icon. Finally, the colours of the titles were changed to match the outline colour of their respective icon.
  - Small code restructuration.

* 19.07.2023
  - ~~Added a currency dropdown that converts all the salaries in the dataset from USD to EUR and RON. It uses data from the [ExchangeRate API](https://www.exchangerate-api.com/)~~. **Update 24.08.2023:** Turns out that this feature wasn't actually implemented and a broken build of the app was uploaded to Git.
  - Slight cosmetic changes.
  - Removed commented and unused code.

* 12.07.2023
  - Translated in French and Romanian the prompt asking the user for their email and password to send the report to their own email address and through it.

* 09.07.2023
  - Git specific: removed all the commits for security purposes, and moved this project to a public repository.

* 07.07.2023
  - Fixed the lack of animation on prompt opening or theme switching on the main menu after attempting to email the PDF report.
  - Added a prompt that asks the user for an Outlook account from and to which send the PDF report, if the project is downloaded from Git, which misses the default email and password (English only, for now).

* 04.07.2023
  - Fixed a bug that was allowing the user to set the lower limit of the interval as a year that is beyond the lower limit of the dataset through the text field, and the app would generate a graph beginning with the specified year.
  - Removed the programming of the "See graph for this range" button from the **initialize()** function and moved everything into the new **processRange()** function which is bound to the button in the **DisplayEvolutionGraph** files, to follow the model of setting up the buttons in the app like it is in the rest of the app, through functions and not through the **setOnAction()** method.
  - Removed the fx:id of the "See graph for this range" button on the **DisplayEvolutionGraph** files, since it is no longer needed.
  - Applied the previous 2 changed to the OK button in the "Predictions discarded" prompt boxes in the **MainMenu** files.

* 02.07.2023
  - Fixed updating the window title in the current language after changing the app language, so that the title doesn't stay as the one in the language the app was in when the window was opened.
  - Added icons that will be shown on the taskbar for the alerts.
  - Changed the formulation of some code explanations.
  - Changed the formulation of the header text in the data inclusion confirmation box that appears before sending an email, from "user data/détails d'utilisateur/datelor utilizatorului" to "validation data/détails de validation/datelor de validare". In the same box, also changed part of the content text in all languages to reflect the change from user data to validation data.

* 01.07.2023
  - Separated the dark overlay from the main menu prompt/confirmation/waiting boxes, and now there's only one in the whole menu. It still shows only with the said boxes, but internally there isn't an overlay wrapped with each box anymore.
  - Repurposed the existing AnimatedSwitcher to animate just the prompt/confirmation/waiting boxes.
  - Added a new AnimatedSwitcher to animate the dark overlay fading in.
  - Changed the boxes' animation from a fade in to a zoom in.
  - Applied the zoom also for the prediction deletion confirmation box.
  - Applied the changes and animations above for the main menu in French and Romanian too.
  - Made the mail button traversable by the tab key upon closing the prediction deletion confirmation box.
  - Removed unnecessary instructions in code (e.g.: making the main menu buttons not traversable by the tab key after submitting a valid prediction value, before showing the waiting box and beginning the prediction. The main menu buttons were set to be not traversable before showing the prediction input prompt).
  - Changed the visibility of the 2 **AnimatedSwitcher**s from public to private and attached the **@FXML** annotation to align with the rest of the class' JavaFX objects being private and accessible inside it only.
  - Git specific: deleted the **main-branch** branch, since it was no longer used, and replaced it with the **master** branch that includes all the changes and fixes.
  - Updated the README to include this changelog, which will be updated with every commit. Also changed the introduction paragraph of the README.

* 30.06.2023
  - Fixed theme switching transition so that the transition plays when switching themes after changing the display language too.
  - Added a transition when navigating between the graph and the data interpretation pages.
  - Added a transition when invoking the prediction and the email prompts on the main menu.

* 26.06.2023
  - Introduced a transition to smoothen the switch between dark mode and light mode.
  - Indented the code in DataProcessing.createPDF().
  - Added a little, empty, nonfunctional hyperlink in all the FXML files, at the top of the hierarchy, so that when a page displays, none of the visible elements on the page that are focus traversable are initially focused, so that the page is displayed with nothing highlighted.
  - Added more comments and explications in the SendEmailInBackground class.

* 24.06.2023
  - Changed the way that DataProcessing.createPDF() functions, so that the PDF report does not get opened from within this function, but from Main.generatePDF(), so that the report doesn't open anymore when the user requests to have it sent by email.
  - Changed the alerts throughout the app to also have dark mode when the application is set to dark mode.
  - Changed the alerts to not have the operating system's window decorations (title bar, borders) so that the title bar won't be white on an alert which is in dark mode.