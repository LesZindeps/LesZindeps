import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.mvc.Http.Response;
import play.mvc.Scope;
import play.test.Fixtures;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.hamcrest.core.Is.is;

public class ApplicationTest extends ZindepFunctionalTest {

    public static final String ZINDEP_NOT_AVAILABLE_EMAIL = "jimi@hendrix.com";

    public static final String DATA_FOR_TESTS = "test-datas.yml";
    public static final String ATOM_CONTENT_TYPE = "application/atom+xml";
    public static final int OK = 200;

    @Before
    public void setUp() {
        Fixtures.deleteAllModels();
        Fixtures.loadModels(DATA_FOR_TESTS);

    }

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
        assertThat(response.getHeader("Location"), is("/admin/index"));
    }


    @Test
    public void testThatBackofficePageIsProtected() {
        Response response = GET("/backoffice/index");
        assertStatus(302, response);
    }

    @Test
    public void testDisponibilites_nominal_case() throws IOException, FeedException {
        //given
        Http.Request request = authenticateAndPopulateSession(ZINDEP_NOT_AVAILABLE_EMAIL);

        SyndFeed feed = getFeed(request);
        assertThat(feed.getEntries().size(), is(0));


        //when

        Scope.Session session = Scope.Session.current();
        Http.Request newRequest = setSessionWithNewRequest(session);
        Http.Response responseShowMyProfile = GET(newRequest, "/admin/showMyProfile");
        assertThat("response code for request /admin/showMyProfile " + responseShowMyProfile.status.toString() + " location=" + responseShowMyProfile.getHeader("Location"), responseShowMyProfile.status, is(200));

        request = setSessionWithNewRequest(session);
        //change availability and save

        request.params.put("zindep.currentAvailability", "FULL_TIME");
        request.params.put("zindep.id", session.get("zindepId"));
        Http.Response response = POST(request, "/admin/doUpdateMyProfile");
        session = Scope.Session.current();
        //then
        assertThat("response code for request /admin/doUpdateMyProfile " + response.status.toString() + " location=" + response.getHeader("Location"), response.status, is(302));
        assertThat(response.getHeader("Location"), is("/admin/showmyprofile"));
        SyndFeed feed2 = getFeed(setSessionWithNewRequest(session));
        assertThat(feed2.getEntries().size(), is(1));
        for (Object entry : feed2.getEntries()) {
            SyndEntry myEntry = (SyndEntry) entry;
            String link = myEntry.getLink();

            Response dispoResponse = GET(setSessionWithNewRequest(session), link);
            assertThat(dispoResponse.status, is(200));
            String dispoContent = dispoResponse.out.toString("UTF-8");

        }
    }

    private SyndFeed getFeed(Http.Request request) throws FeedException, IOException {
        Response disponibilitesResponse = GET(request, "/disponibilites");
        assertStatus(OK, disponibilitesResponse);
        assertContentType(ATOM_CONTENT_TYPE, disponibilitesResponse);
        SyndFeedInput input = new SyndFeedInput();
        return input.build(new XmlReader(new ByteArrayInputStream(disponibilitesResponse.out.toByteArray())));
    }

}

