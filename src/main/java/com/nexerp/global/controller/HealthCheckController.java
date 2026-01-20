package com.nexerp.global.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@RestController
public class HealthCheckController {

  @GetMapping("/health")
  public String healthCheck() {
    return "UP"; // ALB가 이 문자열과 200 OK를 받으면 정상으로 판단함
  }
}
