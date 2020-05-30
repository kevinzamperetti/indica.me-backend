package kzs.com.br.sistemaindica.repository;

import kzs.com.br.sistemaindica.entity.KeyWordIndication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KeyWordIndicationRepository extends JpaRepository<KeyWordIndication, Long> {

    @Query("SELECT k " +
            " FROM KeyWordIndication k " +
            "WHERE k.indication.id = :id " +
            "ORDER BY k.word ASC")
    List<KeyWordIndication> findByIndicationId(@Param("id") Long id);
}
