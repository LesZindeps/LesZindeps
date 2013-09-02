package controllers;

import models.Zindep;
import models.ZindepAvailabilitiesEntry;
import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.FunctionalTest;

import java.util.List;

public class BackOfficeTest extends FunctionalTest {
    @Before
    public void setUp(){
        Fixtures.deleteAllModels();
        Fixtures.loadModels("test-datas.yml");

        assertEquals(0, ZindepAvailabilitiesEntry.findAll().size());
    }

    @Test
    public void deleteZindeps_should_work_if_zindep_change_currentAvailability() {
        Zindep result = Zindep.findByMail("pierre@letesteur.fr");
        assertNotNull(result);
        result.currentAvailability = Zindep.Availability.PART_TIME_ONLY;
        result.save();

        assertEquals(1, ZindepAvailabilitiesEntry.findAll().size());

        BackOffice.deleteZindep(result);
    }

    @Test
    public void deleteZindeps_should_work_for_all() {
        List<Zindep> result = Zindep.findAll();
        for(Zindep z : result) {
            BackOffice.deleteZindep(z);
        }
    }
}
