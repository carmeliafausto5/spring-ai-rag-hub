package io.github.ragHub.api.settings;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/settings")
public class SettingsController {

    private final SettingsService settingsService;

    public SettingsController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping
    public ResponseEntity<Map<String, String>> get() {
        Map<String, String> settings = settingsService.getAll();
        // Mask API keys in response
        settings.replaceAll((k, v) -> k.endsWith("api-key") && v != null && !v.isBlank()
                ? v.substring(0, Math.min(4, v.length())) + "****" : v);
        return ResponseEntity.ok(settings);
    }

    @PutMapping
    public ResponseEntity<Void> save(@RequestBody Map<String, String> settings) {
        settingsService.saveAll(settings);
        return ResponseEntity.noContent().build();
    }
}
