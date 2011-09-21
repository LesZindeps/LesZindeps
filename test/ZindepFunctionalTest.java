import play.Play;
import play.exceptions.UnexpectedException;
import play.libs.Crypto;
import play.libs.Time;
import play.mvc.Http;
import play.mvc.Scope;
import play.test.FunctionalTest;

import java.net.URLEncoder;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * base test for functional Tests.
 * it add some specific methods like setSession.
 */
public abstract class ZindepFunctionalTest extends FunctionalTest {

    public static final String COOKIE_PREFIX = Play.configuration.getProperty("application.session.cookie", "PLAY");
    public static final boolean COOKIE_SECURE = Play.configuration.getProperty("application.session.secure", "false").toLowerCase().equals("true");
    public static final String COOKIE_EXPIRE = Play.configuration.getProperty("application.session.maxAge");
    public static final boolean SESSION_HTTPONLY = Play.configuration.getProperty("application.session.httpOnly", "false").toLowerCase().equals("true");
    public static final boolean SESSION_SEND_ONLY_IF_CHANGED = Play.configuration.getProperty("application.session.sendOnlyIfChanged", "false").toLowerCase().equals("true");
    static Pattern sessionParser = Pattern.compile("\u0000([^:]*):([^\u0000]*)\u0000");
    static final String TIMESTAMP_KEY = "___TS";



     /**
     * store Session in cookie request and response, and set the authenticityTOken into param form.
     * @param session
     * @return
     */
    protected Http.Request setSession(Scope.Session session) {
        Http.Request request = newRequest();
        save(session);
        request.cookies = Http.Response.current().cookies;
        request.params.put("authenticityToken",session.getAuthenticityToken());
        return request;
    }

    /**
     * save session into the Http.Response.current() response.
     * this code comes from play! framework code.
     * @param ses
     */
    void save(Scope.Session ses) {
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
    public boolean isEmpty(Map<String, String> data) {
        for (String key : data.keySet()) {
            if (!TIMESTAMP_KEY.equals(key)) {
                return false;
            }
        }
        return true;
    }

}
