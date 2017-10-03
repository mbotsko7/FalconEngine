import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import javax.xml.ws.Response;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Created by bardsko on 9/2/17.
 */
public class Parser {

    /*
       Accepts individual JSON files, returns the Title, Body as a String array
     */
    public String[] parseJSON(String path) {
        Gson gson = new Gson();
        try {
            String fileData = new String(Files.readAllBytes(Paths.get(path)));
            JsonParser parser = new JsonParser();
            JsonElement json = parser.parse(fileData);
            //if(json.isJsonObject()){
            JsonObject obj = json.getAsJsonObject();
            String body = obj.get("body").getAsString();
            String title = obj.get("title").getAsString();
            return new String[]{title, body};
            //}
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    /*
    Breaks down one JSON document into several
     */
    public void parseDocumentJSON(String path) {
        Gson googleJSON = new Gson();
        try {
            String fileData = new String(Files.readAllBytes(Paths.get(path))); //all-nps-sites.json
            JsonParser parser = new JsonParser();
            JsonElement jsonTree = parser.parse(fileData);
            if (jsonTree.isJsonObject()) {
                JsonObject obj = jsonTree.getAsJsonObject();
                JsonElement docs = obj.get("documents");
                if (docs.isJsonArray()) {
                    JsonArray docList = docs.getAsJsonArray();
                    for (int i = 0; i < docList.size(); i++) {
                        JsonElement doc1 = docList.get(i);
                        if (doc1.isJsonObject()) {
                            JsonObject obj2 = doc1.getAsJsonObject();
                            String body = obj2.get("body").getAsJsonPrimitive().getAsString();
                            String url = obj2.get("url").getAsJsonPrimitive().getAsString();
                            String title = obj2.get("title").getAsJsonPrimitive().getAsString();
                            Document d = new Document(body, url, title);
                            String str = googleJSON.toJson(d);
                            String docname = "doc" + (i + 1) + ".json";
                            FileWriter fw = new FileWriter(new File(docname));
                            fw.write(str);
                            fw.close();
                        }
                    }
                }
            }


        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


}
