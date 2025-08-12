package com.banque.bmce.repository;

import com.banque.bmce.model.Facture;
import org.springframework.data.jpa.repository.JpaRepository;


public interface  FactureRepository extends  JpaRepository<Facture, Long> {


}
