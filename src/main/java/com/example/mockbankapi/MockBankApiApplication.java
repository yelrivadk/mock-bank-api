package com.example.mockbankapi;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class MockBankApiApplication {
  @Value("${server.port}")
  private int serverPort;

  public static void main(String[] args) {
    SpringApplication.run(MockBankApiApplication.class, args);
  }

  @EventListener(ApplicationReadyEvent.class)
  public void printApplicationUrl() {
    System.out.println("Application started at: http://localhost:" + serverPort);
  }
}
