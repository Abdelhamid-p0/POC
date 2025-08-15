package com.service.payement.repository;

import com.service.payement.model.Acte;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ActeRepository extends JpaRepository<Acte, Long> {
    Acte findByCodeActe(String codeActe);
}
