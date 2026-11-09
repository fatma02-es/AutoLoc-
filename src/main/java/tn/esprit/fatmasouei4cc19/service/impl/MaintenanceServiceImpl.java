package tn.esprit.fatmasouei4cc19.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.fatmasouei4cc19.domain.Maintenance;
import tn.esprit.fatmasouei4cc19.domain.Vehicule;
import tn.esprit.fatmasouei4cc19.exception.ResourceNotFoundException;
import tn.esprit.fatmasouei4cc19.repository.IMaintenanceRepository;
import tn.esprit.fatmasouei4cc19.repository.IVehiculeRepository;
import tn.esprit.fatmasouei4cc19.service.IMaintenanceService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MaintenanceServiceImpl implements IMaintenanceService {

    private final IMaintenanceRepository maintenanceRepository;
    private final IVehiculeRepository vehiculeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Maintenance> findAll() {
        return maintenanceRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Maintenance findById(Long id) {
        return maintenanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance introuvable avec l'identifiant " + id));
    }

    @Override
    public Maintenance create(Maintenance maintenance) {
        if (maintenance.getIdMaintenance() != null) {
            throw new IllegalArgumentException("L'identifiant ne doit pas être fourni pour la création");
        }
        validateMaintenance(maintenance);
        attachVehicule(maintenance);
        return maintenanceRepository.save(maintenance);
    }

    @Override
    public Maintenance update(Long id, Maintenance maintenance) {
        Maintenance existing = findById(id);
        validateMaintenance(maintenance);
        existing.setDateDebut(maintenance.getDateDebut());
        existing.setDateFin(maintenance.getDateFin());
        existing.setDescription(maintenance.getDescription());
        if (maintenance.getVehicule() != null) {
            attachVehicule(maintenance);
            existing.setVehicule(maintenance.getVehicule());
        }
        return maintenanceRepository.save(existing);
    }

    @Override
    public void deleteById(Long id) {
        if (!maintenanceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Maintenance introuvable avec l'identifiant " + id);
        }
        maintenanceRepository.deleteById(id);
    }

    private void validateMaintenance(Maintenance maintenance) {
        if (maintenance.getDateDebut() != null && maintenance.getDateFin() != null
                && maintenance.getDateFin().isBefore(maintenance.getDateDebut())) {
            throw new IllegalArgumentException("La date de fin ne peut pas être antérieure à la date de début");
        }
    }

    private void attachVehicule(Maintenance maintenance) {
        if (maintenance.getVehicule() != null && maintenance.getVehicule().getIdVehicule() != null) {
            Vehicule vehicule = vehiculeRepository.findById(maintenance.getVehicule().getIdVehicule())
                    .orElseThrow(() -> new ResourceNotFoundException("Véhicule introuvable avec l'identifiant " + maintenance.getVehicule().getIdVehicule()));
            maintenance.setVehicule(vehicule);
        }
    }
}
