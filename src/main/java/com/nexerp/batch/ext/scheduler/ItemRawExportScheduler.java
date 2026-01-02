//package com.nexerp.batch.ext.scheduler;
//
//import jakarta.annotation.PostConstruct;
//import lombok.RequiredArgsConstructor;
//import org.springframework.batch.core.Job;
//import org.springframework.batch.core.JobParametersBuilder;
//import org.springframework.batch.core.launch.JobLauncher;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//@Component
//@RequiredArgsConstructor
//public class ItemRawExportScheduler {
//
//  private final JobLauncher jobLauncher;
//  private final Job itemRawExportJob;
//
//
//  // @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
//  // 우선은 테스트용으로 앱 실행 시 바로 실행
//  @PostConstruct
//  public void run() throws Exception {
//    jobLauncher.run(
//      itemRawExportJob,
//      new JobParametersBuilder()
//        .addLong("runAt", System.currentTimeMillis())
//        .toJobParameters()
//    );
//  }
//
//}
