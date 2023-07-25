import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.io.InputStream;
/**
 The SiloGUI class extends the BorderPane class and provides a graphical user interface for the Silo programming language.
 The class includes a TextArea component for entering and displaying text, Label components for displaying the current ACC and BAK values,
 and methods for setting the highlight style for the current line of text, updating the transfer value displayed on a port, and setting the text of
 the TextArea and Label components. The class also includes an enumeration of the possible positions of a port on a visual element.
 */
public class SiloGUI extends BorderPane {
    private final TextArea textArea;
    private final Label accValueLabel;
    private final Label bakValueLabel;
    private Interpreter siloInterpreter;
    public SiloGUI() {
        //Square container representing the Silo
        BorderPane siloSquare = new BorderPane();
        siloSquare.setStyle("-fx-control-inner-background: black; -fx-text-fill: white; -fx-border-color: white; -fx-border-width: 5px;");
        siloSquare.setPrefSize(250, 250);
        Font font = Font.loadFont(getClass().getClassLoader().getResource("font/font.ttf").toString(), 16);
        textArea = new TextArea();
        textArea.setFont(font);
        siloSquare.setLeft(textArea);
        textArea.setPrefSize(166.67, 250);

        VBox informationBox = new VBox(10);
        informationBox.setPrefSize(83.33, 250);
        informationBox.setStyle("-fx-control-inner-background: black; -fx-text-fill: white; -fx-border-color: white; -fx-border-width: 2px;");
        Label accLabel = new Label("ACC");
        accLabel.setFont(font);
        accLabel.setTextFill(Color.GRAY);
        accValueLabel = new Label("0");
        accValueLabel.setTextFill(Color.WHITE);
        accValueLabel.setFont(font);
        Label bakLabel = new Label("BAK");
        bakLabel.setTextFill(Color.GRAY);
        bakLabel.setFont(font);
        bakValueLabel = new Label("0");
        bakValueLabel.setTextFill(Color.WHITE);
        bakValueLabel.setFont(font);
        informationBox.getChildren().addAll(accLabel, accValueLabel, bakLabel, bakValueLabel);
        informationBox.setAlignment(Pos.CENTER);
        setAccValueLabel(0);
        setBakValueLabel(0);
        siloSquare.setRight(informationBox);
        setCenter(siloSquare);
    }
    /**
     Sets the highlight style for the current line in a JavaFX TextArea component and selects the text of that line.
     @param currentLine The line number to highlight and select.
     @return void
     */
    public void setCurrentLineHighlight(int currentLine){
        textArea.setStyle("-fx-highlight-fill: white; -fx-highlight-text-fill: black; -fx-font-weight: bold;");
        int start = 0;
        int end = 0;
        for (int i = 0; i <= currentLine; i++) {
            if (i == currentLine) {
                start = end;
            }
            end = textArea.getText().indexOf("\n", end) + 1;
        }
        textArea.selectRange(start,end);
    }
    /**
     Returns the JavaFX TextArea component associated with this object.
     @return The JavaFX TextArea component associated with this object.
     */
    public TextArea getTextArea() {
        return textArea;
    }
    /**
     Sets the text of the JavaFX TextArea component associated with this object.
     @param text The text to set in the TextArea component.
     @return void
     */
    public void setTextArea(String text) {
        textArea.setText(text);
    }
    /**
     Sets the text of the JavaFX Label component associated with this object to display the ACC value.
     @param accValue The ACC value to display in the Label component.
     @return void
     */
    public void setAccValueLabel(int accValue){
        accValueLabel.setText(String.valueOf(accValue));
    }
    /**
     Sets the text of the JavaFX Label component associated with this object to display the BAK value.
     @param bakValue The ACC value to display in the Label component.
     @return void
     */
    public void setBakValueLabel(int bakValue){
        bakValueLabel.setText(String.valueOf(bakValue));
    }
    /**
     An enumeration of the possible positions of a port on a visual element.
     The PortPosition enumeration provides a set of values representing the possible positions of a port on a visual element. The values are TOP, LEFT, RIGHT, and BOTTOM.
     */
    public enum PortPosition {
        TOP,
        LEFT,
        RIGHT,
        BOTTOM
    }
    /**
     Updates the transfer value displayed on a port.
     @param position The position of the port to update.
     @param value The new value to display on the port.
     @return void
     */
    public void updateTransferValue(SiloGUI silo,PortPosition position, int value, boolean isVisible) {
        Node port = null;
        if (position == PortPosition.TOP) {
            port = silo.getTop();
        } else if (position == PortPosition.LEFT) {
            port = silo.getLeft();
        } else if (position == PortPosition.RIGHT) {
            port = silo.getRight();
        } else if (position == PortPosition.BOTTOM) {
            port = silo.getBottom();
        }
        if (port != null) {
            Label transferValue = null;
            if (position == PortPosition.LEFT) {
                transferValue = (Label) ((VBox) port).getChildren().get(1);
            } else if (position == PortPosition.RIGHT) {
                transferValue = (Label) ((VBox) port).getChildren().get(0);
            } else if (position == PortPosition.TOP) {
                transferValue = (Label) ((HBox) port).getChildren().get(0);
            } else if (position == PortPosition.BOTTOM) {
                transferValue = (Label) ((HBox) port).getChildren().get(1);
            }

            if (isVisible) {
                PauseTransition pauseTransition = new PauseTransition(Duration.seconds(0.1));
                Label finalTransferValue = transferValue;
                pauseTransition.setOnFinished(event -> {
                    finalTransferValue.setVisible(true);
                });
                pauseTransition.play();
            }
            if (isVisible == false) {
                transferValue.setVisible(false);
            }
            transferValue.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-font-weight: bold;");
            transferValue.setText(Integer.toString(value));
        }
    }
}