package com.nexerp.domain.test;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Test API", description = "Test")
@RestController
@RequestMapping("/test")
public class TestController {
    @GetMapping()
    public String test(
    ){
        return "테스트입니다.";
    }
}
