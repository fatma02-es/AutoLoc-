package tn.esprit.fatmasouei4cc19.service;

import tn.esprit.fatmasouei4cc19.domain.Contrat;

import java.util.List;

public interface IContratService {
    List<Contrat> findAll();
    Contrat findById(Long id);
    Contrat create(Contrat contrat);
    Contrat update(Long id, Contrat contrat);
    void deleteById(Long id);
}
