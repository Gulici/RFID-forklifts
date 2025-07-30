package kcz.rfid.backend.service.utils;

import org.springframework.stereotype.Component;

@Component("testEpcParser")
public class TestEpcParser implements EpcParser {
    @Override
    public int parseZoneEpc(String epcCode) {
        String zoneIdHex = epcCode.substring(6, 8);
        return Integer.parseInt(zoneIdHex, 16);
    }
}
