import org.junit.Test;
import static org.junit.Assert.*;

public class DocumentTest {
    private String bodyStr = "This is the body.";
    private String urlStr = "This is the url.";
    private String titleStr = "This is the title.";
    private Document doc = new Document(bodyStr, urlStr, titleStr);

    @Test
    public void getBody() throws Exception {
        assertEquals(bodyStr, doc.getBody());
    }

    @Test
    public void setBody() throws Exception {
        String newBodyStr = "This is the updated body.";
        doc.setBody(newBodyStr);
        assertEquals(newBodyStr, doc.getBody());
    }

    @Test
    public void getTitle() throws Exception {
        assertEquals(titleStr, doc.getTitle());
    }

    @Test
    public void setTitle() throws Exception {
        String newTitleStr = "This is the updated title.";
        doc.setTitle(newTitleStr);
        assertEquals(newTitleStr, doc.getTitle());
    }

    @Test
    public void getUrl() throws Exception {
        assertEquals(urlStr, doc.getUrl());
    }

    @Test
    public void setUrl() throws Exception {
        String newUrlStr = "This is the updated URL.";
        doc.setUrl(newUrlStr);
        assertEquals(newUrlStr, doc.getUrl());
    }
}