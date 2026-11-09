package tn.esprit.fatmasouei4cc19.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.fatmasouei4cc19.domain.Agence;
import tn.esprit.fatmasouei4cc19.exception.ResourceNotFoundException;
import tn.esprit.fatmasouei4cc19.repository.IAgenceRepository;
import tn.esprit.fatmasouei4cc19.service.IAgenceService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AgenceServiceImpl implements IAgenceService {

    private final IAgenceRepository agenceRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Agence> findAll() {
        return agenceRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Agence findById(Long id) {
        return agenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agence introuvable avec l'identifiant " + id));
    }

    @Override
    public Agence create(Agence agence) {
        if (agence.getIdAgence() != null) {
            throw new IllegalArgumentException("L'identifiant ne doit pas être fourni pour la création");
        }
        validateAgence(agence);
        return agenceRepository.save(agence);
    }

    @Override
    public Agence update(Long id, Agence agence) {
        Agence existing = findById(id);
        validateAgence(agence);
        existing.setNom(agence.getNom());
        existing.setVille(agence.getVille());
        existing.setAdresse(agence.getAdresse());
        existing.setTelephone(agence.getTelephone());
        return agenceRepository.save(existing);
    }

    @Override
    public void deleteById(Long id) {
        if (!agenceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Agence introuvable avec l'identifiant " + id);
        }
        agenceRepository.deleteById(id);
    }

    private void validateAgence(Agence agence) {
        if (agence.getNom() == null || agence.getNom().trim().isEmpty()
                || agence.getVille() == null || agence.getVille().trim().isEmpty()
                || agence.getAdresse() == null || agence.getAdresse().trim().isEmpty()
                || agence.getTelephone() == null || agence.getTelephone().trim().isEmpty()) {
            throw new IllegalArgumentException("Les attributs obligatoires (nom, ville, adresse, téléphone) ne peuvent pas être vides");
        }
    }
}
