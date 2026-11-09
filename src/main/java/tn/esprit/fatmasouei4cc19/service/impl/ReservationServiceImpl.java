package tn.esprit.fatmasouei4cc19.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.fatmasouei4cc19.domain.Client;
import tn.esprit.fatmasouei4cc19.domain.Reservation;
import tn.esprit.fatmasouei4cc19.domain.Vehicule;
import tn.esprit.fatmasouei4cc19.exception.ResourceNotFoundException;
import tn.esprit.fatmasouei4cc19.repository.IClientRepository;
import tn.esprit.fatmasouei4cc19.repository.IReservationRepository;
import tn.esprit.fatmasouei4cc19.repository.IVehiculeRepository;
import tn.esprit.fatmasouei4cc19.service.IReservationService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationServiceImpl implements IReservationService {

    private final IReservationRepository reservationRepository;
    private final IClientRepository clientRepository;
    private final IVehiculeRepository vehiculeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Reservation findById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation introuvable avec l'identifiant " + id));
    }

    @Override
    public Reservation create(Reservation reservation) {
        if (reservation.getIdReservation() != null) {
            throw new IllegalArgumentException("L'identifiant ne doit pas être fourni pour la création");
        }
        validateReservation(reservation);
        attachRelations(reservation);
        return reservationRepository.save(reservation);
    }

    @Override
    public Reservation update(Long id, Reservation reservation) {
        Reservation existing = findById(id);
        validateReservation(reservation);
        existing.setDateDebut(reservation.getDateDebut());
        existing.setDateFin(reservation.getDateFin());
        existing.setStatut(reservation.getStatut());
        if (reservation.getClient() != null) {
            attachRelations(reservation);
            existing.setClient(reservation.getClient());
        }
        if (reservation.getVehicule() != null) {
            attachRelations(reservation);
            existing.setVehicule(reservation.getVehicule());
        }
        return reservationRepository.save(existing);
    }

    @Override
    public void deleteById(Long id) {
        if (!reservationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Réservation introuvable avec l'identifiant " + id);
        }
        reservationRepository.deleteById(id);
    }

    private void validateReservation(Reservation reservation) {
        if (reservation.getDateDebut() != null && reservation.getDateFin() != null
                && reservation.getDateFin().isBefore(reservation.getDateDebut())) {
            throw new IllegalArgumentException("La date de fin ne peut pas être antérieure à la date de début");
        }
    }

    private void attachRelations(Reservation reservation) {
        if (reservation.getClient() != null && reservation.getClient().getIdClient() != null) {
            Client client = clientRepository.findById(reservation.getClient().getIdClient())
                    .orElseThrow(() -> new ResourceNotFoundException("Client introuvable avec l'identifiant " + reservation.getClient().getIdClient()));
            reservation.setClient(client);
        }
        if (reservation.getVehicule() != null && reservation.getVehicule().getIdVehicule() != null) {
            Vehicule vehicule = vehiculeRepository.findById(reservation.getVehicule().getIdVehicule())
                    .orElseThrow(() -> new ResourceNotFoundException("Véhicule introuvable avec l'identifiant " + reservation.getVehicule().getIdVehicule()));
            reservation.setVehicule(vehicule);
        }
    }
}
