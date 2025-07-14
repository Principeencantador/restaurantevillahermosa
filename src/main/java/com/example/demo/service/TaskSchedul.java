package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@EnableScheduling
@Service
public class TaskSchedul {

    @Autowired
    private reservaimpl reservaService;

    @Scheduled(fixedRate = 60000)
    public void actualizarEstadoDeReservas() {
        reservaService.actualizarEstadoReserva();
    }
}
