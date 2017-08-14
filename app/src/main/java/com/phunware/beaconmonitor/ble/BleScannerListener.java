package com.phunware.beaconmonitor.ble;

import java.util.List;

/**
 * Created by Duy Nguyen on 8/1/17.
 */

public interface BleScannerListener
{
    void onBleScannerResult(List<LiveBeaconReading> readings);
}
