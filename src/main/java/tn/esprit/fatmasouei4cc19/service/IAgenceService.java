package tn.esprit.fatmasouei4cc19.service;

import tn.esprit.fatmasouei4cc19.domain.Agence;

import java.util.List;

public interface IAgenceService {
    List<Agence> findAll();
    Agence findById(Long id);
    Agence create(Agence agence);
    Agence update(Long id, Agence agence);
    void deleteById(Long id);
}
