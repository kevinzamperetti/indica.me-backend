package kzs.com.br.sistemaindica.repository;

import kzs.com.br.sistemaindica.entity.OpportunityBonusLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface OpportunityBonusLevelRepository extends JpaRepository<OpportunityBonusLevel, Long> {

    @Query("SELECT o " +
            " FROM OpportunityBonusLevel o " +
            " LEFT JOIN FETCH o.opportunities op " +
            "WHERE (:enabled IS NULL OR o.enabled = :enabled) " +
            "ORDER BY o.enabled DESC, o.value")
    Set<OpportunityBonusLevel> findOpportunityBonusLevelByEnabled(@Param("enabled") Boolean enabled);

    @Query("SELECT o " +
            " FROM OpportunityBonusLevel o " +
            " LEFT JOIN FETCH o.opportunities op " +
            "WHERE o.id = :id")
    Optional<OpportunityBonusLevel> findById(@Param("id") Long id);

}
