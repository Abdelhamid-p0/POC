//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.PestationSante.prestationSante.controller;

import com.PestationSante.prestationSante.model.Facture;
import com.PestationSante.prestationSante.service.PubFluxFactures;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class FormController {

    private final PubFluxFactures fluxFactures;
    List<String> listeActes = List.of("oc", "ecg (SqlException)", "rx", "end", "biop");

    public FormController(PubFluxFactures fluxFactures) {
        this.fluxFactures = fluxFactures;
    }

    @GetMapping({"/facture"})
    public String afficherFormulaire(Model model) {
        model.addAttribute("facture", new Facture());
        model.addAttribute("listeActes", this.listeActes);
        model.addAttribute("isSimulerErreur", false);
        return "formulaire";
    }

    @PostMapping({"/facture"})
    public String traiterPaiement(@ModelAttribute Facture facture, @RequestParam(name = "isSimulerErreur", required = false) Boolean isSimulerErreur, Model model) {
        model.addAttribute("listeActes", facture.getActes());
        try {
            // Validation inchangée
            if (facture.getOrganisation() == null || facture.getOrganisation().trim().isEmpty()) {
                throw new IllegalArgumentException("Le nom de l'organisation est obligatoire");
            }
            if (facture.getBenificiare() == null || facture.getBenificiare().trim().isEmpty()) {
                throw new IllegalArgumentException("Le nom du bénificiare est obligatoire");
            }
            if (facture.getActes() == null || facture.getActes().isEmpty()) {
                throw new IllegalArgumentException("Le champs d'actes est obligatoire");
            }
            if (facture.getProf_sante() == null || facture.getProf_sante().isEmpty()) {
                throw new IllegalArgumentException("Le champs PS est obligatoire");
            }
            if (facture.getDate() == null) {
                throw new IllegalArgumentException("Le champs date est obligatoire");
            }

            ObjectNode response;
            if (Boolean.TRUE.equals(isSimulerErreur)) {
                // Appel méthode avec factures erronées
                response = fluxFactures.simulerNAppelsSequencielsAvecErreurs(100, facture);
            } else {
                // Appel méthode normale
                response = fluxFactures.simulerNAppelsSequenciels(100, facture);
            }

            if (response != null && Objects.equals(response.get("status").asText(), "success")) {
                model.addAttribute("success", true);
                model.addAttribute("message", "Envoie des factures effectué avec succès !");
                model.addAttribute("nbrFactures", response.get("nbrsFactures").asText());
                model.addAttribute("nbrFacturesErr", response.get("nbrsFacturesErr").asText());
            } else {
                model.addAttribute("success", false);
                model.addAttribute("message", "Error !! Ressayer.");
            }
        } catch (Exception e) {
            model.addAttribute("success", false);
            model.addAttribute("message", "Erreur : " + e.getMessage());
        }

        return "formulaire";
    }

}
