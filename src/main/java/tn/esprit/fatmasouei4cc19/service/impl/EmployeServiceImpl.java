package tn.esprit.fatmasouei4cc19.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.fatmasouei4cc19.domain.Agence;
import tn.esprit.fatmasouei4cc19.domain.Employe;
import tn.esprit.fatmasouei4cc19.exception.ResourceNotFoundException;
import tn.esprit.fatmasouei4cc19.repository.IAgenceRepository;
import tn.esprit.fatmasouei4cc19.repository.IEmployeRepository;
import tn.esprit.fatmasouei4cc19.service.IEmployeService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeServiceImpl implements IEmployeService {

    private final IEmployeRepository employeRepository;
    private final IAgenceRepository agenceRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Employe> findAll() {
        return employeRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Employe findById(Long id) {
        return employeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employe introuvable avec l'identifiant " + id));
    }

    @Override
    public Employe create(Employe employe) {
        if (employe.getIdEmploye() != null) {
            throw new IllegalArgumentException("L'identifiant ne doit pas être fourni pour la création");
        }
        validateEmploye(employe);
        attachAgence(employe);
        return employeRepository.save(employe);
    }

    @Override
    public Employe update(Long id, Employe employe) {
        Employe existing = findById(id);
        validateEmploye(employe);
        existing.setNom(employe.getNom());
        existing.setPrenom(employe.getPrenom());
        existing.setRole(employe.getRole());
        if (employe.getAgence() != null) {
            attachAgence(employe);
            existing.setAgence(employe.getAgence());
        }
        return employeRepository.save(existing);
    }

    @Override
    public void deleteById(Long id) {
        if (!employeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Employe introuvable avec l'identifiant " + id);
        }
        employeRepository.deleteById(id);
    }

    private void validateEmploye(Employe employe) {
        if (employe.getNom() == null || employe.getNom().trim().isEmpty()
                || employe.getPrenom() == null || employe.getPrenom().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom et le prénom de l'employé ne peuvent pas être vides");
        }
    }

    private void attachAgence(Employe employe) {
        if (employe.getAgence() != null && employe.getAgence().getIdAgence() != null) {
            Agence agence = agenceRepository.findById(employe.getAgence().getIdAgence())
                    .orElseThrow(() -> new ResourceNotFoundException("Agence introuvable avec l'identifiant " + employe.getAgence().getIdAgence()));
            employe.setAgence(agence);
        }
    }
}
