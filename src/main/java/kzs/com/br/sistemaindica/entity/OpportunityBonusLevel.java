package kzs.com.br.sistemaindica.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.util.Set;

import static javax.persistence.FetchType.LAZY;

@Entity
@Table(name="opportunity_bonus_level")
@AttributeOverrides({@AttributeOverride(name="id", column=@Column(name="id_opportunity_bonus_level"))})
@Getter
@Setter
@EqualsAndHashCode(callSuper = false, of={"name"})
@ToString(of = {"id"})
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OpportunityBonusLevel  extends BaseEntity {

    private String name;

    @Column(precision = 10, scale = 2)
    private Float value;

    private Boolean enabled;
//
//    @OneToOne(mappedBy = "bonusLevel", fetch = FetchType.LAZY)
//    private Opportunity opportunity;

    @JsonIgnore
    @OneToMany(mappedBy = "bonusLevel", fetch = LAZY)
    private Set<Opportunity> opportunities;

}
