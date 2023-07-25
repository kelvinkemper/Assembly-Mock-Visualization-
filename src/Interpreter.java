import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.util.Duration;

import java.util.Arrays;
import java.util.HashMap;

/**
 * This class represents the silos and loads in instructions and executes them. Also updates its GUI represenation when
 * it does.
 */
public class Interpreter implements Runnable {
    
    //private BlockingQueue<Integer> outgoing;
    //private BlockingQueue<Integer> incoming;
    private Port Top, Right, Down, Left;
    private int ACC = 0,  BAK = 0;
    private SiloGUI siloGUI;
    private final int NIL = 0;
    private int currentLine = 0;
    private HashMap<String, Integer> labelAndLineLocations = new HashMap<>();
    private String[] siloInstructions;
    private boolean pause = true;
    private boolean fastForward = false;
    private boolean taskCompleted = false;
    private boolean reset = false;
    private Limiter mainLimiter;

    private int instructionSize;
    private void findInstructionSize() {
        int i = 0;
        while ( siloInstructions[i] != null ) {
            i++;
        }
    }
    public Interpreter( String[] instructions ) {
        siloInstructions = instructions;
        initialize();
    }

    public Interpreter() {
    }
    public void setSiloGUI( SiloGUI x ) {
        siloGUI = x;
    }
    /**
     * This method reads the whole string of a command and its parameters, then passes the needed parameters
     * from the string to the relevant method to carry out the command.
     * E.g ADD 5 breaks down into ADD, then its only parameter 5 is taken form the string and passed
     * to add() for parsing, etc.
     */

    public void run() {
        Thread.currentThread().setName(name);
        while ( !taskCompleted ) {
            try {
                Platform.runLater(() -> {
                    siloGUI.setCurrentLineHighlight(currentLine);
                    siloGUI.setAccValueLabel(ACC);
                    siloGUI.setBakValueLabel(BAK);
                });
                if (pause) {
                    mainLimiter.pause();
                }
                String fullInstruction;
                try {
                    fullInstruction = siloInstructions[currentLine];
                } catch (ArrayIndexOutOfBoundsException e)
                {
                    break;
                }
                String shortenedInstruction;
                String s = getInstructionType();
                int endOfFirstWord = checkForNextWordIndex(fullInstruction);
                if (endOfFirstWord > 0) {
                    shortenedInstruction = fullInstruction.substring(endOfFirstWord).trim();
                } else {
                    shortenedInstruction = fullInstruction;
                }
                switch (s) {
                    case "NOOP":
                        break;
                    case "MOVE":
                        move(shortenedInstruction);
                        break;
                    case "SWAP":
                        swap();
                        break;
                    case "SAVE":
                        save();
                        break;
                    case "ADD":
                        add(shortenedInstruction);
                        break;
                    case "SUB":
                        sub(shortenedInstruction);
                        break;
                    case "NEGATE":
                        negate();
                        break;
                    case "JUMP":
                        jump(shortenedInstruction.trim());
                        break;
                    case "JEZ":
                        jez(shortenedInstruction.trim());
                        break;
                    case "JNZ":
                        jnz(shortenedInstruction.trim());
                        break;
                    case "JGZ":
                        jgz(shortenedInstruction.trim());
                        break;
                    case "JLZ":
                        jlz(shortenedInstruction.trim());
                        break;
                    case "JRO":
                        jro(shortenedInstruction.trim());
                        break;
                }
                updateGUIAfterCommandRun();
                currentLine++;

                if (currentLine > siloInstructions.length - 1) {
                    currentLine = 0;
                }
                if (!pause && fastForward) {
                    Thread.sleep(12);
                }
                if (!pause && !fastForward) {
                        Thread.sleep(1000);
                    }
            } catch (InterruptedException e) {
                resetSilo();
            }
        }

    }
    public void pauseSilo() {
        fastForward = false;
        pause = true;
    }
    public void unpauseSilo() {
        pause = false;
    }
    public void resetSilo(){
        pause = true;
        ACC = 0;
        BAK = 0;
        currentLine = 0;
        fastForward = false;
    }

    public void fastForwardSilo() {
        fastForward = true;
    }

    public String getName() {
        return name;
    }

    public int getCurrentLine( ) {
        return currentLine;
    }

    public int getACC( ) {
        return ACC;
    }

    public int getBAK( ) {
        return BAK;
    }

    public void setLimiter( Limiter x ) {
        mainLimiter = x;
    }

    /**
     * This method checks if a String has more than just one word in it.
     * E.g. MOVE ADD RIGHT returns 4 and therefore there is not just one word. Returns
     * -1 if there's only 1 word.
     */
    private int checkForNextWordIndex( String instruction ) {
        int index = instruction.indexOf(' ');
        return index;
    }

    /**
     * This function parses the specified source of data and then writes it to the specified destination cited in the
     * string.
     */
    private boolean move( String shortenedInstruct ) throws InterruptedException {
        boolean parseSuccess = true;
        int data = 0;
        try {
            data = parseSourceAndGetValue(shortenedInstruct);
        } catch ( NullPointerException e ) {
            parseSuccess = false;
        }
        String dest = shortenedInstruct.substring(shortenedInstruct.indexOf(' ')).trim();
        if ( parseSuccess ) {
            switch (dest) {
                case "UP":
                    try {
                        Top.getPortIncoming().put(data);
                        int finalData = data;
                        Platform.runLater(() -> {
                            siloGUI.updateTransferValue(siloGUI,SiloGUI.PortPosition.TOP, finalData,true);
                        });
                        Top.waitForSignal();
                       // PauseTransition pauseTransition = new PauseTransition(Duration.seconds(0.5));
                       // pauseTransition.setOnFinished(event -> {
                            Platform.runLater(() -> {
                                System.out.println("SET ISVISIBLE FALSE");
                                siloGUI.updateTransferValue(siloGUI,SiloGUI.PortPosition.TOP, finalData,false);
                            });
                       // });
                      //  pauseTransition.play();
                    } catch (InterruptedException e) {
                        throw new InterruptedException();
                    }
                    break;
                case "RIGHT":
                    try {
                        Right.getPortIncoming().put(data);
                        int temp = data;
                        Platform.runLater(() -> {
                            siloGUI.updateTransferValue(siloGUI,SiloGUI.PortPosition.RIGHT, temp,true);
                        });
                        Right.waitForSignal();
                      //  PauseTransition pauseTransition = new PauseTransition(Duration.seconds(0.5));
                      //  pauseTransition.setOnFinished(event -> {
                            Platform.runLater(() -> {
                                siloGUI.updateTransferValue(siloGUI,SiloGUI.PortPosition.RIGHT, temp,false);
                            });
                      //  });
                       // pauseTransition.play();
                    } catch (InterruptedException e) {
                        throw new InterruptedException();
                    }
                    break;
                case "DOWN":
                    try {
                        Down.getPortIncoming().put(data);
                        int temp = data;
                        Platform.runLater(() -> {
                            siloGUI.updateTransferValue(siloGUI,SiloGUI.PortPosition.BOTTOM, temp,true);
                        });
                        Down.waitForSignal();
                       // PauseTransition pauseTransition = new PauseTransition(Duration.seconds(0.5));
                       // pauseTransition.setOnFinished(event -> {
                            Platform.runLater(() -> {
                                siloGUI.updateTransferValue(siloGUI,SiloGUI.PortPosition.BOTTOM, temp,false);
                            });
                      //  });
                      //  pauseTransition.play();
                    } catch (InterruptedException e) {
                        throw new InterruptedException();
                    }
                    break;
                case "LEFT":
                    try {
                        Left.getPortIncoming().put(data);
                        int temp = data;
                        Platform.runLater(() -> {
                            siloGUI.updateTransferValue(siloGUI,SiloGUI.PortPosition.LEFT, temp,true);
                        });
                        Left.waitForSignal();
                      //  PauseTransition pauseTransition = new PauseTransition(Duration.seconds(0.5));
                       // pauseTransition.setOnFinished(event -> {
                            Platform.runLater(() -> {
                                siloGUI.updateTransferValue(siloGUI,SiloGUI.PortPosition.LEFT, temp,false);
                            });
                      //  });
                      //  pauseTransition.play();
                    } catch (InterruptedException e) {
                        throw new InterruptedException();
                    }
                    break;
                case "ACC":
                    ACC = data;
                    break;
                case "BAK":
                    BAK = data;
                    break;
            }
        }
        return parseSuccess;
    }

    /**
     * This method is meant to get the relevant int value from the specified [SRC].
     * @param shortenedInstruct is the instruction passed without the actual command like ADD or SUB or SAVE, etc.
     * because these commands then specify [SRC] next, that word is obtained and the relevant integer is saved
     * and returned.
     * @return int data, the value that was retrieved from the desired [SRC].
     */
    private int parseSourceAndGetValue( String shortenedInstruct ) throws InterruptedException {
        int data = 0;
        int possibleEndOfAnotherWord = checkForNextWordIndex( shortenedInstruct );
        String source;
        if ( possibleEndOfAnotherWord > 0 ) {
            source = shortenedInstruct.substring(0, possibleEndOfAnotherWord).trim();
        } else {
            source = shortenedInstruct;
        }
        switch ( source ) {
            case "UP":
                try {
                    data =  Top.getPortOutgoing().take();
                    Top.signal();
                } catch (InterruptedException e) {
                    throw new InterruptedException();
                }
                break;
            case "RIGHT":
                try {
                    data = Right.getPortOutgoing().take();
                    Right.signal();
                } catch (InterruptedException e) {
                    throw new InterruptedException();
                }
                break;
            case "DOWN":
                try {
                    data = Down.getPortOutgoing().take();
                    Down.signal();
                } catch (InterruptedException e) {
                    throw new InterruptedException();
                }
                break;
            case "LEFT":
                try {
                    data = Left.getPortOutgoing().take();
                    Left.signal();
                } catch (InterruptedException e) {
                    throw new InterruptedException();
                }
                break;
            case "ACC":
                data = ACC;
                break;
            case "BAK":
                data =  BAK;
                break;
            default:
                data = Integer.parseInt( source );
                break;
        }
        return data;
    }

    /**
     * Swaps the values in the ACC and BAK.
     */
    private void swap( ) {
        int temp = ACC;
        ACC = BAK;
        BAK = temp;
    }

    /**
     * Saves the value in the ACC to the BAK.
     */
    private void save() {
        BAK = ACC;
    }

    /**
     * Adds the value specified in the command to the ACC.
     */
    private void add( String shortenedInstruct ) throws InterruptedException {
        int data = parseSourceAndGetValue( shortenedInstruct );
        ACC += data;
    }

    /**
     * Subtracts the value specified in the command from the ACC.
     */
    private void sub( String shortenedInstruct ) throws InterruptedException {
        int data = parseSourceAndGetValue( shortenedInstruct );
        ACC -= data;
    }

    /**
     * Negates the ACC value by multiplying by negative one.
     */
    private void negate() {
        ACC *= -1;
    }

    /**
     * Takes the specified label and gets the line number from the HashMap and sets the current line equal to it.
     */
    private void jump( String label ) {
        currentLine = labelAndLineLocations.get(label);
    }

    /**
     * Takes the specified label and gets the line number from the HashMap and sets the current line equal to it.
     * if the ACC = 0.
     */
    private void jez( String label) {
        if ( ACC == 0 )
            currentLine = labelAndLineLocations.get(label);
    }

    /**
     * Takes the specified label and gets the line number from the HashMap and sets the current line equal to it.
     * if the ACC != 0.
     */
    private void jnz( String label ) {
        if ( ACC != 0 )
            currentLine = labelAndLineLocations.get(label);
    }

    /**
     * Takes the specified label and gets the line number from the HashMap and sets the current line equal to it.
     * if the ACC > 0.
     */
    private void jgz( String label ) {
        if ( ACC > 0 )
            currentLine = labelAndLineLocations.get(label);
    }

    /**
     * Takes the specified label and gets the line number from the HashMap and sets the current line equal to it.
     * if the ACC < 0.
     */
    private void jlz( String label ) {
        if ( ACC < 0 )
            currentLine = labelAndLineLocations.get(label);
    }

    /**
     * moves the currentline ahead or back by the amount specified in the
     * @param offsetString String
     */
    private void jro( String offsetString ) throws InterruptedException {
        int offset = parseSourceAndGetValue(offsetString);
        int size = siloInstructions.length;
        if ( offset >= 0 ) {
            currentLine += offset % size;

            if ( currentLine >= size ) {
                currentLine = ( currentLine ) - size;
            }
        } else {
            currentLine -= offset % size;
            if ( currentLine < 0 ) {
                currentLine = (size + currentLine);
            }
        }
        if ( currentLine == 0 ) {
            currentLine = size - 1;
        } else {
            currentLine -= 1;
        }
    }

    /**
     * Reads the first word in a string and passes it back to determine what function the silo should perform.
     * @return String command
     */
    private String getInstructionType() {
        String s = siloInstructions[currentLine];
        String command;
        int firstSpace = s.indexOf(' ');
        if ( firstSpace > -1 ) {
            command = s.substring(0, firstSpace).trim();
        } else {
            command = s;
        }
        return command;
    }

    /**
     * looks for labels in the set of instructions and marks the spot where the label is in the list
     */
    private void initialize() {
        for ( int i = 0; i < siloInstructions.length; i++ ) {
            String s = siloInstructions[i];
            if ( s != null && s.charAt(0) == ':' && s.charAt(s.length()-1) == ':') {
                String labelName = s.substring(1, s.length() - 1 );
                labelAndLineLocations.put(labelName, i);
            }
        }
    }

    /**
     *  getter and setter methods just in case
     * @return siloInstructions string array
     */
    public String[] getSiloInstructions() {
        return siloInstructions;
    }

    /**
     *  sets internal siloInstructions to point at
     * @param userGivenInstructions
     */
    public void setSiloInstructions( String[] userGivenInstructions ) {
        siloInstructions = userGivenInstructions;
    }

    /**
     *  updates the GUI represenation of the silo
     */
    public void updateGUIAfterCommandRun() {
        Platform.runLater(() -> {
                    siloGUI.setCurrentLineHighlight(currentLine);
                    siloGUI.setAccValueLabel(ACC);
                    siloGUI.setBakValueLabel(BAK);
                });
    }


    /**
     *  sets the top port pointer to point at
     * @param topPort
     */
    public void setTop( Port topPort ) {
        Top = topPort;
    }

    /**
     *  sets the right port pointer to point at
     * @param rightPort
     */
    public void setRight( Port rightPort ) {
        Right = rightPort;
    }

    /**
     *  sets the bottom port pointer to point at
     * @param bottomPort
     */
    public void setDown( Port bottomPort ) {
        Down = bottomPort;
    }

    /**
     *  sets the left port pointer to point at
     * @param leftPort
     */
    public void setLeft( Port leftPort ) {
        Left = leftPort;
    }

    /**
     * @return Top
     * returns the Port Top
     */
    public Port getTop() {
        return Top;
    }

    /**
     * @return Right
     * returns the Port Right
     */
    public Port getRight() {
        return Right;
    }

    /**
     * @return Down
     * returns the Port Down
     */
    public Port getDown() {
        return Down;
    }

    /**
     * @return Left
     * returns the Port Left
     */
    public Port getLeft() {
        return Left;
    }

    /**
     * sets the name of the silo to
     * @param s
     */
    public void setName( String s ) {
        name = s;
    }
    private String name;

}
