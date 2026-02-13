package com.greenko.assetservice.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AssetWebController {

    @GetMapping("/web/asset/swaggerui")
    public String redirectToSwaggerUi() {
        return "redirect:/swagger-ui/index.html";
    }
}