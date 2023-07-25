import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
public class Port implements Runnable {
    private Integer data;
    private ArrayBlockingQueue<Integer> incoming = new ArrayBlockingQueue<>(1);
    private ArrayBlockingQueue<Integer> outgoing = new ArrayBlockingQueue<>(1);
    private boolean waitingOnSignal = false;
    private boolean isAnOutput = false;
    private boolean isAnInput = false;
    private ArrayList<VBox> outputValues;
    private VBox inputValues;
    private boolean taskEnded = false;
    private ArrayList<Integer> inputData;
    private ArrayList<Integer> outputDataStorage;
    private int index = 0;
    private Font font;
    private AssemblySilosGUI asg;
    private Queue<Integer> outputQ = new LinkedList<>();
    private volatile boolean running = true;
    private Limiter limiter;
    /**
     This method is used to continuously run the thread for input or output data. The method loops while the "running"
     boolean is true, and executes code depending on the boolean values "isAnInput" and "isAnOutput". If neither is true,
     the thread listens to incoming data on the port and sends the same data to the outgoing port, until a "taskEnded"
     flag is set. If "isAnInput" is true, the method listens to incoming data on the port, and sends the same data to the
     outgoing port, while updating the UI and waiting for a signal to continue for each incoming data value, until a
     "taskEnded" flag is set. If "isAnOutput" is true, the method listens to incoming data on the port, saves the data to
     the "outputDataStorage" ArrayList, updates the UI with the latest data, and continues the loop until a "taskEnded"
     flag is set. If an exception occurs during the execution, a RuntimeException is thrown.
     */
    @Override
    public void run() {
        while(running) {
            if (!isAnOutput && !isAnInput) {
                try {
                while (!taskEnded) {
                        int x = incoming.take();
                        data = x;
                        outgoing.put(x);
                    }
                } catch (InterruptedException e) {
                    resetPort();
                }
            }
            if (isAnInput) {
                //System.out.println("entering.");
                Thread.currentThread().setName("Port Thread");
                try {
                while (!taskEnded) {
                        data = inputData.get(index);
                        outgoing.put(data);
                        Platform.runLater(() -> {
                            asg.setInputArrowValue(data);
                            asg.highlightCurrentInput();
                            //for (int i = 0; i < inputValues.getChildren().size(); i++) {
                            //    BorderPane bp = (BorderPane) inputValues.getChildren().get(i);
                            //    Label label = (Label) bp.getCenter();
                            //    System.out.println(label.getText() +" vs " + inputData.get(index).toString() );
                            //    if (label.getText().equals(inputData.get(index).toString())) {
                            //        label.setStyle("-fx-background-color: white; -fx-text-fill: black;");
                            //    }
                            //}
                        });
                        waitForSignal();
                        index++;
                        Platform.runLater(() -> {
                        asg.setInputCounter(index+1); });

                        if (index >= inputData.size()) {
                            waitForSignal();
                            // taskEnded = true;
                            //Platform.runLater(()->{asg.setInputArrowValue(null);});

                        }
                    }
                } catch (InterruptedException e) {
                    resetPort();
                }
            }
            if (isAnOutput) {
                try {
                while (!taskEnded) {
                        int x = incoming.take();
                        data = x;
                        outputQ.add(data);
                        outputDataStorage.add(x);
                        Platform.runLater(() -> {
                            asg.setOutputArrowValue();
                            asg.showOutStorage();
                        });
                    }
                } catch (InterruptedException e) {
                    resetPort();
                }
            }
        }

    }

    /**
     Returns the list of all output coordinates as an ArrayList of integers.
     @param l, the main limiter for the silos
     */
    public void setLimiter( Limiter l ) {
        limiter = l;
    }
    /**
     Sets the flag isAnOutput to true and initializes the outputDataStorage ArrayList.
     This function is called to enable the output port of a basic block.
     */
    public void enableIsAnOutput( ) {
        isAnOutput = true;
        outputDataStorage = new ArrayList<>();
    }
    /**
     Enables the current port as an input port with the given data.
     @param theData an ArrayList of Integers representing the input data to be used by this port
     */
    public void enableIsAnInput(ArrayList<Integer> theData ) {
        isAnInput = true;
        inputData = theData;

    }
    /**
 This method returns the output data storage which is an ArrayList of integers.
 This ArrayList contains all the data sent out by the current port.
 @return ArrayList<Integer> An ArrayList containing all the data sent out by the current port.
  */
    public synchronized ArrayList<Integer> getOutputStorage() {
        return outputDataStorage;
    }

    /**
     This resets the ports as well as their attached output values if the port is an output.
     */
    public synchronized void resetPort() {
        notify();
        if (isAnOutput) {
            outputDataStorage.clear();
//            outputValues.getChildren().clear();
          //  outputValues.getChildren().removeIf(node -> !outputValues.getChildren().get(0).equals(node));
            for (int i = 0; i < outputValues.size(); i++) {
                int finalI = i;
                outputValues.get(i).getChildren().removeIf(node -> !outputValues.get(finalI).getChildren().get(0).equals(node));
            }
        }
        if (isAnInput) {
            index = 0;
        }
        incoming.clear();
        outgoing.clear();
    }

    /**
     This returns
     @return outputQ
     */
    public synchronized Queue<Integer> getOutputQ() {
        return outputQ;
    }

    /**
     This returns the int DATA currently in the port
     */
    public synchronized int getData() {
        return data;
    }

    /**
     This method returns the BlockingQueue object used as the incoming port for the basic node.
     The incoming port is where the basic node will receive data from other nodes.
     @return the BlockingQueue<Integer> object representing the incoming port of the basic node.
     @throws InterruptedException if the current thread is interrupted while waiting for the incoming port to be available.
     */
    public synchronized BlockingQueue<Integer> getPortIncoming( ) throws InterruptedException {
        return incoming;
    }
    /**
     Returns the outgoing blocking queue for the current port.
     @return the outgoing blocking queue for the current port.
     @throws InterruptedException if any thread has interrupted the current thread while waiting for the blocking queue.
     */
    public synchronized BlockingQueue<Integer> getPortOutgoing () throws InterruptedException {
        return outgoing;
    }
    /**
     This method signals the waiting thread to resume its execution after being paused by the {@link #waitForSignal()} method.
     If no thread is waiting on a signal, the current thread waits for the signal.
     Once the waiting thread is notified, it resumes its execution from where it was paused.
     This method is synchronized to ensure only one thread can signal at a time.
     @throws InterruptedException if any thread interrupts the current thread while waiting for the signal.
     */
    public synchronized void signal() throws InterruptedException {
        if (!waitingOnSignal) {
            wait();
        }
        waitingOnSignal = false;
        notify();
    }
    /**
     This method is used to wait for the signal from the input thread to continue processing data.
     It checks if the port is not an output port, sets the waitingOnSignal flag to true and notifies any waiting threads.
     Then it waits for the signal and prints the name of the thread that is waiting.
     After the signal is received, it prints the name of the thread that is no longer waiting.
     @throws InterruptedException if the thread is interrupted while waiting
     */
    public synchronized void waitForSignal() throws InterruptedException {
        try {
            waitingOnSignal = true;
            if (!isAnOutput) {
                notify();
                wait();
            }
        } catch ( InterruptedException e ) {
        }
    }
    /**
     Links the given interpreters, where the given top interpreter will send its output to the given bottom interpreter.
     @param Top The top Interpreter to be linked.
     @param Bottom The bottom Interpreter to be linked.
     */
    public void linkTopAndBottom( Interpreter Top, Interpreter Bottom ) {
        Top.setDown(this);
        Bottom.setTop(this);
    }
    /**
     Links two Interpreter objects together horizontally.
     The left Interpreter's right output port is connected to this interpreter's left input port,
     and this interpreter's right output port is connected to the right Interpreter's left input port.
     @param Left the Interpreter object to be linked on the left side of this interpreter
     @param Right the Interpreter object to be linked on the right side of this interpreter
     */
    public void linkLeftAndRight( Interpreter Left, Interpreter Right ) {
        Left.setRight( this );
        Right.setLeft( this );
    }

    /**
     Sets the VBox where the output values are displayed.
     @param x the VBox where the output values are displayed.
     */
    public void setVboxList( ArrayList<VBox> x ) {
        outputValues = x;
    }
    /**
     Sets the VBox for the input values of the port.
     @param x the VBox to be set as input values.
     */
    public void setInputVBox (VBox x){
        inputValues = x;
    }
    /**
     Sets the AssemblySilosGUI object used to display the status of the assembly silos.
     @param assemblySilosGUI the AssemblySilosGUI object used to display the status of the assembly silos.
     */
    public void setAssemblySiloGUI(AssemblySilosGUI assemblySilosGUI) {
        this.asg = assemblySilosGUI;
    }
    /**
     Sets the running flag to false, indicating that the thread should stop running.
     */
    public void stop(){
        running = false;
    }
}