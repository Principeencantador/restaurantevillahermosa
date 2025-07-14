package com.example.demo.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Plato;

@Repository
public interface platorepository extends JpaRepository<Plato, Integer> {
    // Método correcto siguiendo la convención de nombres de Spring Data JPA
    Plato findById(@Param("id") int id);

}
