package tn.esprit.fatmasouei4cc19.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.fatmasouei4cc19.domain.Reservation;

@Repository
public interface IReservationRepository extends JpaRepository<Reservation, Long> {
}
