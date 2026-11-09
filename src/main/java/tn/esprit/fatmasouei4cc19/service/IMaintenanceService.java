package tn.esprit.fatmasouei4cc19.service;

import tn.esprit.fatmasouei4cc19.domain.Maintenance;

import java.util.List;

public interface IMaintenanceService {
    List<Maintenance> findAll();
    Maintenance findById(Long id);
    Maintenance create(Maintenance maintenance);
    Maintenance update(Long id, Maintenance maintenance);
    void deleteById(Long id);
}
