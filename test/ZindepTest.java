import org.junit.*;
import java.util.*;
import play.test.*;
import models.*;

/**
 * Exemple de test unitaire... libre à vous de compléter.
 */
public class ZindepTest extends UnitTest {

    @Test
    public void shouldReturns5ZindepForFindAllByName() {
        List<Zindep> result=Zindep.findAllByName();
        assertNotNull(result);
        assertEquals(5,result.size());
    }

    @Test
    public void shouldReturnAnEmptyListWhenParamIsNullOrEmpty() {
        List<Zindep> result=Zindep.findByLastNameLike(null);
        assertNotNull(result);

        List<Zindep> result2=Zindep.findByLastNameLike("");
        assertNotNull(result2);

    }

}
