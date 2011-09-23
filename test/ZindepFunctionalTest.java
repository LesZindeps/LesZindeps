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
import java.util.regex.Pattern;

/**
 * base test for functional Tests.
 * it add some specific methods like setSession.
 */
public class ZindepFunctionalTest extends FunctionalTest {

    public static final String COOKIE_PREFIX = Play.configuration.getProperty("application.session.cookie", "PLAY");
    public static final boolean COOKIE_SECURE = Play.configuration.getProperty("application.session.secure", "false").toLowerCase().equals("true");
    public static final String COOKIE_EXPIRE = Play.configuration.getProperty("application.session.maxAge");
    public static final boolean SESSION_HTTPONLY = Play.configuration.getProperty("application.session.httpOnly", "false").toLowerCase().equals("true");
    public static final boolean SESSION_SEND_ONLY_IF_CHANGED = Play.configuration.getProperty("application.session.sendOnlyIfChanged", "false").toLowerCase().equals("true");
    static Pattern sessionParser = Pattern.compile("\u0000([^:]*):([^\u0000]*)\u0000");
    static final String TIMESTAMP_KEY = "___TS";
    public static final String AUTHENTICITY_TOKEN_KEY = "___AT";
    public static final String ZINDEP_WITHOUT_PICTURE_URL_EMAIL = "pierre@letesteur.fr";
    public static final String PICTURE_ON_LINKED_IN = "http://media.linkedin.com/mpr/mprx/0_-vl8EBrjrIhgK-YHrn30Eqi1rwQtKlmHKN50EqACfmtC7zMeYPTuXN5uOL6S1n7X1nPY5Pfgv14Y";


    /**
     * store Session in cookie request and response, and set the authenticityTOken into param form.
     *
     * @param session
     * @return
     */
    private Http.Request setSession(Scope.Session session) {
        Http.Request request = newRequest();
        save(session);
        request.cookies = Http.Response.current().cookies;
        request.params.put("authenticityToken", session.getAuthenticityToken());
        return request;
    }

    /**
     * save session into the Http.Response.current() response.
     * this code comes from play! framework code.
     *
     * @param ses
     */
    private void save(Scope.Session ses) {
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

    private Http.Request setOpenIDAuthenticationSucess(String zindepId, String zindepEmail) {
        Scope.Session session = Scope.Session.current();
        session.put(AUTHENTICITY_TOKEN_KEY, session.getAuthenticityToken());
        session.put("zindepId", zindepId);
        session.put("zindepEmail", zindepEmail);
        return setSession(session);
    }

    protected Http.Request authenticateAndPopulateSession(String zindepEmail) {
        Http.Request request = authenticateAndPopulateSessionWithoutPictureUrl(zindepEmail);
        request.params.put("zindep.pictureUrl", PICTURE_ON_LINKED_IN);
        return request;
    }

    public String authenticateAndReturnSessionValue(String zindepEmail) {
        Http.Request request = authenticateAndPopulateSessionWithoutPictureUrl(zindepEmail);
        request.params.put("zindep.pictureUrl", PICTURE_ON_LINKED_IN);
        String value = request.cookies.get("PLAY_SESSION").value;
        return value;
    }

    protected Http.Request authenticateAndPopulateSessionWithoutPictureUrl(String zindepEmail) {
        TypedQuery<String> query = JPA.em().createQuery("select z.id from models.Zindep z where z.email ='" + zindepEmail + "'", String.class);
        String zindepId = query.getSingleResult();
        Http.Request request = authenticateAndPopulateProfileFormForFacelessUser(zindepEmail, zindepId);
        return request;
    }

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
