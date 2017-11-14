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
import java.math.BigDecimal;
import java.util.List;

public class Controller {
    @FXML
    private TextField stem_field;
    @FXML
    private TextField query_field;
    @FXML
    private ScrollPane display_box;
    @FXML
    private Button status;
    @FXML
    private RadioButton boolean_retrieval;
    @FXML
    private RadioButton ranked_retrieval;
    @FXML
    private TextField index_location;

    Driver driver = new Driver();
    String path = "";
    String correction = "";

    public void handleIndexButtonAction(ActionEvent event) {
        status.setText("Indexing...");
        display_box.setContent(null);
        Node node = (Node) event.getSource();
        final DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select the directory to index");
        final File dir = directoryChooser.showDialog(node.getScene().getWindow());
        if (dir != null) {
            boolean indexed = driver.indexDirectory(dir.getAbsolutePath());
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
    private void handleIndexLocationAction(ActionEvent event) {
        Node node = (Node) event.getSource();
        final DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select the index to read");
        final File directory = directoryChooser.showDialog(node.getScene().getWindow());
        if (directory != null) {
            path = directory.getAbsolutePath();
            index_location.setText(path);
        }
        driver.readWildcardIndex(path);
    }

    @FXML
    private void handleSearchButtonAction(ActionEvent event) {
        path = index_location.getText();
        if (path == null || path.trim().isEmpty()) {
            status.setText("Please select a directory");
            return;
        }

        /*   boolean retrieval  */
        if (boolean_retrieval.isSelected()) {
            status.setText("Searching...");
            String term = query_field.getText();
            VBox content = new VBox();

            if (term != null && !term.trim().isEmpty()) {
                // perform search and generate button for each result
                List<Integer> booleanResults = driver.searchBoolean(path, term);
                for (Integer result : booleanResults) {
                    String fileName = "article" + result + ".json";
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

                    } catch (Exception e) {
                        System.err.println("File doesn't exist");
                    }
                }

                display_box.setContent(content);
                if (booleanResults.size() > 0) {
                    String msg = booleanResults.size() + " results - Click file to view";
                    status.setText(msg);
                } else {
                    // offer spelling correction if terms not found
                    correction = driver.getCorrection();
                    status.setText("Did you mean " + correction + "?");
                }

            } else {
                display_box.setContent(null);
                status.setText("Please input search query");
            }

            /*   ranked retrieval    */
        }
        else if (ranked_retrieval.isSelected()) {
            status.setText("Searching...");
            String query = query_field.getText().trim();
            VBox content = new VBox();
            DocWeight[] rankedResults = new DocWeight[10];

            if (query != null && !query.isEmpty()) {
                rankedResults = driver.searchRanked(path, query);
                // search query and generate button for each result
                for (DocWeight dw: rankedResults) {
                    if (dw == null) {
                        // offer spelling correction if null result
                        correction = driver.getCorrection();
                        status.setText("Did you mean " + correction + "?");
                        break;
                    }
                    String fileName = "article" + dw.getDocID()+".json";
                    String text = round(dw.getDocWeight()) + "  -  " + fileName;

                    try {
                        Button button = new Button(text);
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

                    } catch (Exception e) {
                        System.err.println("File doesn't exist");
                    }
                }
                display_box.setContent(content);
                correction = "";
            } else {
                display_box.setContent(null);
                status.setText("Please input search query");
            }

         // no search mode selected
        } else {
            status.setText("Please select a search mode");

        }
    }

    private static double round(double value) {
        BigDecimal decimal = new BigDecimal(value);
        decimal = decimal.setScale(6, BigDecimal.ROUND_HALF_UP);
        return decimal.doubleValue();
    }
}
