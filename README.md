#Gender Gap Analyser  
A program
written in Java for my bachelor's degree thesis
that downloads a dataset of men's and women's salaries from the United States,
from the U.S. Department of Labor's website,
and that analyses the salaries, creates graphs and predictions using data mining and simple regression for up to 100 years,
generates a PDF report that contains an evolution graph, the interpretations of the evolutions and a table with all the data used, and can mail the PDF report to the user's email address.<br>
The app is available in English, French and Romanian, and the statistics used by it come in the United States Dollar, but the app can convert them to Euro and Romanian New Leu using exchange rates provided by <a href="https://www.exchangerate-api.com/">ExchangeRate API</a>, based on the user's language or currency choice.<br>
It uses JavaFX in order to have a GUI that's not cluttered and that's easy and pleasant to use.
Coordinating teacher: Iuliana Marin

### Changelog
<ul>
<li>
<u>20.09.2023</u><br>
- Fixed online recovery by copying the downloaded resource that was missing to the target folder too.
This only happens for downloaded FXML and CSS files,
since the rest are obtained through an absolute path to the "resources" folder. Also, the app also checks for the resources in the "target" folder.<br>
- Moved the app integrity check & recovery procedure to its own function called <b>checkAndRecover()</b>.<br>
- Removed setting the visibility of the recovery label on the splash screen as it was throwing NullPointerException, will look into it soon.<br>
- Added credits for the rainbow spinning ball animation on the credits page.<br>
- Added a link to the project's GitHub page on the credits page.<br>
- Now the app window closes when a severe application error occurs.
</li>
<br>
<li>
<u>15.09.2023</u><br>
- <b>Added earlier but forgotten to be mentioned in changelogs: </b> On the graphs screen, now the salaries and the pay gaps all have tooltips that specify the full sum, so that if the sum doesn't fit inside its cell (in the case of RON, for example) then the user can see the sum inside the tooltip.<br>
- Fixed the digits being slightly cut off or only 3 digits being displayed (if using typing a number using wide digits like 9999) in the inputs for the limit years of the range of years to be included in the graph on the graph screen by slightly increasing the width of the inputs, so that 4 digits fully fit into the input box now.<br>
- Fixed the graph recovery not happening when toggling the inclusion of the pay gap or the predictions (if applicable) on the graphs screen.<br>
- Fixed the background not getting darker when opening a prompt on the main menu after discarding generated predictions from it.<br>
- Removed some instructions that weren't actually helping with anything
(e.g. setting the dark overlay in <b>Main.attemptPrediction()</b>
as an animator child and making it visible while it was already visible from <b>Main.togglePredictionPrompt()</b>).<br>
- Made the icons of the buttons on the main menu smaller for French and Romanian,
so they match the ones for English.<br>
- Modified the data sources popup to be more spacious and introduced icons in it,
and also added a section into the same prompt
to credit the creators of the icons and pictures
used throughout the program and the person
who came up with the idea for this app and the reason why it was developed.<br>
- Changed the name of all the emojis used in the app to use the official Unicode Character Database names.<br>
- Reorganised the Resources folder: the Glyphs folder still contains the Emojis folder,
and the rest of its items are now contained into the new Miscellaneous folder;
the FXML files are now contained into the new AppScreens folder.<br>
- Split the SplashScreen.fxml file into three files dependent on the app language
(like for the other three screens).<br>
- Improved the app integrity check:
the app now checks for each file specifically by path and name, not just by extension.
Also, for the files that couldn't be found, the app now attempts to download them from GitHub (to be tested).<br>
- Removed the SplashStart class and made Main the controller of the splash screen.
</li>
<br>
<li>
<u>01.09.2023</u><br>
- Discovered that the folder where evolution graphs are stored, which is empty when the app is not running, wasn't being uploaded to Git, thus preventing the app from starting. Now, if the folder doesn't exist, it is created in the DataProcessing.createSalaryGraphForEverybody() function, since it's the first graph drawing function to be called when the app first starts, thus assuring that the folder exists.<br>
- Considering the above fix, now the Graphs folder isn't emptied out upon exit anymore, but deleted as a whole instead.<br>
- The application is now more resilient if something happens to one or all of the graphs or the folder where they're saved. If that happens, the app recreates the folder and the graphs that were drawn up until the point of failure, plus the needed graphs requested by the user. That way, the app is now able to recover from this kind of failure.<br>
- Fixed a bug that was preventing analyses from being written when a currency other than the USD was being used and predictions were created.<br>
- Fixed a mistake that allowed the salary column in the statistics table on the evolution graphs screen to change according to the salary's length and not stay fixed to a third of the VBox parent and shorten the salary when predictions were created, thus displaying a horizontal scroll bar below the table and creating visual glitches.<br>
- Replaced checks of empty strings using <b>equals("")</b> with <b>isEmpty()</b>.<br>
- Added an application integrity check that checks if the application has all the files needed for it to run, and if not, then an error is shown in the default look and the app start is cancelled.<br>
- The app now ships without a UserSettings file, and it's generated with the default settings if it doesn't exist (the app will be in English, in dark mode, and will be using USD as the currency). The exchange rates will be updated when the application starts.<br>
- Removed the unused Google GSON Maven dependency.
</li>
<br>
<li>
<u>24.08.2023</u> - big update day!<br>
- Fixed the bug that was preventing the app from starting.<br>
- Added a new splash screen with the app icon, the app title in English/French/Romanian (according to the user's preferred language) and a spinning wheel animation. The splash screen follows the user's preferred theme. The app now appears on the screen faster while it processes the dataset and prepares everything in the background, so it won't take long to display after starting it on slower systems, and there's something displayed while the data and resources are being prepared.<br>
- Two new currencies are available in the app: the Euro (EUR) and the Romanian New Leu (RON). The user can choose in which one of the 3 available currencies to view th graphs, stats and interpretations, and the app will either use the original values - which come in the United States Dollar (USD) - or convert the USD values in the currency the user chose in the picker. The exchange rates are provided by <a href="https://www.exchangerate-api.com/">ExchangeRate-API</a>. When the user changes the app language, the currency also changes to the currency tied to that language (English - USD; French - EUR; Romanian - RON), and can be easily changed through the picker.<br>
- Added a link on the main menu screen that opens a popup describing the sources of data (statistics and exchange rates). Each has a link that opens the webpage of the source in the OS' default browser, and the latest data obtained from that source.<br>
- Fixed the theme switching animation not playing after the app reloads the main menu or switches back to it when the new dataset finishes downloading and was processed.<br>
- Made the theme toggles accessible when a popup is displayed on the screen.<br>
- Made the title of the PDF report to display the earliest and latest year of statistics taken from the dataset, so the year 1960 (lower limit of salary statistics available on 24.08.2023) isn't hardcoded anymore. That way, if a dataset with statistics before 1960 is found, the title of the PDF doesn't still display 1960.<br>
- Fixed sending the PDF report through e-mail when the app does contain internally the e-mail address and password the e-mail should be sent from. Now, the app won't ask for the destination e-mail when first attempting to send it, and then saying that there is no source credentials and asking the user for their e-mail address and password when trying to send the e-mail again afterwards. Instead, it only asks for the destination e-mail address at each send attempt.<br>
- A wait popup is displayed when changing the currency or the language of the app, since the dataset is reprocessed with the new currency and the graphs and predictions are regenerated. That way, the app doesn't freeze on slower systems.<br>
- The app is now more colourful! Replaced in-code emojis with pictures of the actual emojis (used by Microsoft across its products), obtained with <a href="https://emojipedia.org/">Emojipedia</a>. That way, the icons of the buttons on the main screen have colour. Also, the arrows in the navigation links are now full emojis and look better, the theme toggle has colourful icons and the colour of the button doesn't change anymore, but the icons of the buttons each get bigger to indicate the current theme. On the interpretations screen, the slim emojis were replaced by full emojis for men and women, and the gender gap icon is now the app's icon. Finally, the colours of the titles were changed to match the outline colour of their respective icon.<br>
- Small code restructuration.
</li>
<br>
<li>
<u>19.07.2023</u><br>
- <s>Added a currency dropdown that converts all the salaries in the dataset from USD to EUR and RON. It uses data from the <a href="https://www.exchangerate-api.com/">ExchangeRate API</a>.</s> <u>Update 24.08.2023: </u>Turns out that this feature wasn't actually implemented and a broken build of the app was uploaded to Git.<br>
- Slight cosmetic changes. <br>
- Removed commented and unused code.
</li>
<br>
<li>
<u>12.07.2023</u><br>
- Translated in French and Romanian the prompt
asking the user for their email and password to send the report to their own email address and through it.
</li>
<br>
<li>
<u>09.07.2023</u><br>
- Git specific: removed all the commits for security purposes, and moved this project to a public repository.
</li>
<br>
<li>
<u>07.07.2023</u><br>
- Fixed the lack of animation on prompt opening or theme switching on the main menu
after attempting to email the PDF report.<br>
- Added a prompt that asks the user for an Outlook account from and to which send the PDF report,
if the project is downloaded from Git, which misses the default email and password (English only, for now).
</li>
<br>
<li>
<u>04.07.2023</u><br>
- Fixed a bug
that was allowing the user
to set the lower limit of the interval as a year that is beyond the lower limit of the dataset through the text field,
and the app would generate a graph beginning with the specified year.<br>
- Removed the programming of the "See graph for this range"
button from the <b>initialize()</b> function and moved everything into the new <b>processRange()</b> function which is
bound to the button in the <b>DisplayEvolutionGraph</b> files, to follow the model of setting up the buttons in the app
like it is in the rest of the app, through functions and not through the <b>setOnAction()</b> method.<br>
- Removed the fx:id of the "See graph for this range"
button on the <b>DisplayEvolutionGraph</b> files, since it is no longer needed.<br>
- Applied the previous 2 changed to the OK button in the "Predictions discarded"
prompt boxes in the <b>MainMenu</b> files.
</li>
<br>
<li>
<u>02.07.2023</u><br>
- Fixed updating the window title in the current language after changing the app language,
so that the title doesn't stay as the one in the language the app was in when the window was opened.<br>
- Added icons that will be shown on the taskbar for the alerts.<br>
- Changed the formulation of some code explanations.<br>
- Changed the formulation of the header text in the data inclusion confirmation box
that appears before sending an email,
from "user data/détails d'utilisateur/datelor utilizatorului"
to "validation data/détails de validation/datelor de validare".
In the same box,
also changed part of the content text in all languages to reflect the change from user data to validation data.
</li>
<br>
<li>
<u>01.07.2023</u><br>
- Separated the dark overlay from the main menu prompt/confirmation/waiting boxes,
and now there's only one in the whole menu.
It still shows only with the said boxes, but internally there isn't an overlay wrapped with each box anymore.<br>
- Repurposed the existing AnimatedSwitcher to animate just the prompt/confirmation/waiting boxes.<br>
- Added a new AnimatedSwitcher to animate the dark overlay fading in.<br>
- Changed the boxes' animation from a fade in to a zoom in.<br>
- Applied the zoom also for the prediction deletion confirmation box.<br>
- Applied the changes and animations above for the main menu in French and Romanian too.<br>
- Made the mail button traversable by the tab key upon closing the prediction deletion confirmation box.<br>
- Removed unnecessary instructions in code
(e.g.: making the main menu buttons not traversable by the tab key after submitting a valid prediction value,
before showing the waiting box
and beginning the prediction. The main menu buttons were set
to be not traversable before showing the prediction input prompt).<br>
- Changed the visibility of the 2 <b>AnimatedSwitcher</b>s from public to private and attached the <b>@FXML</b> annotation to align with the rest of the class' JavaFX objects being private and accessible inside it only.<br>
- Git specific: deleted the <b>main-branch</b> branch, since it was no longer used, and replaced it with the <b>master</b> branch that includes all the changes and fixes.<br>
- Updated the README to include this changelog, which will be updated with every commit.
Also changed the introduction paragraph of the README.
</li>
<br>
<li>
<u>30.06.2023</u><br>
- Fixed theme switching transition
so that the transition plays when switching themes after changing the display language too.<br>
- Added a transition when navigating between the graph and the data interpretation pages.<br>
- Added a transition when invoking the prediction and the email prompts on the main menu.<br>
</li>
<br>
<li>
<u>26.06.2023</u><br>
- Introduced a transition to smoothen the switch between dark mode and light mode<br>
- Indented the code in DataProcessing.createPDF()<br>
- Added a little, empty, nonfunctional hyperlink in all the FXML files, at the top of the hierarchy,
so that when a page displays, none of the visible elements on the page that are focus traversable are initially focused,
so that the page is displayed with nothing highlighted<br>
- Added more comments and explications in the SendEmailInBackground class
</li>
<br>
<li>
<u>24.06.2023</u><br> 
- Changed the way that DataProcessing.createPDF() functions,
so that the PDF report does not get opened from within this function,
but from Main.generatePDF(),
so that the report doesn't open anymore when the user requests to have it sent by email.<br>
- Changed the alerts throughout the app to also have dark mode when the application is set to dark mode.<br>
- Changed the alerts to not have the operating system's window decorations
(title bar, borders) so that the title bar won't be white on an alert which is in dark mode.
</li>
</ul>
