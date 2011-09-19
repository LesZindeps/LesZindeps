import org.junit.*;
import java.util.*;
import play.test.*;
import models.*;

/**
 * Exemple de test unitaire... libre à vous de compléter.
 */
public class ZindepTest extends UnitTest {


    @Before
    public void setUp(){
       Fixtures.deleteAllModels();
       Fixtures.loadModels("test-datas.yml");
    }

    @After
    public void tearsDown(){
        Fixtures.deleteAllModels();
    }

    
    @Test
    public void shouldReturns4ZindepForFindAllVisibleByName() {
        // the default is false
        List<Zindep> result=Zindep.findAllVisibleByName();
        assertNotNull(result);
        assertEquals(4,result.size());
    }

    @Test
    public void shouldReturnAnEmptyListWhenParamIsNullOrEmpty() {
        List<Zindep> result=Zindep.findByLastNameLike(null);
        assertNotNull(result);

        List<Zindep> result2=Zindep.findByLastNameLike("");
        assertNotNull(result2);
    }
    
    @Test
    public void findByMail() {
        Zindep result=Zindep.findByMail(null);
        assertNull(result);

        Zindep result2=Zindep.findByMail("");
        assertNull(result2);

        Zindep result3=Zindep.findByMail("pierre@letesteur.fr");
        assertNotNull(result3);
        assertEquals("Pierre", result3.firstName);
        assertEquals("Letesteur", result3.lastName);
    }



}
