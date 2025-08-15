package com.service.payement.ListenerFactures;


import com.service.payement.ServiceFactory.TraitementFactures;
import com.service.payement.service.PublierLog;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class FacturesConsommateur {

    TraitementFactures traitementFactures;
    PublierLog publierLog;
    int i = 0 ;

    FacturesConsommateur(TraitementFactures traitementFactures, PublierLog publierLog) {
        this.traitementFactures = traitementFactures;
        this.publierLog = publierLog;
    }

    @KafkaListener(topics = "factures",groupId = "my-group")
    public void listen(String message){
        i++;
        traitementFactures.traitementFactures(message);
        publierLog.publierconsommationFlux(i);

    }



}
