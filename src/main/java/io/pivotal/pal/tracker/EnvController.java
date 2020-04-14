package io.pivotal.pal.tracker;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class EnvController {

    private final Map<String, String> values = new HashMap<>();

    public EnvController(@Value("PORT:NOT SET") String port,
                         @Value("MEMORY_LIMIT:NOT SET") String memoryLimit,
                         @Value("CF_INSTANCE_INDEX:NOT SET") String cfInstanceIndex,
                         @Value("CF_INSTANCE_ADDR:NOT SET") String cfInstanceAddress) {
        values.put("PORT", port);
        values.put("MEMORY_LIMIT", memoryLimit);
        values.put("CF_INSTANCE_INDEX", cfInstanceIndex);
        values.put("CF_INSTANCE_ADDR", cfInstanceAddress);
    }

    @GetMapping("/env")
    public Map<String, String> getEnv() {
        return values;
    }

}
