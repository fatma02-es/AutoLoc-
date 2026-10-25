package tn.esprit.fatmasouei4cc19.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.fatmasouei4cc19.domain.Agence;

@Repository
public interface IAgenceRepository extends JpaRepository<Agence, Long> {
}
