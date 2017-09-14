import javax.print.Doc;
import java.util.List;

/**
 * Created by bardsko on 9/9/17.
 */


public class Document {
    private String body, url, title;
    public Document(String b, String u, String t){
        body = b;
        url = u;
        title = t;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
