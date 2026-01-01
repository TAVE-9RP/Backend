package com.nexerp.batch.ext.controller;

import com.nexerp.batch.ext.service.S3TestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/etl/test")
public class EtlTestController {

  private final S3TestService s3TestService;

  @GetMapping("/upload")
  public String testUpload() throws Exception {
    s3TestService.uploadTestFile();
    return "Uploaded!";
  }
}
