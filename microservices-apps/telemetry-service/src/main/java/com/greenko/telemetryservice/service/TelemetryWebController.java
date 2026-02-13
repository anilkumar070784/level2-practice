package com.greenko.telemetryservice.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TelemetryWebController {

    @GetMapping("/web/telemetry/swaggerui")
    public String redirectToSwaggerUi() {
        return "redirect:/swagger-ui/index.html";
    }
}