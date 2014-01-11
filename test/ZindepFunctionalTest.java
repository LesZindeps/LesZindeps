import org.junit.Test;
import play.Play;
import play.db.jpa.JPA;
import play.exceptions.UnexpectedException;
import play.libs.Crypto;
import play.libs.Time;
import play.mvc.Http;
import play.mvc.Scope;
import play.test.FunctionalTest;

import javax.persistence.TypedQuery;
import java.net.URLEncoder;
import java.util.Map;

/**
 * base test for functional Tests.
 * it add some specific methods like setSessionWithNewRequest.
 */
public class ZindepFunctionalTest extends FunctionalTest {

    public static final String COOKIE_PREFIX = Play.configuration.getProperty("application.session.cookie", "PLAY");
    public static final boolean COOKIE_SECURE = Play.configuration.getProperty("application.session.secure", "false").toLowerCase().equals("true");
    public static final String COOKIE_EXPIRE = Play.configuration.getProperty("application.session.maxAge");
    public static final boolean SESSION_HTTPONLY = Play.configuration.getProperty("application.session.httpOnly", "false").toLowerCase().equals("true");
    public static final String AUTHENTICITY_TOKEN_KEY = "___AT";
    public static final String ZINDEP_WITHOUT_PICTURE_URL_EMAIL = "pierre@letesteur.fr";
    public static final String PICTURE_ON_LINKED_IN = "http://media.linkedin.com/mpr/mprx/0_-vl8EBrjrIhgK-YHrn30Eqi1rwQtKlmHKN50EqACfmtC7zMeYPTuXN5uOL6S1n7X1nPY5Pfgv14Y";
    public static final String ZINDEP_ID_SESSION_KEY = "zindepId";
    public static final String ZINDEP_EMAIL_SESSION_KEY = "zindepEmail";
    public static final String ZINDEP_PICTURE_URL_SESSION_KEY = "zindep.pictureUrl";
    static final String TIMESTAMP_KEY = "___TS";

    /**
     * store Session in cookie request and response, and set the authenticityToken into param form.
     */
    public Http.Request setSessionWithNewRequest(Scope.Session session) {
        Http.Request request = newRequest();
        saveSessionInCurrentRequest(session);
        request.cookies = Http.Response.current().cookies;
        request.params.put("authenticityToken", session.getAuthenticityToken());
        return request;
    }

    /**
     * save session into the Http.Response.current() response.
     * this code comes from play! framework code.
     */
    private void saveSessionInCurrentRequest(Scope.Session ses) {
        Map<String, String> data = ses.all();


        if (isEmpty(data)) {
            // The session is empty: delete the cookie
            Http.Response.current().setCookie(COOKIE_PREFIX + "_SESSION", "", null, "/", 0, COOKIE_SECURE, SESSION_HTTPONLY);
            return;
        }
        try {
            StringBuilder session = new StringBuilder();
            for (String key : data.keySet()) {
                session.append("\u0000");
                session.append(key);
                session.append(":");
                session.append(data.get(key));
                session.append("\u0000");
            }
            String sessionData = URLEncoder.encode(session.toString(), "utf-8");
            String sign = Crypto.sign(sessionData, Play.secretKey.getBytes());
            if (COOKIE_EXPIRE == null) {
                Http.Response.current().setCookie(COOKIE_PREFIX + "_SESSION", sign + "-" + sessionData, null, "/", null, COOKIE_SECURE, SESSION_HTTPONLY);
            } else {
                Http.Response.current().setCookie(COOKIE_PREFIX + "_SESSION", sign + "-" + sessionData, null, "/", Time.parseDuration(COOKIE_EXPIRE), COOKIE_SECURE, SESSION_HTTPONLY);
            }
        } catch (Exception e) {
            throw new UnexpectedException("Session serializationProblem", e);
        }
    }

    /**
     * Returns true if the session is empty,
     * e.g. does not contain anything else than the timestamp
     */
    private boolean isEmpty(Map<String, String> data) {
        for (String key : data.keySet()) {
            if (!TIMESTAMP_KEY.equals(key)) {
                return false;
            }
        }
        return true;
    }

    @Test
    public void dummyTest() {

    }

    /**
     * simulate an openID authentication success for the zindep with the id and email provided.
     *
     * @param zindepId    id of Zindep
     * @param zindepEmail email of the zindep successfully authenticated
     */
    private Http.Request setOpenIDAuthenticationSucess(String zindepId, String zindepEmail) {
        Scope.Session session = Scope.Session.current();
        session.put(AUTHENTICITY_TOKEN_KEY, session.getAuthenticityToken());
        session.put(ZINDEP_ID_SESSION_KEY, zindepId);
        session.put(ZINDEP_EMAIL_SESSION_KEY, zindepEmail);
        return setSessionWithNewRequest(session);
    }

    protected Http.Request authenticateAndPopulateSession(String zindepEmail) {
        Http.Request request = authenticateAndPopulateSessionWithoutPictureUrl(zindepEmail);
        request.params.put(ZINDEP_PICTURE_URL_SESSION_KEY, PICTURE_ON_LINKED_IN);
        return request;
    }

    public String authenticateAndReturnSessionValue(String zindepEmail) {
        Http.Request request = authenticateAndPopulateSessionWithoutPictureUrl(zindepEmail);
        request.params.put(ZINDEP_PICTURE_URL_SESSION_KEY, PICTURE_ON_LINKED_IN);
        return request.cookies.get("PLAY_SESSION").value;
    }

    protected Http.Request authenticateAndPopulateSessionWithoutPictureUrl(String zindepEmail) {
        TypedQuery<String> query = JPA.em().createQuery("select z.id from models.Zindep z where z.email ='" + zindepEmail + "'", String.class);
        String zindepId = query.getSingleResult();
        return authenticateAndPopulateProfileFormForFacelessUser(zindepEmail, zindepId);
    }

    /**
     * authenticate a zindep which does not have a picture in its profile.
     */
    private Http.Request authenticateAndPopulateProfileFormForFacelessUser(String zindepEmailWithoutPictureUrl, String zindepId) {
        Http.Request request = setOpenIDAuthenticationSucess(zindepId, zindepEmailWithoutPictureUrl);
        request.params.put("zindep.email", zindepEmailWithoutPictureUrl);
        request.params.put("zindep.id", zindepId);
        request.params.put("zindep.firstName", "Pierre");
        request.params.put("zindep.lastName", "Letesteur");
        request.params.put("zindep.location", "Paris");
        return request;
    }


}
