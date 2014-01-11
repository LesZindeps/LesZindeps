import models.OldZindep;
import models.Zindep;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.db.jpa.JPABase;
import play.test.Fixtures;
import play.test.UnitTest;

import java.util.List;

public class OldZindepTest extends UnitTest {


    @Before
    public void setUp() {
        Fixtures.deleteAllModels();
        Fixtures.loadModels("test-datas.yml");
    }

    @After
    public void tearsDown() {
        Fixtures.deleteAllModels();
    }

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
