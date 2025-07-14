package com.example.demo.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Mesa;

@Repository
public interface mesarepository extends JpaRepository<Mesa, Integer> {
    public List<Mesa> mesalibre(@Param("capacidad") int capacidad, @Param("fecha") LocalDateTime fecha);

    public Mesa mesanro(@Param("nro_mesa") int nro_mesa);

}