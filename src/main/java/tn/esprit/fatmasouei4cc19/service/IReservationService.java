package tn.esprit.fatmasouei4cc19.service;

import tn.esprit.fatmasouei4cc19.domain.Reservation;

import java.util.List;

public interface IReservationService {
    List<Reservation> findAll();
    Reservation findById(Long id);
    Reservation create(Reservation reservation);
    Reservation update(Long id, Reservation reservation);
    void deleteById(Long id);
}
