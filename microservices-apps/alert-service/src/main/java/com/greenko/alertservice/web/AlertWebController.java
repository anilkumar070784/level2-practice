package com.greenko.alertservice.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AlertWebController {

    @GetMapping("/web/alert/swaggerui")
    public String redirectToSwaggerUi() {
        return "redirect:/swagger-ui/index.html";
    }
}