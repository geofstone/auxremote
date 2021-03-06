package com.bobs.io;

import com.bobs.TestConfig;
import com.bobs.mount.GuideRate;
import com.bobs.mount.Mount;
import com.bobs.mount.TrackingMode;
import com.bobs.serialcommands.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by dokeeffe on 24/12/16.
 */
@Ignore //This test was used as part of the experimental POC work when connected to a physical mount.
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
public class NexstarAuxSerialAdapterExploratoryTest {

    @Autowired
    private NexstarAuxSerialAdapter sut;

    @Autowired
    private Mount mount;

    @Before
    public void setup() {
        sut.setSerialPortBuilder(new SerialPortBuilder());
        mount.setTrackingMode(TrackingMode.EQ_NORTH);
        mount.setLatitude(52.0);
        mount.setLongitude(357.0);
        sut.setSerialPortName("/dev/celestron");
        new Thread(sut).start();

    }


    @Test
    public void sendMountPositionQueries() throws Exception {
        sut.queueCommand(new QueryAzMcPosition(mount));
        sut.queueCommand(new SetGuideRate(mount, MountCommand.AZM_BOARD, GuideRate.OFF));
        sut.queueCommand(new SetGuideRate(mount, MountCommand.ALT_BOARD, GuideRate.OFF));
        sut.queueCommand(new SetGuideRate(mount, MountCommand.AZM_BOARD, GuideRate.SIDEREAL));

        while (true) {
            Thread.sleep(2000);
            sut.queueCommand(new QueryAzMcPosition(mount));
            sut.queueCommand(new QueryAltMcPosition(mount));
            System.out.println("RA:" + mount.getRaHours());
            System.out.println("DEC:" + mount.getDecDegrees());
        }

    }

    @Test
    public void sendMountSync() throws Exception {
        sut.queueCommand(new SetGuideRate(mount, MountCommand.AZM_BOARD, GuideRate.OFF));
        sut.queueCommand(new SetGuideRate(mount, MountCommand.ALT_BOARD, GuideRate.OFF));
        sut.queueCommand(new SetGuideRate(mount, MountCommand.AZM_BOARD, GuideRate.SIDEREAL));

        sut.queueCommand(new QueryAzMcPosition(mount));
        sut.queueCommand(new QueryAltMcPosition(mount));
        Thread.sleep(2000);
        System.out.println("RA:" + mount.getRaHours());
        System.out.println("DEC:" + mount.getDecDegrees());
        sut.queueCommand(new SetAltMcPosition(mount, 16.540));
        sut.queueCommand(new SetAzMcPosition(mount, 203.932));
        Thread.sleep(2000);
        sut.queueCommand(new QueryAzMcPosition(mount));
        sut.queueCommand(new QueryAltMcPosition(mount));
        Thread.sleep(2000);
        System.out.println("RA:" + mount.getRaHours());
        System.out.println("DEC:" + mount.getDecDegrees());
        sut.stop();

    }

    @Test
    public void sendGpsCommands() throws Exception {
        while (true) {
            sut.queueCommand(new GpsLinked(mount));
            sut.queueCommand(new GpsLat(mount));
            sut.queueCommand(new GpsLon(mount));
            sut.queueCommand(new GpsRecieverStatus(mount));
            Thread.sleep(10000);
            System.out.println("GPS:" + mount.isGpsConnected());
            System.out.println("LAT:" + mount.getLatitude());
            System.out.println("LON:" + mount.getLongitude());
        }

    }


    @Test
    public void queryCordWrap() {
        sut.queueCommand(new QueryCordWrap(mount));
        sut.queueCommand(new QueryCordWrapPos(mount));
        sut.queueCommand(new EnableCordWrap(mount));
        sut.queueCommand(new QueryCordWrap(mount));
        sut.waitForQueueEmpty();
    }
}