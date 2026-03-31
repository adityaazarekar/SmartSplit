package com.expense.service;

import com.expense.model.PaymentMethod;

import java.awt.Desktop;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class PaymentGatewayService {
    public static class PaymentResult {
        private final boolean success;
        private final String message;

        public PaymentResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }

    public PaymentResult initiatePayment(PaymentMethod method, double amount, String payerName, String payeeName, String upiId, String cardNumber) {
        try {
            if (!Desktop.isDesktopSupported()) {
                return new PaymentResult(false, "Desktop integration is unavailable on this machine.");
            }

            switch (method) {
                case UPI:
                    if (upiId == null || upiId.isBlank() || !upiId.contains("@")) {
                        return new PaymentResult(false, "Enter a valid UPI ID (example: name@bank).");
                    }
                    String upiUrl = "upi://pay?pa=" + enc(upiId)
                            + "&pn=" + enc(payeeName)
                            + "&am=" + enc(String.format("%.2f", amount))
                            + "&cu=INR&tn=" + enc("SmartSplit settlement by " + payerName);
                    Desktop.getDesktop().browse(URI.create(upiUrl));
                    return new PaymentResult(true, "UPI app launched. Complete payment on your phone.");

                case CREDIT_CARD:
                case DEBIT_CARD:
                case NET_BANKING:
                    String hostedCheckout = System.getenv("SMARTSPLIT_CHECKOUT_URL");
                    if (hostedCheckout == null || hostedCheckout.isBlank()) {
                        return new PaymentResult(false,
                                "Configure SMARTSPLIT_CHECKOUT_URL to a live gateway page (Razorpay/Stripe hosted checkout).");
                    }
                    if ((method == PaymentMethod.CREDIT_CARD || method == PaymentMethod.DEBIT_CARD)
                            && (cardNumber == null || cardNumber.replaceAll("\\s+", "").length() < 12)) {
                        return new PaymentResult(false, "Enter a valid card number.");
                    }
                    String url = hostedCheckout
                            + (hostedCheckout.contains("?") ? "&" : "?")
                            + "amount=" + enc(String.format("%.2f", amount))
                            + "&payer=" + enc(payerName)
                            + "&payee=" + enc(payeeName)
                            + "&method=" + enc(method.name());
                    Desktop.getDesktop().browse(URI.create(url));
                    return new PaymentResult(true, "Hosted checkout opened in browser.");

                case CASH:
                default:
                    return new PaymentResult(true, "Cash settlement recorded.");
            }
        } catch (Exception ex) {
            return new PaymentResult(false, "Payment initiation failed: " + ex.getMessage());
        }
    }

    private String enc(String s) {
        return URLEncoder.encode(s == null ? "" : s, StandardCharsets.UTF_8);
    }
}
