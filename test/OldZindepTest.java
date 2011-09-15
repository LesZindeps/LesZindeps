import models.OldZindep;
import models.Zindep;
import org.junit.Test;
import play.db.jpa.JPABase;
import play.test.UnitTest;

import java.util.List;

/**
 */
public class OldZindepTest extends UnitTest {
    @Test
    public void createAndFindAll() {
        Zindep result = Zindep.findByMail("pierre@letesteur.fr");
        assertNotNull(result);

        List<JPABase> all = OldZindep.findAll();
        assertEquals(0, all.size());

        OldZindep oldZindep = new OldZindep(result);
        oldZindep.save();

        all = OldZindep.findAll();
        assertEquals(1, all.size());
    }

}
