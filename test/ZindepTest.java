import models.Zindep;
import models.ZindepAvailabilitiesEntry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;


public class ZindepTest extends UnitTest {


    @Before
    public void setUp() {
        Fixtures.deleteAllModels();
        Fixtures.loadModels("test-datas.yml");
    }

    @After
    public void tearsDown() {

    }


    @Test
    public void shouldReturns4ZindepForFindAllVisibleByName() {

        List<Zindep> result = Zindep.findAllVisibleByName();
        assertNotNull(result);
        assertEquals(8, result.size());
    }

    @Test
    public void shouldReturnAnEmptyListWhenParamIsNullOrEmpty() {
        List<Zindep> result = Zindep.findByLastNameLike(null);
        assertNotNull(result);

        List<Zindep> result2 = Zindep.findByLastNameLike("");
        assertNotNull(result2);
    }

    @Test
    public void findByMail() {
        Zindep result = Zindep.findByMail(null);
        assertNull(result);

        Zindep result2 = Zindep.findByMail("");
        assertNull(result2);

        Zindep result3 = Zindep.findByMail("pierre@letesteur.fr");
        assertNotNull(result3);
        assertEquals("Pierre", result3.firstName);
        assertEquals("Letesteur", result3.lastName);
    }


    @Test
    public void testFindAllByAvailability_nominal_case() {

        List<Zindep> zindepsNotAvailable = Zindep.findAllByAvailability(Zindep.Availability.NOT_AVAILABLE);
        assertThat(zindepsNotAvailable.size(), is(6));

        List<Zindep> zindepsPartTimeOnly = Zindep.findAllByAvailability(Zindep.Availability.PART_TIME_ONLY);
        assertThat(zindepsPartTimeOnly.size(), is(1));

        List<Zindep> zindepsFullTime = Zindep.findAllByAvailability(Zindep.Availability.FULL_TIME);
        assertThat(zindepsFullTime.size(), is(3));


    }

    @Test
    public void testFindAllByAvailability_with_a_change_from_NOT_AVAILABLE_TO_AVAILABLE() {

        List<Zindep> zindepsNotAvailable = Zindep.findAllByAvailability(Zindep.Availability.NOT_AVAILABLE);
        assertThat(zindepsNotAvailable.size(), is(6));

        List<Zindep> zindepsPartTimeOnly = Zindep.findAllByAvailability(Zindep.Availability.PART_TIME_ONLY);
        assertThat(zindepsPartTimeOnly.size(), is(1));

        List<Zindep> zindepsFullTime = Zindep.findAllByAvailability(Zindep.Availability.FULL_TIME);
        assertThat(zindepsFullTime.size(), is(3));

        //change
        Zindep zindep = zindepsFullTime.get(0);
        zindep.setCurrentAvailability(Zindep.Availability.NOT_AVAILABLE);
        zindep.save();

        //check
        zindepsNotAvailable = Zindep.findAllByAvailability(Zindep.Availability.NOT_AVAILABLE);
        assertThat(zindepsNotAvailable.size(), is(7));

        zindepsPartTimeOnly = Zindep.findAllByAvailability(Zindep.Availability.PART_TIME_ONLY);
        assertThat(zindepsPartTimeOnly.size(), is(1));

        zindepsFullTime = Zindep.findAllByAvailability(Zindep.Availability.FULL_TIME);
        assertThat(zindepsFullTime.size(), is(2));

    }


    @Test
    public void testFindAllByAvailability_with_removal_of_one_zindep() {
        List<Zindep> zindepsNotAvailable = Zindep.findAllByAvailability(Zindep.Availability.NOT_AVAILABLE);
        assertThat(zindepsNotAvailable.size(), is(6));
        Zindep zindep = zindepsNotAvailable.get(0);
        zindep.delete();
        zindepsNotAvailable = Zindep.findAllByAvailability(Zindep.Availability.NOT_AVAILABLE);
        assertThat(zindepsNotAvailable.size(), is(5));
    }

    @Test
    public void testFindAllByAvailability_with_addition_of_one_zindep() {
        List<Zindep> zindepsNotAvailable = Zindep.findAllByAvailability(Zindep.Availability.NOT_AVAILABLE);
        assertThat(zindepsNotAvailable.size(), is(6));
        Zindep zindep = new Zindep();
        zindep.firstName = "john";
        zindep.lastName = "lennon";
        zindep.setCurrentAvailability(Zindep.Availability.NOT_AVAILABLE);
        zindep.save();
        zindepsNotAvailable = Zindep.findAllByAvailability(Zindep.Availability.NOT_AVAILABLE);
        assertThat(zindepsNotAvailable.size(), is(7));
    }


    @Test
    public void testZindepAvailabilitiesEntry_creation() {
        List<Zindep> zindepsNotAvailable = Zindep.findAllByAvailability(Zindep.Availability.NOT_AVAILABLE);
        assertThat(zindepsNotAvailable.size(), is(6));

        List<Zindep> zindepsPartTimeOnly = Zindep.findAllByAvailability(Zindep.Availability.PART_TIME_ONLY);
        assertThat(zindepsPartTimeOnly.size(), is(1));

        List<Zindep> zindepsFullTime = Zindep.findAllByAvailability(Zindep.Availability.FULL_TIME);
        assertThat(zindepsFullTime.size(), is(3));

        List<ZindepAvailabilitiesEntry> entries = ZindepAvailabilitiesEntry.findAll();
        assertThat(entries.size(), is(0));

        //change
        Zindep zindep = zindepsFullTime.get(0);
        zindep.setCurrentAvailability(Zindep.Availability.NOT_AVAILABLE);
        zindep.save();

        //check
        entries = ZindepAvailabilitiesEntry.findAll();
        assertThat(entries.size(), is(1));
        ZindepAvailabilitiesEntry entry = entries.get(0);
        assertThat(entry.currentAvailability, is(Zindep.Availability.NOT_AVAILABLE));
        assertThat(entry.previousAvailability, is(Zindep.Availability.FULL_TIME));

    }


    @Test
    public void test_setCurrentAvailability_nominal_case() {

        //given
        ZindepAvailabilitiesEntry.deleteAll();
        String zindepIds = Zindep.getZindepIdsAsString(Zindep.Availability.NOT_AVAILABLE);
        List<String> ids = Arrays.asList(zindepIds.split(","));
        assertThat(ids.size(), is(6));
        assertThat(new Long(ZindepAvailabilitiesEntry.count()).intValue(), is(0));

        //when
        Zindep zindep = Zindep.findById(ids.get(0));
        zindep.setCurrentAvailability(Zindep.Availability.FULL_TIME);
        zindep.save();

        //then
        assertThat(new Long(ZindepAvailabilitiesEntry.count()).intValue(), is(1));

    }

    @Test
    public void test_setCurrentAvailability_change_and_revert() {

        //given
        ZindepAvailabilitiesEntry.deleteAll();
        String zindepIds = Zindep.getZindepIdsAsString(Zindep.Availability.NOT_AVAILABLE);
        List<String> ids = Arrays.asList(zindepIds.split(","));
        assertThat(ids.size(), is(6));
        assertThat(new Long(ZindepAvailabilitiesEntry.count()).intValue(), is(0));

        //when
        Zindep zindep = Zindep.findById(ids.get(0));
        zindep.setCurrentAvailability(Zindep.Availability.FULL_TIME);
        zindep.save();
        zindep.setCurrentAvailability(Zindep.Availability.NOT_AVAILABLE);
        zindep.save();

        //then
        assertThat(new Long(ZindepAvailabilitiesEntry.count()).intValue(), is(2));

    }


}
