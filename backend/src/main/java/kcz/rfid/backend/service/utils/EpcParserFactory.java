package kcz.rfid.backend.service.utils;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class EpcParserFactory {
    private final Map<String, EpcParser> parserMap;
    private final String parserType;

    public EpcParserFactory(
            @Qualifier("defaultEpcParser") EpcParser defaultParser,
            @Qualifier("testEpcParser") EpcParser testParser,
            @Value("${epc.parser.strategy:default}") String parserType) {
        this.parserType = parserType;
        this.parserMap = Map.of(
                "default", defaultParser,
                "test", testParser
        );
    }

    public EpcParser getParser() {
        return parserMap.getOrDefault(parserType, parserMap.get("default"));
    }
}
