package tn.esprit.fatmasouei4cc19.service;

import tn.esprit.fatmasouei4cc19.domain.Equipement;

import java.util.List;

public interface IEquipementService {
    List<Equipement> findAll();
    Equipement findById(Long id);
    Equipement create(Equipement equipement);
    Equipement update(Long id, Equipement equipement);
    void deleteById(Long id);
}
