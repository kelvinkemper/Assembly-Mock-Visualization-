import javafx.animation.PauseTransition;
import javafx.geometry.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutorService;



public class AssemblySilosGUI {
    private BorderPane root;
    private GridPane siloContainer;
    private SiloGUI[][] arraysOfSiloGUIs;
    private Font font;
    private Parser parser;
    private Interpreter[][] interpreters;
    private Interpreter[] interpretersWithInstructions;
    BorderPane leftDisplay;
    private Limiter limiter;
    private ArrayList<Port> currentPorts;

    private ExecutorService executorService;
    private ArrayList<Thread> interpreterThreads;
    private ArrayList<Thread> portThreads;

    private ArrayList<Port> iPortList;
    private ArrayList<Port> oPortList;
    private ArrayList<VBox> inputValuesList;
    private ArrayList<VBox> inputValuesList2;

    private ArrayList<VBox> outputValuesList;
    private ArrayList<HBox> inputContainerList;
    private ArrayList<HBox> outputContainerList;
    private int inputCounter;

    /**
     Creates the graphical user interface (GUI) for the silo system, given the number of rows and columns for the silos,
     the main parser object, and a 2D array of interpreter objects for each silo.
     @param numRows the number of rows for the silos
     @param numColumns the number of columns for the silos
     @param mainParser the main parser object used for interpreting user input
     @param siloInterpreters the 2D array of interpreter objects for each silo
     @throws FileNotFoundException if the specified font file is not found
     */
    public void createGUI(int numRows, int numColumns, Parser mainParser, Interpreter[][] siloInterpreters) throws FileNotFoundException {
        interpreters = siloInterpreters;
        parser = mainParser;
        root = new BorderPane();
        root.setStyle("-fx-background-color: black;");
        font = Font.loadFont(getClass().getClassLoader().getResource("font/font.ttf").toString(), 16);
        inputCounter = 1;
        leftDisplay();
        // Create silos and store references to them in a 2D array
        arraysOfSiloGUIs = createSilos(numRows, numColumns);
        // Add silos to container
        siloContainer = new GridPane();
        siloContainer.setHgap(10);
        siloContainer.setVgap(10);
        siloContainer.setPadding(new Insets(10));
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numColumns; col++) {
                siloContainer.add(arraysOfSiloGUIs[row][col], col, row + 1);
            }
        }
        root.setCenter(siloContainer);
    }
    /**
     Returns the root Pane of the GUI created by this object. This Pane contains all the components
     and layout of the GUI.
     @return the root Pane of the GUI created by this object
     */
    public Pane getGUI() {
        return root;
    }
    /**
     Creates a 2D array of SiloGUI objects with the specified number of rows and columns.
     Sets the top, left, right, and bottom ports of each silo as necessary based on the row and column number.
     Sets the text area of each silo with the next instruction from the Parser's instruction list.
     Associates each SiloGUI object with its corresponding Interpreter object from the siloInterpreters array.
     @param numRows the number of rows in the 2D array of silos to be created
     @param numCols the number of columns in the 2D array of silos to be created
     @return the 2D array of SiloGUI objects with the specified number of rows and columns
     */
    private SiloGUI[][] createSilos(int numRows, int numCols) {
        SiloGUI[][] silos = new SiloGUI[numRows][numCols];
        int counter = 0;
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                SiloGUI silo = new SiloGUI();
                if (row != 0) {
                    ImageView topPort = new ImageView(new Image(getClass().getClassLoader().getResource("images/upArrow.png").toString()));
                    Label topTransferValue = new Label();
                    topTransferValue.setText("0");
                    topTransferValue.setVisible(false);
                    topTransferValue.setTextFill(Color.WHITE);
                    HBox port = new HBox(10);
                    port.setAlignment(Pos.CENTER);
                    port.getChildren().addAll(topTransferValue, topPort);
                    silo.setTop(port);
                }
                if (col != 0) {
                    ImageView leftPort = new ImageView(new Image(getClass().getClassLoader().getResource("images/leftArrow.png").toString()));
                    Label leftTransferValue = new Label();
                    leftTransferValue.setText("0");
                    leftTransferValue.setVisible(false);
                    leftTransferValue.setTextFill(Color.WHITE);
                    VBox port = new VBox(10);
                    port.setAlignment(Pos.CENTER);
                    port.getChildren().addAll(leftPort, leftTransferValue);
                    silo.setLeft(port);
                }
                if (col != numCols - 1) {
                    ImageView rightPort = new ImageView(new Image(getClass().getClassLoader().getResource("images/rightArrow.png").toString()));
                    Label rightTransferValue = new Label();
                    rightTransferValue.setText("0");
                    rightTransferValue.setVisible(false);
                    rightTransferValue.setTextFill(Color.WHITE);
                    VBox port = new VBox(10);
                    port.setAlignment(Pos.CENTER);
                    port.getChildren().addAll(rightTransferValue, rightPort);
                    silo.setRight(port);
                }
                if (row != numRows - 1) {
                    ImageView bottomPort = new ImageView(new Image(getClass().getClassLoader().getResource("images/downArrow.png").toString()));
                    Label bottomTransferValue = new Label();
                    bottomTransferValue.setText("0");
                    bottomTransferValue.setVisible(false);
                    bottomTransferValue.setTextFill(Color.WHITE);
                    HBox port = new HBox(10);
                    port.setAlignment(Pos.CENTER);
                    port.getChildren().addAll(bottomPort, bottomTransferValue);
                    silo.setBottom(port);
                }
                if (counter < parser.getTrueInterpreterCount()) {
                    silo.setTextArea(toString(parser.getInstructionList().get(counter)));
                }
                silos[row][col] = silo;
                interpreters[row][col].setSiloGUI(silo);
                interpreters[row][col].setName("Silo " + counter + ": ");
                counter++;
            }
        }
        return silos;
    }
    /**
     Sets the array of interpreters with instructions to be used in the simulation.
     @param trueInterpreters an array of Interpreter objects with instructions
     */
    public void setTrueInterpreterArray(Interpreter[] trueInterpreters) {
        interpretersWithInstructions = trueInterpreters;
    }
    /**
     Sets the limiter for the program.
     @param l the Limiter object to be set
     */
    public void setLimiter(Limiter l) {
        limiter = l;
    }
    /**
     Creates and displays the left side of the GUI, including a console for input and output, player controls for executing the code, and input/output values.
     @throws FileNotFoundException if a file input stream cannot be found
     */
    private void leftDisplay() throws FileNotFoundException {
        leftDisplay = new BorderPane();
        Label consoleLabel = new Label(" - CONSOLE - ");
        consoleLabel.setFont(font);
        consoleLabel.setAlignment(Pos.CENTER);
        consoleLabel.setTextFill(Color.WHITE);
        TextArea console = new TextArea();
        console.setFont(font);
        console.setText(toString(parser.getConfigs()));
        console.setStyle("-fx-control-inner-background: black; -fx-text-fill: white; -fx-border-color: white; -fx-border-width: 2px;");
        VBox consoleControls = new VBox(5);
        consoleControls.setAlignment(Pos.CENTER);
        consoleControls.getChildren().addAll(consoleLabel, console);
        leftDisplay.setTop(consoleControls);
        Background bg = new Background(new BackgroundFill(Color.BLACK, null, null));

        HBox playerControls = new HBox(5);

        /*
        FileInputStream inputStop = new FileInputStream("resources/images/stopBtnNotClicked.png");
        FileInputStream inputRun = new FileInputStream("resources/images/run.png");
        FileInputStream inputFast = new FileInputStream("resources/images/fast.png");
        FileInputStream inputStep = new FileInputStream("resources/images/step.png");
        FileInputStream inputPause = new FileInputStream("resources/images/pausebtn.png");
         */
        Button stopButton = new Button("", new ImageView(new Image(getClass().getClassLoader().getResource("images/stopBtnNotClicked.png").toString())));
        Button pauseButton = new Button("", new ImageView(new Image(getClass().getClassLoader().getResource("images/pausebtn.png").toString())));
        Button stepButton = new Button("", new ImageView(new Image(getClass().getClassLoader().getResource("images/step.png").toString())));
        Button fastForwardButton = new Button("", new ImageView(new Image(getClass().getClassLoader().getResource("images/fast.png").toString())));
        Button runButton = new Button("", new ImageView(new Image(getClass().getClassLoader().getResource("images/run.png").toString())));

        stopButton.setBackground(bg);
        stopButton.setOnAction(event -> {
            for (int row = 0; row < parser.getNumRows(); row++) {
                for (int col = 0; col < parser.getNumCols(); col++) {
                    arraysOfSiloGUIs[row][col].getTextArea().setEditable(true);
                }
            }
            inputCounter=0;
            highlightReset();
            resetSetup();
        });
        stepButton.setBackground(bg);
        stepButton.setOnAction(event -> {
            pauseAllSilos();
            limiter.letSilosRun();
        });
        fastForwardButton.setBackground(bg);
        fastForwardButton.setOnAction(event -> {
            for (Interpreter interpreterWithInstruction : interpretersWithInstructions) {
                interpreterWithInstruction.unpauseSilo();
                interpreterWithInstruction.fastForwardSilo();
                limiter.letSilosRun();
            }
        });
        pauseButton.setBackground(bg);
        pauseButton.setOnAction(event -> {
            pauseAllSilos();
            playerControls.getChildren().clear();
            playerControls.getChildren().addAll(stopButton, stepButton, runButton, fastForwardButton);
        });
        runButton.setBackground(bg);
        runButton.setOnAction(event -> {
            for (int row = 0; row < parser.getNumRows(); row++) {
                for (int col = 0; col < parser.getNumCols(); col++) {
                    String[] lines = arraysOfSiloGUIs[row][col].getTextArea().getText().split("\\n");
                    interpreters[row][col].setSiloGUI(arraysOfSiloGUIs[row][col]);
                    interpreters[row][col].setSiloInstructions(lines);
                    arraysOfSiloGUIs[row][col].getTextArea().setEditable(false);
                }
            }

            unpauseAllSilos();
            //for ( Thread t : portThreads ) {
            //}
            limiter.letSilosRun();
            playerControls.getChildren().clear();
            playerControls.getChildren().addAll(stopButton, pauseButton, runButton, fastForwardButton);
        });
        playerControls.getChildren().addAll(stopButton, stepButton, runButton, fastForwardButton);
        playerControls.setAlignment(Pos.BOTTOM_CENTER);
        playerControls.setPadding(new Insets(10, 0, 50, 0));

        leftDisplay.setBottom(playerControls);

        HBox valuesContainer = new HBox(5);
        valuesContainer.setPadding(new Insets(20, 0, 0, 0));
        valuesContainer.setSpacing(10);
        valuesContainer.setAlignment(Pos.CENTER);
        /**
         * input value vbox
         */
        inputValuesList = new ArrayList<>();
        inputValuesList2 = new ArrayList<>();
        for (int i = 0; i < parser.getCollectionOfInputCoordinates().size(); i++) {
            VBox inputValues = new VBox();
            inputValues.setAlignment(Pos.TOP_CENTER);
            VBox inputValuesBox2 = new VBox();
            inputValuesBox2.setAlignment(Pos.TOP_CENTER);
            // inputValues.setStyle("-fx-control-inner-background: black; -fx-text-fill: white; -fx-border-color: white; -fx-border-width: 2px;");
            BorderPane inLabel = new BorderPane();
            inLabel.setPadding(new Insets(1,5,1,5));
            inLabel.setStyle("-fx-control-inner-background: black; -fx-text-fill: white; -fx-border-color: white; -fx-border-width: 2px;");
            Label inputValue = new Label("IN."+(char) (i + 'A'));
            inputValue.setFont(font);
            inputValue.setTextFill(Color.WHITE);
            inputValue.setAlignment(Pos.CENTER);
            inputValue.setFont(font);
            BorderPane inLabel2 = new BorderPane();
            inLabel2.setPadding(new Insets(1,5,1,5));
            inLabel2.setStyle("-fx-control-inner-background: black; -fx-text-fill: white; -fx-border-color: white; -fx-border-width: 2px;");
            Label inputValue2 = new Label("IN."+(char) (i + 'A'));
            inputValue2.setTextFill(Color.WHITE);
            inputValue2.setAlignment(Pos.CENTER);
            inputValue2.setFont(font);
            inLabel2.setCenter(inputValue2);
            inLabel.setCenter(inputValue);
            inputValues.getChildren().add(inLabel);
            inputValuesBox2.getChildren().add(inLabel2);
            for (int j = 0; j < parser.getCollectionOfInputValues().get(i).size(); j++) {
                BorderPane borderPane = new BorderPane();
                borderPane.setPadding(new Insets(1,5,1,5));
                borderPane.setStyle("-fx-control-inner-background: black; -fx-text-fill: white; -fx-border-color: white; -fx-border-width: 2px;");
                Label inputValue1 = new Label(parser.getCollectionOfInputValues().get(i).get(j).toString());
                inputValue1.setFont(font);
                inputValue1.setTextFill(Color.WHITE);
                inputValue1.setAlignment(Pos.CENTER);
                borderPane.setCenter(inputValue1);
                if (j < 26) {
                    inputValues.getChildren().add(borderPane);
                    inputValuesBox2.setVisible(false);
                }
                if (j > 26)
                {
                    inputValuesBox2.setVisible(true);
                    inputValuesBox2.getChildren().add(borderPane);
                }
            }
            inputValuesList.add(inputValues);
            inputValuesList2.add(inputValuesBox2);
            valuesContainer.getChildren().addAll(inputValues,inputValuesBox2);
        }

        outputValuesList= new ArrayList<>();
        for (int i = 0; i < parser.getOutputCoordinatesCollection().size(); i++) {
            VBox outputValues = new VBox();
            outputValues.setAlignment(Pos.TOP_CENTER);
            BorderPane borderPane = new BorderPane();
            borderPane.setPadding(new Insets(1,5,1,5));
            borderPane.setStyle("-fx-control-inner-background: black; -fx-text-fill: white; -fx-border-color: white; -fx-border-width: 2px;");
            Label outputValue = new Label("OUT."+(char) (i + 'A'));
            outputValue.setFont(font);
            outputValue.setTextFill(Color.WHITE);
            outputValue.setAlignment(Pos.CENTER);
            borderPane.setCenter(outputValue);
            outputValues.getChildren().add(borderPane);
            valuesContainer.getChildren().addAll(outputValues);
            outputValuesList.add(outputValues);
        }

        // outputValues.setStyle("-fx-control-inner-background: black; -fx-text-fill: white; -fx-border-color: white; -fx-border-width: 2px;");
        leftDisplay.setLeft(valuesContainer);
        root.setLeft(leftDisplay);
    }
    /**
     * shows what current input is the program, un highlights the
     */
    public void highlightCurrentInput() {
        //   System.out.println(inputValuesList.size());
        for (int i = 0; i < inputValuesList.size(); i++) {
            if (inputCounter == 0)
            {
                break;
            }

            if (inputCounter<26) {
                    for (int j = 0; j < inputValuesList.size(); j++) {
                        if (inputCounter >= inputValuesList.get(j).getChildren().size())
                        {
                            break;
                        }
                        inputValuesList.get(j).getChildren().get(inputCounter).setStyle("-fx-background-color: darkgrey; -fx-border-color: white; -fx-border-width: 2px;");
                        inputValuesList.get(j).getChildren().get(inputCounter - 1).setStyle("-fx-background-color: black; -fx-border-color: white; -fx-border-width: 2px;");
                    }
            }
            else if (inputCounter-26==0) {
                //   inputValuesList.get(0).getChildren().get(inputCounter).setStyle("-fx-background-color: darkgrey; -fx-border-color: white; -fx-border-width: 2px;");
                for (int j = 0; j < inputValuesList.size(); j++) {
                    inputValuesList.get(j).getChildren().get(inputCounter).setStyle("-fx-background-color: darkgrey; -fx-border-color: white; -fx-border-width: 2px;");
                    if (inputCounter >= inputValuesList2.get(j).getChildren().size())
                    {
                        break;
                    }
                    inputValuesList2.get(j).getChildren().get(inputCounter - 1).setStyle("-fx-background-color: black; -fx-border-color: white; -fx-border-width: 2px;");
                }
            }
            else  if (inputCounter-26 >= inputValuesList2.get(0).getChildren().size())
            {
                break;
            }
            else if (inputCounter> 26) {
                // inputValuesList.get(0).getChildren().get(inputValuesList.get(0).getChildren().size()-1).setStyle("-fx-background-color: black; -fx-border-color: white; -fx-border-width: 2px;");
                //   System.out.println(inputValuesList.get(1).getChildren().toString());
                for (int j = 0; j < inputValuesList2.size(); j++) {
                    inputValuesList2.get(j).getChildren().get(inputCounter - 26).setStyle("-fx-background-color: darkgrey; -fx-border-color: white; -fx-border-width: 2px;");
                    inputValuesList2.get(j).getChildren().get(inputCounter - 27).setStyle("-fx-background-color: black; -fx-border-color: white; -fx-border-width: 2px;");
                }

            }
        }
    }

    private void highlightReset() {
        for (int i = 0; i < inputValuesList.size(); i++) {
            for (int j = 0; j < inputValuesList.get(i).getChildren().size(); j++) {
                inputValuesList.get(i).getChildren().get(j).setStyle("-fx-background-color: black; -fx-border-color: white; -fx-border-width: 2px;");
            }
        }
        for (int i = 0; i < inputValuesList2.size(); i++) {
            for (int j = 0; j < inputValuesList2.get(i).getChildren().size(); j++) {
                inputValuesList2.get(i).getChildren().get(j).setStyle("-fx-background-color: black; -fx-border-color: white; -fx-border-width: 2px;");
            }
        }
    }
    /**
     * Displays which output values reach the end of the program. Attaches to the output ports and uses
     * a queue for each output value for each port
     */
    public void showOutStorage() {
        for (int i = 0; i < oPortList.size(); i++) {
            if (oPortList.get(i).getOutputQ().isEmpty()) {
                continue;
            }
            BorderPane borderPane = new BorderPane();
            borderPane.setPadding(new Insets(1,5,1,5));
            borderPane.setStyle("-fx-control-inner-background: black; -fx-text-fill: white; -fx-border-color: white; -fx-border-width: 2px;");
            Label label = new Label(String.valueOf(oPortList.get(i).getOutputQ().remove()));
            label.setFont(font);
            label.setTextFill(Color.WHITE);
            borderPane.setCenter(label);
            outputValuesList.get(i).getChildren().add(borderPane);
        }

        // label.setAlignment(Pos.CENTER);


    }
    /**
     Returns a string representation of the elements in the provided ArrayList.
     The elements are concatenated into a single string, separated by newline characters.
     @param listToString the ArrayList to convert to a string
     @return a string representation of the elements in the ArrayList
     */
    private String toString(ArrayList<String> listToString) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String s : listToString) {
            stringBuilder.append(s).append("\n");
        }
        return stringBuilder.toString();
    }
    /**
     Sets the input location for the parser and adds an arrow icon to the corresponding cell in the UI grid.

     //   @param inputRow the row index of the cell to add the arrow icon to
     //  @param inputColumn the column index of the cell to add the arrow icon to

     @param inputsCoords the coordinations of the input GUI as a list of list
     in case there is more than one input location
     */
    public void setInputLocations(ArrayList<ArrayList<Integer>> inputsCoords ) {
        inputContainerList = new ArrayList<>();
        for ( int i = 0; i < inputsCoords.size(); i++ ) {
            ImageView inputArrow = new ImageView(new Image(getClass().getClassLoader().getResource("images/downArrow.png").toString()));
            HBox inputContainer = new HBox(5);
            inputContainer.setAlignment(Pos.CENTER);
            Label inputLabel = new Label("IN."+(char) (i + 'A'));
            inputLabel.setFont(font);
            inputLabel.setTextFill(Color.WHITE);
            Label inputArrowValue = new Label("  ");
            inputContainer.getChildren().addAll(inputLabel, inputArrow, inputArrowValue);
            int inputRow = inputsCoords.get(i).get(0);
            if ( inputRow < 0 ) {
                inputRow = 0;
            } else if ( inputRow >= parser.getNumRows() ) {
                inputRow = parser.getNumRows() - 1;
            }
            int inputColumn = inputsCoords.get(i).get(1);
            if ( inputColumn < 0 ) {
                inputColumn = 0;
            } else if ( inputColumn > parser.getNumCols()) {
                inputColumn = parser.getNumCols() - 1;
            }
            GridPane.setConstraints(inputContainer, 1, 1, 1, 1, HPos.CENTER, VPos.CENTER);
            siloContainer.add(inputContainer, inputColumn, inputRow);
            inputContainerList.add(inputContainer);
        }
    }
    /**
     Sets the value of the input arrow label to the given Integer value.
     If the input value is null, sets the label text to " ".
     @param input the Integer value to set the label text to
     */
    public void setInputArrowValue(Integer input) {
        for (int i = 0; i < iPortList.size(); i++) {
            if (input != null) {
                inputContainerList.get(i).getChildren().remove(2);
                Label l = new Label(String.valueOf(iPortList.get(i).getData()));
                l.setFont(font);
                l.setTextFill(Color.WHITE);
                inputContainerList.get(i).getChildren().add(l);
            } else {
                inputContainerList.get(i).getChildren().remove(2);
                inputContainerList.get(i).getChildren().add(new Label("  "));
            }
        }
    }
    /**
     Sets the location of the output arrow in the silo grid. Creates an HBox containing a label "OUT.X" and
     an ImageView of a down arrow. Adds this HBox to the silo grid at the specified location.

     // @param outputRow The row index of the location for the output arrow in the silo grid
     //  @param outputColumn The column index of the location for the output arrow in the silo grid
     */
    /*
    public void setOutputLocationsUnused(ArrayList<ArrayList<Integer>> outputsCoords) {
        //System.out.println("OUTPUT LOCATION: "+parser.getOutputLocation());
        for (ArrayList<Integer> outputsCoord : outputsCoords) {
            outputContainer = new HBox(5);
        }
    }
     */

    public void setOutputLocations(ArrayList<ArrayList<Integer>> outputsCoords) {
        outputContainerList = new ArrayList<>();
        for ( int i = 0; i < outputsCoords.size(); i++ ) {
            HBox outputContainer = new HBox(5);

            outputContainer.setAlignment(Pos.CENTER);
            Label outputLabel = new Label("OUT."+ (char) (i + 'A'));
            outputLabel.setFont(font);
            outputLabel.setTextFill(Color.WHITE);
            Label outputArrowValue = new Label("  ");
            ImageView outputArrow = new ImageView(new Image(getClass().getClassLoader().getResource("images/downArrow.png").toString()));
            outputContainer.getChildren().addAll(outputLabel, outputArrow, outputArrowValue);
            GridPane.setConstraints(outputArrow, 1, 1, 1, 1, HPos.CENTER, VPos.CENTER);

            int outputRow = outputsCoords.get(i).get(0)+1;
            int outputColumn = outputsCoords.get(i).get(1);

            siloContainer.add(outputContainer, outputColumn, outputRow);
            outputContainerList.add(outputContainer);
        }
    }
    /**
     Sets the value of the output arrow label to the given Integer value. If the value is not null,
     the label will display the Integer value as a string. If the value is null, the label will display
     an empty space.
     */
    public void setOutputArrowValue() {
//        for (int i = 0; i < oPortList.size(); i++) {
//            outputContainerList.get(i).getChildren().remove(2);
//            Label l = new Label(String.valueOf(oPortList.get(i).getData()));
//            l.setFont(font);
//            l.setTextFill(Color.WHITE);
//            outputContainerList.get(i).getChildren().add(l);
//            PauseTransition pauseTransition = new PauseTransition(Duration.seconds(1));
//            pauseTransition.setOnFinished(event -> {
//                l.setText("  ");
//            });
//            pauseTransition.play();
//        }
    }
    /**
     Returns the 2D array of SiloGUI objects representing the silo grid.
     @return a 2D array of SiloGUI objects representing the silo grid.
     */
    public SiloGUI[][] getArraysOfSiloGUIs() {
        return arraysOfSiloGUIs;
    }
    /**
     Pauses all silos in the interpreters 2D array by calling their pauseSilo() method.
     */
    private void pauseAllSilos() {
        for (int i = 0; i < parser.getNumRows(); i++) {
            for (int j = 0; j < parser.getNumCols(); j++) {
                interpreters[i][j].pauseSilo();
            }
        }
    }

    private void setExecutorService( ExecutorService eService ) {
        executorService = eService;
    }

    public void setInterpreterThreads( ArrayList<Thread> threadArrayList) {
        interpreterThreads = threadArrayList;
    }

    public void setPortThreads(ArrayList<Thread> threadArrayList ) {
        portThreads = threadArrayList;
    }
    /**
     Unpauses all silos by calling the unpauseSilo() method for each silo in the interpreters array.
     */
    private void unpauseAllSilos() {
        for (int i = 0; i < parser.getNumRows(); i++) {
            for (int j = 0; j < parser.getNumCols(); j++) {
                interpreters[i][j].unpauseSilo();
            }
        }
    }
    /**
     Stops all silos by calling the stopSilo() method on each interpreter in the interpreters array.
     */
    private void resetSetup(){
        for (Thread t : interpreterThreads ) {
            t.interrupt();
        }

        for (Thread t : portThreads ) {
            t.interrupt();
        }

        for ( Port p : currentPorts ) {
            p.resetPort();
            p.setAssemblySiloGUI(AssemblySilosGUI.this);
        }
    }

    private void resetCurrentPorts() {
        for ( int i = 0; i < currentPorts.size(); i++ ) {
            currentPorts.get(i).resetPort();
            currentPorts.get(i).setAssemblySiloGUI(AssemblySilosGUI.this);
        }
    }

    //  private void wakeAllPorts() {
    //     for ( int i = 0; i < currentPorts.size(); i++ ) {
    //        currentPorts.get(i).breakWait();
    //     }
    // }
    /**
     Fast-forwards all silos in the simulation by calling the fastForwardSilo() method on each interpreter object.
     */
    private void fastForwardAllSilos() {
        for (int i = 0; i < parser.getNumRows(); i++) {
            for (int j = 0; j < parser.getNumRows(); j++) {
                interpreters[i][j].fastForwardSilo();
            }
        }
    }
    /**
     Returns the VBox object that contains the output display.
     @return the VBox object that contains the output display
     */
    public ArrayList<VBox> getOutputVBox() {
        HBox HBox = (HBox) leftDisplay.getLeft();
        VBox rightVBox = (VBox) HBox.getChildren().get(HBox.getChildren().size()-2);
        VBox rightVBox2 = (VBox) HBox.getChildren().get(HBox.getChildren().size()-1);
        ArrayList<VBox> toSend = new ArrayList<>();
        toSend.add(rightVBox);
        toSend.add(rightVBox2);
        return toSend;
    }
    /**
     * Returns the VBox containing all the inputs entered by the user.
     * @return the VBox containing all the inputs entered by the user.
     */
    public VBox getInputVBox() {
        HBox HBox = (HBox) leftDisplay.getLeft();
        VBox leftVBox = (VBox) HBox.getChildren().get(0);
        return leftVBox;
    }
    /**
     Sets the list of current ports to the specified list of ports.
     @param x the list of ports to set as the current ports
     */
    public void setPorts( ArrayList<Port> x ) {
        currentPorts = x;
    }
    /**
     * gives the AssemblySiloGUI access to each input ports if there are more than 1
     * @param inputPorts list of input ports
     */
    public void setInputPort(ArrayList<Port> inputPorts) {
        iPortList = inputPorts;
    }
    /**
     * gives the AssemblySiloGUI access to each input ports if there are more than 1
     * @param outputPorts list of output ports
     */
    public void setOutputPort(ArrayList<Port> outputPorts){
        oPortList = outputPorts;
    }
    public void setInputCounter(int counter) {
        inputCounter= counter;
    }


}