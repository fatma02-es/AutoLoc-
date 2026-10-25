package tn.esprit.fatmasouei4cc19.service.Impris;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.fatmasouei4cc19.domain.Vehicule;
import tn.esprit.fatmasouei4cc19.repository.IVehiculeRepository;
import tn.esprit.fatmasouei4cc19.service.IVehiculeService;

@Service
@RequiredArgsConstructor
public class VehiculeService implements IVehiculeService {
    private final IVehiculeRepository iVehiculeRepository;

    @Override
    public Vehicule createVehicule(Vehicule vehicule) {
        return iVehiculeRepository.save(vehicule);
    }

    @Override
    public Vehicule getVehiculeById(Long id) {
        return iVehiculeRepository.findById(id).orElse(null);
    }

    @Override
    public Vehicule updateVehicule(Vehicule vehicule) {
        return iVehiculeRepository.save(vehicule);
    }

    @Override
    public void deleteVehiculeById(Long id) {
        iVehiculeRepository.deleteById(id);
    }
}
