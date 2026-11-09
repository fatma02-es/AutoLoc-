package tn.esprit.fatmasouei4cc19.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.fatmasouei4cc19.domain.Agence;
import tn.esprit.fatmasouei4cc19.domain.Vehicule;
import tn.esprit.fatmasouei4cc19.exception.ResourceNotFoundException;
import tn.esprit.fatmasouei4cc19.repository.IAgenceRepository;
import tn.esprit.fatmasouei4cc19.repository.IVehiculeRepository;
import tn.esprit.fatmasouei4cc19.service.IVehiculeService;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class VehiculeServiceImpl implements IVehiculeService {

    private final IVehiculeRepository vehiculeRepository;
    private final IAgenceRepository agenceRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Vehicule> findAll() {
        return vehiculeRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Vehicule findById(Long id) {
        return vehiculeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Véhicule introuvable avec l'identifiant " + id));
    }

    @Override
    public Vehicule create(Vehicule vehicule) {
        if (vehicule.getIdVehicule() != null) {
            throw new IllegalArgumentException("L'identifiant ne doit pas être fourni pour la création");
        }
        validateVehicule(vehicule);
        attachAgence(vehicule);
        return vehiculeRepository.save(vehicule);
    }

    @Override
    public Vehicule update(Long id, Vehicule vehicule) {
        Vehicule existing = findById(id);
        validateVehicule(vehicule);
        existing.setImmatriculation(vehicule.getImmatriculation());
        existing.setMarque(vehicule.getMarque());
        existing.setModele(vehicule.getModele());
        existing.setCategorie(vehicule.getCategorie());
        existing.setTarifJournalier(vehicule.getTarifJournalier());
        existing.setStatut(vehicule.getStatut());
        if (vehicule.getAgence() != null) {
            attachAgence(vehicule);
            existing.setAgence(vehicule.getAgence());
        }
        return vehiculeRepository.save(existing);
    }

    @Override
    public void deleteById(Long id) {
        if (!vehiculeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Véhicule introuvable avec l'identifiant " + id);
        }
        vehiculeRepository.deleteById(id);
    }

    private void validateVehicule(Vehicule vehicule) {
        if (vehicule.getTarifJournalier() != null && vehicule.getTarifJournalier().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Le tarif journalier ne peut pas être négatif");
        }
        if (vehicule.getImmatriculation() == null || vehicule.getImmatriculation().trim().isEmpty()) {
            throw new IllegalArgumentException("L'immatriculation du véhicule ne peut pas être vide");
        }
    }

    private void attachAgence(Vehicule vehicule) {
        if (vehicule.getAgence() != null && vehicule.getAgence().getIdAgence() != null) {
            Agence agence = agenceRepository.findById(vehicule.getAgence().getIdAgence())
                    .orElseThrow(() -> new ResourceNotFoundException("Agence introuvable avec l'identifiant " + vehicule.getAgence().getIdAgence()));
            vehicule.setAgence(agence);
        }
    }
}
