package com.quynhlm.dev.be.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import ch.qos.logback.core.model.Model;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequestMapping(path = "/api/message")
public class MessageController {

    @GetMapping("/index")
    public String getIndexPage(Model model) {
        return "index";
    }
}
