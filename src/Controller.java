import com.google.gson.Gson;
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

import java.io.*;
import java.util.List;

import java.util.Set;

public class Controller {
    @FXML
    private TextField stem_field;
    @FXML
    private TextField query_field;
    @FXML
    private ScrollPane display_box;
    @FXML
    private Label status;

    Driver driver = new Driver();
    String path = "";

    public void handleIndexButtonAction(ActionEvent event) {
        status.setText("Indexing...");
        display_box.setContent(null);
        Node node = (Node) event.getSource();
        final DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select the directory to index");
        final File dir = directoryChooser.showDialog(node.getScene().getWindow());
        if (dir != null) {
            path = dir.getAbsolutePath();
            File f = new File(path);
            boolean indexed = driver.indexDirectory(f);
            if (indexed)
                status.setText("Indexing complete");
            else
                status.setText("Directory invalid");
        }
    }

    public void handleVocabButtonAction() {
        status.setText("Getting vocab...");
        VBox box = new VBox();
        display_box.setContent(box);
        String[] vocab = driver.getVocabList();
        for (String term: vocab) {
            box.getChildren().add(new Label(term));
        }
        status.setText(vocab.length + " terms fetched");
    }

    public void handleStemButtonAction() {
        String term = stem_field.getText();
        if (term != null && !term.trim().isEmpty()) {
            String stemmedToken = driver.stemToken(term);
            display_box.setContent(new Label(stemmedToken));  // test
            status.setText("");
        }
        else {
            display_box.setContent(null);
            status.setText("Please enter a token to stem");
        }
    }

    @FXML
    private void handleSearchButtonAction(ActionEvent event) {
        status.setText("Searching...");
        String term = query_field.getText();
        VBox content = new VBox();

        if (term != null && !term.trim().isEmpty()) {
            List<Integer> results = driver.search(term);
            for (Integer result : results) {
                Gson gson = new Gson();
                String fileName = "article" + result + ".json";
                String title = "";

                try {
                    Button button = new Button(fileName);
                    button.getStyleClass().add("result-button");
                    content.getChildren().add(button);
                    // open file in a new window
                    button.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent e) {
                            Stage stage = new Stage();
                            stage.setTitle(fileName);
                            File fileToView = new File(path + "/" + fileName);
                            Label doc = new Label(Driver.readDocument(fileToView));
                            doc.setWrapText(true);

                            ScrollPane pane = new ScrollPane(doc);
                            pane.setPadding(new Insets(45, 10, 20, 10));
                            pane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
                            pane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

                            Scene scene = new Scene(pane, 500, 600);
                            pane.setMinWidth(scene.getWidth());
                            doc.setMaxWidth(pane.getWidth() - 60);

                            stage.setScene(scene);
                            stage.sizeToScene();
                            stage.show();
                        }
                    });

                }
                catch (Exception e) {
                    System.err.println("File doesn't exist");
                }
            }

            display_box.setContent(content);
            if (results.size() > 0) {
                String msg = results.size() + " results - Click file to view";
                status.setText(msg);
            }
            else {
                status.setText("No results found");
            }

        }
        else {
            display_box.setContent(null);
            status.setText("Please input search query");
        }
    }
}
