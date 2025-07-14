package com.example.demo.service;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.demo.stripe.pago;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;

@Service
public interface pagoservice {
    public PaymentIntent paymentintent(pago paymentintent) throws StripeException;

    public PaymentIntent confirmtest(String id) throws StripeException;

    public PaymentIntent cancel(String id) throws StripeException;

    public ResponseEntity<Map<String, String>> sesionpay(Map<String, Object> mapeo);

}
