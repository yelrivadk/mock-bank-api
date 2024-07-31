package com.example.mockbankapi;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Controller
@RequestMapping("/api/payment")
public class PaymentController {

  private static final String STATUS_SUCCESS = "success";
  private static final String STATUS_FAILURE = "failure";
  private static final String STATUS_3D_SECURE = "secure";

  private final ConcurrentMap<String, String> transactions = new ConcurrentHashMap<>();
  private final RestTemplate restTemplate = new RestTemplate();

  @PostMapping("/process")
  public ResponseEntity<PaymentResponse> processPayment(@RequestBody PaymentRequest request) {
    var transactionId = "txn" + System.currentTimeMillis();
    var existingStatus = transactions.get(transactionId);

    if (existingStatus != null) {
      return ResponseEntity.ok(PaymentResponse.builder()
        .transactionId(transactionId)
        .status(STATUS_FAILURE)
        .message("Transaction already exists")
        .build());
    }

    var random = new Random();
    var result = random.nextInt(5);

    return switch (result) {
      case 0 -> {
        transactions.put(transactionId, STATUS_SUCCESS);
        yield ResponseEntity.ok(PaymentResponse.builder()
          .transactionId(transactionId)
          .status(STATUS_SUCCESS)
          .build());
      }
      case 1 -> {
        transactions.put(transactionId, STATUS_FAILURE);
        yield ResponseEntity.ok(PaymentResponse.builder()
          .transactionId(transactionId)
          .status(STATUS_FAILURE)
          .message("Insufficient funds")
          .build());
      }
      case 2, 3, 4 -> {
        transactions.put(transactionId, STATUS_3D_SECURE);
        yield ResponseEntity.ok(PaymentResponse.builder()
          .transactionId(transactionId)
          .status(STATUS_3D_SECURE)
          .message("http://localhost:8888/api/payment/3d-secure?transactionId=" + transactionId)
          .build());
      }
      default -> throw new IllegalStateException("Unexpected value: " + result);
    };
  }

  @GetMapping("/3d-secure")
  public String get3DSecurePage(@RequestParam String transactionId, Model model) {
    model.addAttribute("transactionId", transactionId);
    return "3d-secure";
  }


  @PostMapping("/validate-token")
  public String validateToken(@RequestParam("transactionId") String transactionId,
                              @RequestParam("token") String token,
                              Model model) {
    var resultPage = "result";
    var correctToken = "9999";

    if (!Objects.equals(token, correctToken)) {
      model.addAttribute("successful", false);
      model.addAttribute("message", "3D Secure Authentication Failed! (9999 is a valid test token)");
      return resultPage;
    }

    try {
      var response = callGateway(PaymentResponse.builder()
        .status(STATUS_SUCCESS)
        .transactionId(transactionId)
        .build());

      if (response.getStatusCode().is2xxSuccessful()) {
        return "redirect:" + response.getBody();
      }
    } catch (Exception e) {
      model.addAttribute("successful", false);
      model.addAttribute("message", e.getMessage());
      return resultPage;
    }

    model.addAttribute("successful", false);
    model.addAttribute("message", "Payment Failed!");
    return resultPage;
  }

  private ResponseEntity<String> callGateway(PaymentResponse paymentResponse) {
    var url = "http://localhost:8000/api/payment/3ds-callback";
    var headers = new HttpHeaders();
    headers.set("Content-Type", "application/json");
    var request = new HttpEntity<>(paymentResponse, headers);
    return restTemplate.exchange(url, HttpMethod.POST, request, String.class);
  }
}
