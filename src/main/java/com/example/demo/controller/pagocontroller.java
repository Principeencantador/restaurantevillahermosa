package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.pagoimpl;
import com.example.demo.stripe.pago;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/stripe")
public class pagocontroller {

    @Autowired
    pagoimpl pagoimpl;

    @PostMapping("/paymentinten")
    public ResponseEntity<String> payment(@RequestBody pago pago) throws StripeException {

        PaymentIntent paymentIntent = pagoimpl.paymentintent(pago);
        String paymentString = paymentIntent.toJson();

        return new ResponseEntity<String>(paymentString, HttpStatus.OK);
    }

    @PostMapping("/confirm/{id}")
    public ResponseEntity<String> confirm(@PathVariable("id") String id) throws StripeException {
        PaymentIntent paymentIntent = pagoimpl.confirmtest(id);
        String paymentString = paymentIntent.toJson();
        return new ResponseEntity<String>(paymentString, HttpStatus.OK);
    }

    @PostMapping("/cancel/{id}")
    public ResponseEntity<String> cancel(@PathVariable("id") String id) throws StripeException {
        // TODO: process POST request
        PaymentIntent paymentIntent = pagoimpl.cancel(id);
        String paymentString = paymentIntent.toJson();

        return new ResponseEntity<String>(paymentString, HttpStatus.OK);
    }

}
