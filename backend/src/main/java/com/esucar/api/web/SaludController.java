package com.esucar.api.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class SaludController {
    @GetMapping({"/", "/api/salud"})
    public Map<String, String> salud() {
        return Map.of("estado", "ok", "servicio", "esucar-api");
    }
}
