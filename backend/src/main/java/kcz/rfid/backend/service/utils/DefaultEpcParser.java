package kcz.rfid.backend.service.utils;

import org.springframework.stereotype.Component;

@Component("defaultEpcParser")
public class DefaultEpcParser implements EpcParser {
    @Override
    public int parseZoneEpc(String epcCode) {
        String zoneIdHex = epcCode.substring(2, 6);
        return Integer.parseInt(zoneIdHex, 16);
    }
}
