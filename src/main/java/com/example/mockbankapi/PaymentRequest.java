package com.example.mockbankapi;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentRequest {
  private String transactionReference;
  private Merchant merchant;
  private Instruction instruction;
  private String channel;

  @Data
  @Builder
  public static class Merchant {
    private String entity;
  }

  @Data
  @Builder
  public static class Instruction {
    private RequestAutoSettlement requestAutoSettlement;
    private Value value;
    private PaymentInstrument paymentInstrument;

    @Data
    @Builder
    public static class RequestAutoSettlement {
      private boolean enabled;
    }

    @Data
    @Builder
    public static class Value {
      private String currency;
      private int amount;
    }

    @Data
    @Builder
    public static class PaymentInstrument {
      private String type;
      private String cardNumber;
      private ExpiryDate expiryDate;

      @Data
      @Builder
      public static class ExpiryDate {
        private int month;
        private int year;
      }
    }
  }
}
