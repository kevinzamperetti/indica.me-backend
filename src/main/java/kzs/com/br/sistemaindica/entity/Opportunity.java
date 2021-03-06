package kzs.com.br.sistemaindica.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import kzs.com.br.sistemaindica.enums.OpportunityExperienceLevel;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Set;

import static javax.persistence.FetchType.LAZY;

@Entity
@Table(name="opportunity")
@AttributeOverrides({@AttributeOverride(name="id", column=@Column(name="id_opportunity"))})
@Getter
@Setter
@EqualsAndHashCode(callSuper = false, of={"name"})
@ToString(of = {"id"})
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Opportunity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, columnDefinition = "varchar(2000)")
    private String description;

//    @JsonIgnore
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "id_campaign", referencedColumnName = "id_campaign")
    private Campaign campaign;

    @JsonIgnore
    @OneToMany(mappedBy = "opportunity", fetch = LAZY)
    private Set<Indication> indications;

    @JsonIgnore
    @OneToMany(mappedBy = "opportunity", fetch = LAZY)
    private Set<KeyWord> keyWords;

//    @OneToOne
//    @JoinColumn(name = "id_opportunity_bonus_level", referencedColumnName = "id_opportunity_bonus_level")
//    private OpportunityBonusLevel bonusLevel;

//    @JsonIgnore
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "id_opportunity_bonus_level", referencedColumnName = "id_opportunity_bonus_level")
    private OpportunityBonusLevel bonusLevel;

    @Column(name = "experience_level")
    @Enumerated(EnumType.STRING)
    private OpportunityExperienceLevel experienceLevel;

    @Column(name = "creation_date")
    private LocalDate creationDate;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

//    @Column(name = "key_words")
//    private List<String> keyWords;

    @Column(name = "automatic_evaluation_quantity")
    private Integer automaticEvaluationQuantity;

    private Boolean enabled;

}
