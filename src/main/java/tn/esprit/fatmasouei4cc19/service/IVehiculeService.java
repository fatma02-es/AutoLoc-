package tn.esprit.fatmasouei4cc19.service;

import tn.esprit.fatmasouei4cc19.domain.Vehicule;

import java.util.List;

public interface IVehiculeService {
    List<Vehicule> findAll();
    Vehicule findById(Long id);
    Vehicule create(Vehicule vehicule);
    Vehicule update(Long id, Vehicule vehicule);
    void deleteById(Long id);
}
