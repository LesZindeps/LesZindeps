import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import models.Zindep;
import org.junit.Test;
import play.mvc.Http;
import play.mvc.Http.Response;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.hamcrest.core.Is.is;

public class ApplicationTest extends ZindepFunctionalTest {

    public static final String ZINDEP_FULL_TIME_EMAIL = "damien.gouyette@xxxxx.com";

    @Test
    public void testThatIndexPageWorks() {
        Response response = GET("/");
        assertIsOk(response);
        assertContentType("text/html", response);
        assertCharset("utf-8", response);
    }

    @Test
    public void testThatAdminPageIsProtected() {
        Response response = GET("/admin/showmyprofile");
        assertStatus(302, response);
    }


    @Test
    public void testThatBackofficePageIsProtected() {
        Response response = GET("/backoffice/index");
        assertStatus(302, response);
    }

    @Test
    public void testDisponibilites() throws IOException, FeedException {
        //given
        SyndFeed feed = getFeed();
        assertThat(feed.getEntries().size(), is(0));


        //when
        Http.Request request = authenticateAndPopulateSessionWithoutPictureUrl(ZINDEP_FULL_TIME_EMAIL);
        //change availability and save
        request.params.put("currentAvailability", Zindep.Availability.NOT_AVAILABLE.toString());
        Http.Response response = POST(request, "/admin/doUpdateMyProfile");

        //then
        SyndFeed feed2 = getFeed();
        assertThat(feed.getEntries().size(), is(1));
    }

    private SyndFeed getFeed() throws FeedException, IOException {
        Response disponibilitesResponse = GET("/disponibilites");
        assertStatus(200, disponibilitesResponse);
        assertContentType("application/atom+xml", disponibilitesResponse);
        SyndFeedInput input = new SyndFeedInput();
        return input.build(new XmlReader(new ByteArrayInputStream(disponibilitesResponse.out.toByteArray())));
    }

}

