package com.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RootForward {
    @GetMapping("/") String index() { return "forward:/index.html"; }
}