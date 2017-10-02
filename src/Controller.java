import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.geometry.Insets;

import java.io.File;
import java.util.Set;

public class Controller {
    @FXML
    private TextArea textbox;
    @FXML
    private ScrollPane display_box;

    Driver driver = new Driver();
    String path = "";

//    public void handleExitButtonAction() {
//        Platform.exit();
//    }

    public void handleIndexButtonAction(ActionEvent event) {
        Node node = (Node) event.getSource();
        final DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select the directory to index");
        final File selectedDirectory = directoryChooser.showDialog(node.getScene().getWindow());
        if (selectedDirectory != null) {
            path = selectedDirectory.getAbsolutePath();
            File f = new File(path);
            driver.indexDirectory(f, display_box);
        }
    }

    public void handleVocabButtonAction() {
        Label label = new Label();
        driver.printVocab(display_box);
    }

    public void handleStemButtonAction() {
        String term = textbox.getText();
        if (term != null && !term.trim().isEmpty()) {
            driver.stemToken(term, display_box);
        } else {
            display_box.setContent(new Label("Please enter a token to stem"));
        }
    }

    @FXML
    private void handleSearchButtonAction(ActionEvent event) {
        String term = textbox.getText();
        VBox content = new VBox();

        if (term != null && !term.trim().isEmpty()) {
            Set<Integer> results = driver.search(term, display_box);
            for (Integer result: results) {
                String title = "article" + result+ ".json";
                Button button = new Button(title);
                button.getStyleClass().add("result-button");
                content.getChildren().add(button);

                button.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent e) {
                        Stage stage = new Stage();
                        stage.setTitle(title);
                        File fileToView = new File(path + "/" + title);
                        Label doc = new Label(Driver.readDocument(fileToView));
                        doc.setWrapText(true);

                        ScrollPane pane = new ScrollPane(doc);
                        pane.setPadding(new Insets(45,10,20,10));
                        pane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
                        pane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

                        Scene scene = new Scene(pane, 500,600);
                        pane.setMinWidth(scene.getWidth());
                        doc.setMaxWidth(pane.getWidth() - 60);

                        stage.setScene(scene);
                        stage.sizeToScene();
                        stage.show();
                    }
                });
            }
            display_box.setContent(content);

        } else {
            display_box.setContent(new Label("Please input search query"));
        }
    }
}
