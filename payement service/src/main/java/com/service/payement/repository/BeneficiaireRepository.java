package com.service.payement.repository;

import com.service.payement.model.Beneficiaire;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BeneficiaireRepository extends JpaRepository<Beneficiaire,Long> {
    Beneficiaire findByNom(String benificiare);
}
