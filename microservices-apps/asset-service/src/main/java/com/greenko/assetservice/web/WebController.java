package com.greenko.assetservice.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class WebController {


    @GetMapping("/api")
    public String getSwaggerUI(){
        return "swagger-ui.html";
    }

}
