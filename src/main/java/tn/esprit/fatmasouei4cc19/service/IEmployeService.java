package tn.esprit.fatmasouei4cc19.service;

import tn.esprit.fatmasouei4cc19.domain.Employe;

import java.util.List;

public interface IEmployeService {
    List<Employe> findAll();
    Employe findById(Long id);
    Employe create(Employe employe);
    Employe update(Long id, Employe employe);
    void deleteById(Long id);
}
