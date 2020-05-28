package kzs.com.br.sistemaindica.repository;

import kzs.com.br.sistemaindica.entity.KeyWordCandidature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KeyWordCandidatureRepository extends JpaRepository<KeyWordCandidature, Long> {

    @Query("SELECT k " +
            " FROM KeyWordCandidature k " +
            "WHERE k.candidature.id = :id " +
            "ORDER BY k.word ASC")
    List<KeyWordCandidature> findByCandidatureId(@Param("id") Long id);
}
