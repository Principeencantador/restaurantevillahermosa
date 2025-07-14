package com.example.demo.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.demo.dao.pedidorepository;
import com.example.demo.entity.Pedido;
import com.example.demo.entity.PedidoPlato;
import com.example.demo.stripe.pago;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.param.PaymentIntentCancelParams;
import com.stripe.param.PaymentIntentConfirmParams;
import com.stripe.param.PaymentIntentSearchParams;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.checkout.SessionCreateParams.LineItem;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class pagoimpl implements pagoservice {

    @Value("${stripe.key.secret}")
    String secretkey;

    @Autowired
    pedidorepository pedidodao;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public PaymentIntent paymentintent(pago paymentintent) throws StripeException {
        Stripe.apiKey = secretkey;
        Map<String, Object> params = new HashMap<>();
        params.put("amount", paymentintent.getAmount());
        params.put("currency", "pen");
        params.put("description", paymentintent.getDescription());

        // Si deseas añadir tipos de pago específicos, agrega aquí
        List<String> payment_method_types = new ArrayList<>();
        payment_method_types.add("card");

        return PaymentIntent.create(params);
    }

    @Override
    public PaymentIntent confirmtest(String id) throws StripeException {
        Stripe.apiKey = secretkey;
        PaymentIntent resource = PaymentIntent.retrieve(id);
        PaymentIntentConfirmParams params = PaymentIntentConfirmParams.builder()
                .setPaymentMethod("pm_card_visa")
                .setReturnUrl("http://localhost:3600/index")
                .build();
        return resource.confirm(params);
    }

    @Override
    public PaymentIntent cancel(String id) throws StripeException {
        Stripe.apiKey = secretkey;
        PaymentIntent resource = PaymentIntent.retrieve(id);
        PaymentIntentCancelParams params = PaymentIntentCancelParams.builder().build();
        return resource.cancel(params);
    }

    public PaymentIntent sendtest(String id) throws StripeException {
        Stripe.apiKey = secretkey;
        return PaymentIntent.retrieve(id);
    }

    public PaymentIntent search(String id) throws StripeException {
        Stripe.apiKey = secretkey;
        PaymentIntentSearchParams params = PaymentIntentSearchParams.builder().build();
        return PaymentIntent.search(params).getData()
                .stream()
                .filter(pi -> pi.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public ResponseEntity<Map<String, String>> sesionpay(Map<String, Object> mapeo) {
        try {
            Stripe.apiKey = secretkey;

            // 1) Obtener el pedido por ID
            int pedidoId = (int) mapeo.get("id_pedido");
            Pedido pedido = pedidodao.findById(pedidoId).orElse(null);
            if (pedido == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Pedido no encontrado"));
            }

            // 2) Construir line items
            List<LineItem> lineItems = new ArrayList<>();
            for (PedidoPlato pp : pedido.getPedidoPlatos()) {
                LineItem item = LineItem.builder()
                        .setQuantity((long) pp.getCantidad())
                        .setPriceData(
                            LineItem.PriceData.builder()
                                .setCurrency("pen")
                                .setUnitAmount((long)(pp.getPlato().getPrecio() * 100))
                                .setProductData(
                                    LineItem.PriceData.ProductData.builder()
                                        .setName(pp.getPlato().getNombre())
                                        .build()
                                )
                                .build()
                        )
                        .build();
                lineItems.add(item);
            }

            // 3) Crear session params, incluyendo metadata del pedido
            SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setCustomerEmail((String) mapeo.get("nombre"))
                .setPaymentIntentData(
                    SessionCreateParams.PaymentIntentData.builder()
                        .putMetadata("pedido_id", String.valueOf(pedidoId))
                        .build()
                )
                .setSuccessUrl("http://localhost:3000/success?pedido_id=" + pedidoId)
                .setCancelUrl("http://localhost:3000/pedidos")
                .addAllLineItem(lineItems)
                .build();

            // 4) Crear la sesión y devolver la URL
            Session session = Session.create(params);
            return ResponseEntity.ok(Map.of("url", session.getUrl()));

        } catch (StripeException e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
