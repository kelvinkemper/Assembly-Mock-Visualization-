import javafx.scene.layout.BorderPane;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Parser {
    private ArrayList<ArrayList<String>> instructionList;
    private int numberOfSilos;
    private ArrayList<String> configs;
    private int configSize;
   // private ArrayList<Integer> inputValues;
    private ArrayList<Integer> inputValueStartingIndexPositions = new ArrayList<>();
    private ArrayList<ArrayList<Integer>> collectionOfInputValues = new ArrayList<>();
    private ArrayList<ArrayList<Integer>> collectionOfInputCoordinates = new ArrayList<>();
    private ArrayList<Integer> inputEndpoints = new ArrayList<>();
    private ArrayList<Integer> outputIndexes = new ArrayList<>();
    private ArrayList<ArrayList<Integer>> outputCoordinatesCollection = new ArrayList<>();
    private int numRows;
    private int numCols;
    private int numSilosWithInstructions;
    Parser() {
        configs = new ArrayList<>();
    }
    /**
     This method reads the input from a file named "example_input1.txt" located in the "resources" directory
     and stores the input configurations into the ArrayList "configs". Then, it calls the "runAfterConfigsGathered()"
     method to process the gathered configurations and initialize the program variables.
     */
    public void runFromFile() {
        // Initially taking empty string
        String text = "";
        try {
            // Creating a FileReader object so as to get the directory of file to be read
            FileReader readfile = new FileReader("resources/example_input1.txt");
            BufferedReader readbuffer = new BufferedReader(readfile);
            while ((text = readbuffer.readLine()) != null) {
                configs.add(text);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        runAfterConfigsGathered();
    }
    /**
     Reads the input configuration data from the command line and stores it in the configs ArrayList.
     Continuously prompts the user to enter input until "OUTPUT" is encountered, after which it adds the final
     two lines of input data to the ArrayList as well. Then, runs the runAfterConfigsGathered method to
     process the configuration data and set the necessary class fields.
     */
    public void runFromCommandLine() {
        // System.out.println("\nEnter \".\" after entering SILO setup instructions.");
        Scanner sc = new Scanner(System.in);
        String input = "";
        while(!input.equalsIgnoreCase(".")) {
            input = sc.nextLine();
            if ( ! input.equalsIgnoreCase(".")) {
                configs.add(input);
            }
        }
        if (configsLegal()) {
            sc.close();
            runAfterConfigsGathered();

        } else {
            System.out.println("Illegal word detected. Try Again.");
            configs.clear();
            runFromCommandLine();
        }

//        while(!input.equalsIgnoreCase(".")) {
//            input = sc.nextLine();
//            if ( ! input.equalsIgnoreCase(".")) {
//                configs.add(input);
//            }
//        }

    }

    /**
     * Checks from a list of legal works that are allowed in the program.
     * If there happens to be a label, this will be added to the legalword list.
     * @return bool
     */
    public boolean configsLegal() {
        List<String> legalWords = new ArrayList<>();
        List<String> checker = new ArrayList<>();
        legalWords.addAll(Arrays.asList("MOVE", "END", "UP","RIGHT","DOWN","LEFT","ACC","BAK","NIL","SWAP","NOOP",
                "SAVE","ADD","SUB","NEGATE","JUMP","JEZ","JNZ","JGZ","JLZ","JRO"," ","","INPUT","OUTPUT"));
        String s = "";
        String tempy = "";
        for (int i = 0; i < configs.size(); i++) {
            s = configs.get(i);
            if (s.contains(":")) {
                s = s.replaceAll("[^a-zA-Z]", "");
                legalWords.add(s);
            }
            if (s.contains("-")) {
                continue;
            }
            s = s.replaceAll("\\d", "");
            tempy += s + " ";
        }String[] tempy2 = tempy.split("\\W");
        for (int i = 0; i < tempy2.length; i++) {
            tempy2[i].trim();
            checker.add(tempy2[i]);
        }

        for (int i = 0; i < checker.size(); i++) {
            if (!legalWords.contains(checker.get(i))) {
                return false;
            }
        }
        return true;
    }



    /**
     This method takes in a String input from the GUI and sets it as the configuration to be run. It then calls the
     'runAfterConfigsGathered()' method to process the configuration and set the necessary fields and variables.
     @param s a String input containing the configuration to be run
     */
    public void runFromGUIInput(String s) {
        configs = new ArrayList<>(Arrays.asList(s.split("\n")));
        runAfterConfigsGathered();
    }
    /**
     This method is called after gathering all configurations. It sets the class variables of configSize,
     numRows, numCols, numberOfSilos, instructionList, inputIndex, inputLocation, outputLocation, inputValues,
     and numSilosWithInstructions. It calls various helper methods to populate these variables with the relevant
     values extracted from the configurations.
     */
    private void runAfterConfigsGathered() {
        configSize = configs.size();
        String rowsCol = configs.get(0);
        ArrayList<Integer> rowColInts = convertStringsToInteger(rowsCol);
        numRows = rowColInts.get(0);
        numCols = rowColInts.get(1);
        numberOfSilos = numRows * numCols;
        instructionList = new ArrayList<>();
        //printAllConfigs(configs);
        findInputs();
        findOutputsIndexes();
     //   findInputLocation();
      //  findOutputLocation();
        findInputValuesAndCoordinates();
        findOutputCoordinates();
        findTrueInterpreterCount();
        createInstructionList();

    //    for (int i = 0; i < instructionList.size(); i++) {
    //        System.out.println("Silo " + (i+1) + " instructions as list: " + instructionList.get(i));
    //    }
    //    getSiloInstructsSB();
    }

    /**
     * This method searches for the user's specified output coordinates and marks their spot in the outputCoordinatesCollection
     */
    private void findOutputCoordinates() {
        String outputCoordinatesString = "";
        for (int i = 0; i < outputIndexes.size(); i++) {
            outputCoordinatesString = configs.get( outputIndexes.get(i) );
            ArrayList<Integer> outputCoordinates = convertStringsToInteger(outputCoordinatesString);
            outputCoordinatesCollection.add( outputCoordinates );
        }
    }

    /**
     Creates a list of instructions for each silo with instructions.
 //    @param numberOfSilos the total number of silos in the configuration file
     */
    private void createInstructionList() {
        for (int i = 0; i < numSilosWithInstructions; i++) {
            ArrayList<String> instructionsSet = new ArrayList<>();
            instructionList.add(instructionsSet);
        }
        for (int i = 0; i < numSilosWithInstructions; i++) {
            for (int j = 1; j < inputValueStartingIndexPositions.get(0)-2; j++) {
                if (!configs.get(j).contains("END") &&  i < instructionList.size() ) {
                    instructionList.get(i).add(configs.get(j));
                } else {
                    i++;
                }
            }
        }
    }
    /**
     Finds the number of silos with valid instructions and sets the instance variable numSilosWithInstructions accordingly.
     */
    public void findTrueInterpreterCount() {
        numSilosWithInstructions = 0;
        for (int j = 1; j < inputValueStartingIndexPositions.get(0) - 2; j++) {

            if (configs.get(j).contains("END")) {
                numSilosWithInstructions++;
            }
        }
    }
    /**
     Returns the number of silos with valid instructions.
     @return the number of silos with valid instructions
     */
    public int getTrueInterpreterCount() {
        return numSilosWithInstructions;
    }
    /**
     Returns the instructions for each silo as a 2D array of strings.
     @return the instructions for each silo as a 2D array of strings
     */
    public String[][] getSiloInstructsStrings() {
        String [][] arr = new String[instructionList.size()][];
        int i = 0;
        for (ArrayList<String> eachSilo : instructionList) {
            arr[i++] = eachSilo.toArray(new String[eachSilo.size()]);
        }
      //  String[][] instructionListSA =
        //  instructionList.stream().map(l-> l.toArray(new String[instructionList.size()])).toArray(String[][]::new);

       // System.out.println(Arrays.deepToString(arr));
        return arr;
    }
    /**
     Converts an ArrayList of strings to an array of strings.
     @param stringsList the ArrayList of strings to be converted
     @return the array of strings
     */
    private String[] convertToStringArray(ArrayList<String> stringsList) {
        String[] s = new String[stringsList.size()];
        for (int i = 0; i < stringsList.size(); i++) {
            s[i] = stringsList.get(i);
        }
        return s;
    }
    /**
     Finds and returns the index of the line that contains the input configuration.
     @return the index of the line that contains the input configuration
     */
    private void findInputs() {
        for (int i = 0; i < configSize; i++) {
            if (configs.get(i).contains("INPUT")) {
                int j = i;
                int inputIndex = i+2;
                inputValueStartingIndexPositions.add(inputIndex);
                while ( ! configs.get(j).contains("END") ) {
                    j++;
                }
                inputEndpoints.add(j);
            }
        }
    }
    /**
     Finds and returns the index of the line that contains the output configuration.
     @return the index of the line that contains the output configuration
     */
    private int findOutputIndex() {
        int outIndex = 0;
        for (int i = 0; i < configSize; i++) {
            if (configs.get(i).contains("OUTPUT")) {
                outIndex = i+1;
            }
        }
        return outIndex;
    }

    /**
     * This method searches for the user's specified output coordinates and adds their index spot in the configs array.
     */
    private void findOutputsIndexes() {
        int outIndex = 0;
        for (int i = 0; i < configSize; i++) {
            if (configs.get(i).contains("OUTPUT")) {
                outIndex = i+1;
                outputIndexes.add(outIndex);
            }
        }
    }

    /**
     Finds and returns the location of the input silo as an ArrayList of integers.
     @return the location of the input silo
     */
    private ArrayList<Integer> findInputLocation() {
        String inputSilo = configs.get(inputValueStartingIndexPositions.get(0)-1);
        return convertStringsToInteger(inputSilo);
       // System.out.println("Input location as string: " + inputSilo);
        //TODO remove after testing
       // System.out.println("Input location as int list: " + convertStringsToInteger(inputSilo));
    }
    /**
     Returns the location of the input silo as an ArrayList of integers.
     @return the location of the input silo
     */
    public ArrayList<Integer> getInputLocation() {
        return findInputLocation();
    }
    /**
     Finds and sets the list of input values.
     Assumes that the input values are located between the input silo and the output silo in the configs list.
     */
    private void findInputValuesAndCoordinates() {
        String inputValuesString = "";
        for (int j = 0; j < inputValueStartingIndexPositions.size(); j++ ) {
            ArrayList<Integer> inputValues;
            ArrayList<Integer> inputCoords;
            String inputCoordsString = configs.get(inputValueStartingIndexPositions.get(j)-1);
            inputCoords = convertStringsToInteger(inputCoordsString);
            collectionOfInputCoordinates.add(inputCoords);
            for (int i = inputValueStartingIndexPositions.get(j); i < inputEndpoints.get(j); i++) {
                inputValuesString += configs.get(i) + " ";
            }
            inputValues = convertStringsToInteger(inputValuesString);
            collectionOfInputValues.add(inputValues);
            inputValuesString = "";
        }

        //TODO remove after testing
       // System.out.println("Input values as string: " + inputValuesString);
       // System.out.println("Input values that will be plugged in as a list: "+ inputValues);
    }

    /**
     Returns the list of all input values as an ArrayList of integers.
     @return the list of all input values
     */
    public ArrayList<ArrayList<Integer>> getCollectionOfInputValues() {
        return collectionOfInputValues;
    }

    /**
     Returns the list of all input values as an ArrayList of integers.
     @return the list of all Input Coordinates
     */
    public ArrayList<ArrayList<Integer>> getCollectionOfInputCoordinates() {
        return collectionOfInputCoordinates;
    }

    /**
     Returns the list of all output coordinates as an ArrayList of integers.
     @return the list of all output Coordinates
     */
    public ArrayList<ArrayList<Integer>> getOutputCoordinatesCollection() {
        return outputCoordinatesCollection;
    }

   // public ArrayList<Integer> getAllInputValues() {
     //   return inputValues;
   // }
    /**
     Finds and returns the location of the output silo as an ArrayList of integers.
     @return the location of the output silo
     */
  //  private ArrayList<Integer> findOutputLocation() {
  //      String outputSilo = configs.get(findOutputIndex());
   //     return convertStringsToInteger(outputSilo);
        //TODO remove after testing
    //    System.out.println("Output location as string: " + outputSilo);
     //   System.out.println("Output location as int list: " + convertStringsToInteger(outputSilo));
 //   }
    /**
     Returns the location of the output file.
     @return the location of the output file as an ArrayList of integers
     */
  //  public ArrayList<Integer> getOutputLocation() {
   //     return findOutputLocation();
  //  }
    /**
     * helper function for input and output silo sites, takes into account negative values
     * @param str
     * @return
     */
  // private String inputOutputFinder(String str) {
  //     if (str.charAt(0) == '-' && str.charAt(3) == '-') {
  //         str = str.substring(0,5);
  //         inputValueIndex = 7;
  //     } else if (str.charAt(0) == '-' && str.charAt(3) != '-'){
  //         str = str.substring(0,4);
  //         inputValueIndex = 6;
  //     } else if (str.charAt(0) != '-' && str.charAt(2) == '-') {
  //         str = str.substring(0,4);
  //         inputValueIndex = 6;
  //     } else {
  //         str = str.substring(0,3);
  //         inputValueIndex = 5;
  //     }
  //     return str;
  // }
    /**
     Converts a string of integers into an ArrayList of integers.
     @param input the string to be converted
     @return the ArrayList of integers
     */
    private ArrayList<Integer> convertStringsToInteger(String input) {
        Scanner scanner = new Scanner(input);
        ArrayList<Integer> list = new ArrayList<>();
        while (scanner.hasNextInt()) {
            list.add(scanner.nextInt());
        }
        return list;
    }
    /**
     Returns the list of instructions, where each instruction is a list of strings.
     @return the list of instructions
     */
    public ArrayList<ArrayList<String>> getInstructionList() {
        return instructionList;
    }
    /**
     Prints all configurations to the console.
     @param configs the list of configurations to be printed
     */
    private void printAllConfigs(ArrayList<String> configs) {
        for (String s : configs) {
            System.out.println(s);
        }
    }
    /**
     Returns the number of rows in the data structure.
     @return the number of rows
     */
    public int getNumRows() {
        return numRows;
    }
    /**
     Returns the number of columns in the data structure.
     @return the number of columns
     */
    public int getNumCols() {
        return numCols;
    }
    /**
     Returns the number of silos in the storage facility.
     @return the number of silos
     */
    public int getNumberOfSilos( ) {
        return numberOfSilos;
    }
    /**
     Returns the list of configurations for the system.
     @return the list of configurations
     */
    public ArrayList<String> getConfigs() {
        return configs;
    }
}