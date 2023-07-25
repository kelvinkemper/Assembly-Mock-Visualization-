import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 * This class sets up the silos and ports, as well as their threads and links them all together.
 */
public class Main extends Application {
    private final Parser parser = new Parser();
    private final AssemblySilosGUI AssemblySilosDisplay = new AssemblySilosGUI();
    private ArrayList<Thread> interpreterThreads = new ArrayList<>();
    private ArrayList<Thread> portThreads = new ArrayList<>();
    private Interpreter[] interpretersWithInstructions;
    private Interpreter[][] allInterpreters;
    private ArrayList<Port> currentPorts;
    private ArrayList<Port> allInputs = new ArrayList<>();
    private ArrayList<Port> allOutputs = new ArrayList<>();
    private final Limiter limiter = new Limiter();
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * This method gets input from the user and sets up the silos and the GUI according to their specifications.
     */
    @Override
    public void start(Stage primaryStage) throws FileNotFoundException {
        //read from parser
        parser.runFromCommandLine();
        allInterpreters = createInterpreterGrid();
        int numRows = parser.getNumRows();
        int numColumns = parser.getNumCols();
        setInputPorts();
        setOutputPort();
        AssemblySilosDisplay.setTrueInterpreterArray( interpretersWithInstructions );
        AssemblySilosDisplay.setLimiter(limiter);
        AssemblySilosDisplay.createGUI(numRows, numColumns, parser, allInterpreters);
        AssemblySilosDisplay.setInputLocations( parser.getCollectionOfInputCoordinates() );
        AssemblySilosDisplay.setOutputLocations( parser.getOutputCoordinatesCollection() );
        AssemblySilosDisplay.setOutputPort(allOutputs);
        AssemblySilosDisplay.setInputPort(allInputs);
        for (Port allOutput : allOutputs) {
          //  allOutput.setVbox(AssemblySilosDisplay.getOutputVBox());
            allOutput.setVboxList(AssemblySilosDisplay.getOutputVBox());
            allOutput.setAssemblySiloGUI(AssemblySilosDisplay);
        }
        for (Port allInput : allInputs) {
            allInput.setInputVBox(AssemblySilosDisplay.getInputVBox());
            allInput.setAssemblySiloGUI(AssemblySilosDisplay);
        }
        runInterpretersAndPorts();
        AssemblySilosDisplay.setInterpreterThreads( interpreterThreads );
        AssemblySilosDisplay.setPortThreads( interpreterThreads );
        initializeStage(primaryStage);
    }

    /**
     * This method just sets up the scene to be displayed.
     */

    private void initializeStage(Stage primaryStage) {
        // Get the dimensions of the user's screen
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        // Create a new StackPane layout
        Pane root = AssemblySilosDisplay.getGUI();
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        scrollPane.setContent(root);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        // Create a new Scene and set the root node
        Scene scene = new Scene(scrollPane, screenBounds.getWidth(), screenBounds.getHeight());
        // Set the scene for the primary stage
        primaryStage.setScene(scene);
        // Show the primary stage
        primaryStage.setTitle("Assembly Silos");
        primaryStage.show();
        primaryStage.setOnCloseRequest(t -> {
            Platform.exit();
            System.exit(0);

        });
    }

    /**
     * This method creates the interpreters, adds them to arrays, and also creates their ports and links them all
     * together accordingly
     */
    private Interpreter[][] createInterpreterGrid() {
        Interpreter[][] interpreters = new Interpreter[parser.getNumRows()][parser.getNumCols()];
        interpretersWithInstructions = new Interpreter[parser.getTrueInterpreterCount()];
        int numOfPorts = (parser.getNumRows() * (parser.getNumCols() + 1)) + ((parser.getNumCols() + 1) * parser.getNumRows());
        currentPorts = new ArrayList<>(numOfPorts);
        int counter = 0;
        for (int row = 0; row < parser.getNumRows(); row++) {
            for (int col = 0; col < parser.getNumCols(); col++) {
                if ( counter < parser.getSiloInstructsStrings().length ) {
                    interpreters[row][col] = new Interpreter(parser.getSiloInstructsStrings()[counter]);
                    interpretersWithInstructions[counter] = interpreters[row][col];
                    counter++;
                } else {
                    interpreters[row][col] = new Interpreter();
                }
                if (col == 0) {
                    Port leftMostPort = new Port();
                    interpreters[row][col].setLeft(leftMostPort);
                    currentPorts.add(leftMostPort);
                }
                if (parser.getNumCols() > 1 && col == parser.getNumCols() - 1) {
                    Port rightMostPort = new Port();
                    interpreters[row][col].setRight(rightMostPort);
                    currentPorts.add(rightMostPort);
                } else if (parser.getNumCols() == 1) {
                    Port rightPort = new Port();
                    interpreters[row][col].setRight(rightPort);
                    currentPorts.add(rightPort);
                }
                if (row == 0) {
                    Port topMostPort = new Port();
                    interpreters[row][col].setTop(topMostPort);
                    currentPorts.add(topMostPort);
                }
                if (row == parser.getNumRows() - 1) {
                    Port bottomMostPort = new Port();
                    interpreters[row][col].setDown(bottomMostPort);
                    currentPorts.add(bottomMostPort);
                }
                interpreters[row][col].setLimiter(limiter);
            }
        }
        for (int row = 0; row < parser.getNumRows(); row++) {
            for (int col = 0; col < parser.getNumCols(); col++) {
                if (col > 0) {
                    Port middlePort = new Port();
                    Interpreter leftSilo = interpreters[row][col - 1];
                    Interpreter rightSilo = interpreters[row][col];
                    middlePort.linkLeftAndRight(leftSilo, rightSilo);
                    currentPorts.add(middlePort);
                }

                if ( parser.getNumRows() > 1 && row + 1 < parser.getNumRows() ) {
                    Port topAndBottomPort = new Port();
                    Interpreter topSilo = interpreters[row][col];
                    Interpreter bottomSilo = interpreters[row + 1][col];
                    topAndBottomPort.linkTopAndBottom(topSilo, bottomSilo);
                    currentPorts.add(topAndBottomPort);
                }
            }
        }
        AssemblySilosDisplay.setPorts(currentPorts);
        return interpreters;
    }

    /**
     * This method creates the threads needed for the silos and ports and runs them.
     */
    private void runInterpretersAndPorts() {
        for (Interpreter[] allInterpreter : allInterpreters) {
            for (Interpreter interpreter : allInterpreter) {
                Thread thread = new Thread(interpreter);
                thread.start();
                interpreterThreads.add(thread);
            }
        }

        for (Port p : currentPorts) {
            Thread thread = new Thread(p);
            thread.start();
            portThreads.add(thread);
            p.setLimiter(limiter);
        }
    }

    /**
     * This method uses the user's specified coordinates for the inputs to set up the GUI representation of them.
     */
    private void setInputPorts() {
        ArrayList<ArrayList<Integer>> allInputValues = parser.getCollectionOfInputValues();
        ArrayList<ArrayList<Integer>> allInputCoordinates = parser.getCollectionOfInputCoordinates();
        for ( int i = 0; i < allInputCoordinates.size(); i++ ) {
            int inputRowCoordinate = allInputCoordinates.get(i).get(0);
            int inputColumnCoordinate = allInputCoordinates.get(i).get(1);
            if ( inputRowCoordinate < 0 ) {
                allInterpreters[0][inputColumnCoordinate].getTop().enableIsAnInput( allInputValues.get(i) );
                allInputs.add( allInterpreters[0][inputColumnCoordinate].getTop() );
            } else if ( inputRowCoordinate >= parser.getNumRows() ) {
                allInterpreters[0][inputColumnCoordinate].getDown().enableIsAnInput( allInputValues.get(i) );
                allInputs.add( allInterpreters[0][inputColumnCoordinate].getDown() );
            } else if ( inputColumnCoordinate < 0 ) {
                allInterpreters[inputRowCoordinate][0].getLeft().enableIsAnInput( allInputValues.get(i) );
                allInputs.add( allInterpreters[inputRowCoordinate][0].getLeft() );
            } else if ( inputColumnCoordinate >= parser.getNumCols() ) {
                allInterpreters[inputRowCoordinate][parser.getNumCols()-1].getRight().enableIsAnInput( allInputValues.get(i) );
                allInputs.add( allInterpreters[inputRowCoordinate][parser.getNumCols()-1].getRight() );
            }
        }
    }

    /**
     * This method uses the user's specified coordinates for the outputs to set up the GUI representation of them.
     */
    private void setOutputPort() {
        ArrayList<ArrayList<Integer>> allOutputCoordinates = parser.getOutputCoordinatesCollection();
        for ( int i = 0; i < allOutputCoordinates.size(); i++ ) {
            int inputRowCoordinate = allOutputCoordinates.get(i).get(0);
            int inputColumnCoordinate = allOutputCoordinates.get(i).get(1);
            if ( inputRowCoordinate < 0 ) {
                allInterpreters[0][inputColumnCoordinate].getTop().enableIsAnOutput();
                allOutputs.add( allInterpreters[0][inputColumnCoordinate].getTop() );
            } else if ( inputRowCoordinate >= parser.getNumRows() ) {
                allInterpreters[parser.getNumRows()-1][inputColumnCoordinate].getDown().enableIsAnOutput();
                allOutputs.add( allInterpreters[parser.getNumRows()-1][inputColumnCoordinate].getDown() );
            } else if ( inputColumnCoordinate < 0 ) {
                allInterpreters[inputRowCoordinate][0].getLeft().enableIsAnOutput();
                allOutputs.add( allInterpreters[inputRowCoordinate][0].getLeft() );
            } else if ( inputColumnCoordinate >= parser.getNumCols() ) {
                allInterpreters[inputRowCoordinate][parser.getNumCols()-1].getRight().enableIsAnOutput();
                allOutputs.add( allInterpreters[inputRowCoordinate][parser.getNumCols()-1].getRight() );
            }
        }
    }
}