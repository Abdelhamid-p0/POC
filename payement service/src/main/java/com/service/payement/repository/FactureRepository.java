package com.service.payement.repository;

import com.service.payement.model.Facture;
import org.springframework.data.jpa.repository.JpaRepository;


public interface  FactureRepository extends  JpaRepository<Facture, Long> {

}
