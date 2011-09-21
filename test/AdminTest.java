import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.Play;
import play.db.jpa.JPA;
import play.exceptions.UnexpectedException;
import play.libs.Crypto;
import play.libs.Time;
import play.mvc.Http;
import play.mvc.Scope;
import play.test.Fixtures;
import play.test.FunctionalTest;

import javax.persistence.TypedQuery;
import java.net.URLEncoder;
import java.util.Map;
import java.util.regex.Pattern;

public class AdminTest extends ZindepFunctionalTest{

    public static final String ADMIN_SHOW_MY_PROFILE = "/admin/showmyprofile";
    public static final String LOCATION_HEADER = "Location";
    public static final String ZINDEP_WITHOUT_PICTURE_URL_EMAIL = "pierre@letesteur.fr";
    public static final String PICTURE_ON_LINKED_IN = "http://media.linkedin.com/mpr/mprx/0_-vl8EBrjrIhgK-YHrn30Eqi1rwQtKlmHKN50EqACfmtC7zMeYPTuXN5uOL6S1n7X1nPY5Pfgv14Y";
    public static final String AUTHENTICITY_TOKEN_KEY = "___AT";
    public static final String DATA_FOR_TESTS = "test-datas.yml";

    @Before
    public void setUp(){
        Fixtures.deleteAllModels();
        Fixtures.loadModels(DATA_FOR_TESTS);
    }

    @After
    public void tearsDown(){
        Fixtures.deleteAllModels();
    }

    @Test
    public void testThatValidationRulesCheckPictureUrlPresence() {
        TypedQuery<String> query = JPA.em().createQuery("select z.id from models.Zindep z where z.email ='"+ ZINDEP_WITHOUT_PICTURE_URL_EMAIL +"'",String.class);
        String zindepId = query.getSingleResult();

        Http.Request request = authenticateAndPopulateProfileFormForFacelessUser(ZINDEP_WITHOUT_PICTURE_URL_EMAIL, zindepId);
        Http.Response response = POST(request,"/admin/doUpdateMyProfile");
        //a 200 return code is done, but we are on the same page because he pictureUrl is null
        assertStatus(200,response);
    }


    @Test
    public void testThatValidationRulesOKWhenPictureUrlIsPresent() {
        TypedQuery<String> query = JPA.em().createQuery("select z.id from models.Zindep z where z.email ='pierre@letesteur.fr'",String.class);
        String zindepId = query.getSingleResult();

        Http.Request request = authenticateAndPopulateProfileFormForFacelessUser(ZINDEP_WITHOUT_PICTURE_URL_EMAIL, zindepId);
        request.params.put("zindep.pictureUrl", PICTURE_ON_LINKED_IN);
        Http.Response response = POST(request,"/admin/doUpdateMyProfile");
        assertStatus(302,response);
        String redirectValue = response.headers.get(LOCATION_HEADER).values.get(0);
        assertTrue("redirect to  " + redirectValue + " instead of " + ADMIN_SHOW_MY_PROFILE, redirectValue.equals(ADMIN_SHOW_MY_PROFILE));
    }

    private Http.Request authenticateAndPopulateProfileFormForFacelessUser(String zindepEmailWithoutPictureUrl, String zindepId) {
        Http.Request request = setOpenIDAuthenticationSucess(zindepId, zindepEmailWithoutPictureUrl);
        request.params.put("zindep.email",zindepEmailWithoutPictureUrl);
        request.params.put("zindep.id",zindepId);
        request.params.put("zindep.firstName","Pierre");
        request.params.put("zindep.lastName","Letesteur");
        request.params.put("zindep.location","Paris");
        return request;
    }

    private Http.Request setOpenIDAuthenticationSucess(String zindepId, String zindepEmail) {
        Scope.Session session =Scope.Session.current();
        session.put(AUTHENTICITY_TOKEN_KEY,session.getAuthenticityToken());
        session.put("zindepId",zindepId);
        session.put("zindepEmail",zindepEmail);
        return setSession(session);
    }




    

}
