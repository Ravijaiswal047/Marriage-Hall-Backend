package com.marriagehall.hall_service.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/halls/config")
@RefreshScope   // <-- bean is rebuilt on /actuator/refresh, re-reading the value below
public class RefreshDemoController {

    @Value("${app.message:no message configured}")
    private String message;

    @GetMapping("/message")
    public String message() {
        return message;
    }
}
