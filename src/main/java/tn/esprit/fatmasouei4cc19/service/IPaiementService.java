package tn.esprit.fatmasouei4cc19.service;

import tn.esprit.fatmasouei4cc19.domain.Paiement;

import java.util.List;

public interface IPaiementService {
    List<Paiement> findAll();
    Paiement findById(Long id);
}
