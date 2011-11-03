import models.Mission;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;

public class MissionTest {


    @Test
    public void testGetIntermediaryCommissionWithIntermediaryPrice() {
        Mission mission = new Mission();
        mission.intermediaryPrice = (long) 100;
        mission.clientPrice = (long) 90;
        Assert.assertThat(mission.getIntermediaryCommission(), is("10.00"));
    }

    @Test
    public void testGetIntermediaryCommissionWithoutIntermediaryPrice() {
        Mission mission = new Mission();
        mission.intermediaryPrice = 0L;
        mission.clientPrice = (long) 90;
        Assert.assertThat(mission.getIntermediaryCommission(), is("0"));
    }
}
