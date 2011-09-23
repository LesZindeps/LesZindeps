import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.test.Fixtures;

public class AdminTest extends ZindepFunctionalTest {

    public static final String ADMIN_SHOW_MY_PROFILE = "/admin/showmyprofile";
    public static final String LOCATION_HEADER = "Location";

    public static final String DATA_FOR_TESTS = "test-datas.yml";

    @Before
    public void setUp() {
        Fixtures.deleteAllModels();
        Fixtures.loadModels(DATA_FOR_TESTS);

    }

    public void tearsDown() {
    }

    @Test
    public void testThatValidationRulesCheckPictureUrlPresence() {
        Http.Request request = authenticateAndPopulateSessionWithoutPictureUrl(ZINDEP_WITHOUT_PICTURE_URL_EMAIL);
        Http.Response response = POST(request, "/admin/doUpdateMyProfile");
        //a 200 return code is done, but we are on the same page because he pictureUrl is null
        assertStatus(200, response);
    }


    @Test
    public void testThatValidationRulesOKWhenPictureUrlIsPresent() {
        Http.Request request = authenticateAndPopulateSession(ZINDEP_WITHOUT_PICTURE_URL_EMAIL);
        Http.Response response = POST(request, "/admin/doUpdateMyProfile");
        assertStatus(302, response);
        String redirectValue = response.headers.get(LOCATION_HEADER).values.get(0);
        assertTrue("redirect to  " + redirectValue + " instead of " + ADMIN_SHOW_MY_PROFILE, redirectValue.equals(ADMIN_SHOW_MY_PROFILE));
    }


}
