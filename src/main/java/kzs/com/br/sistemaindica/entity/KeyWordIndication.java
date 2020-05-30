package kzs.com.br.sistemaindica.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import javax.persistence.*;

import static javax.persistence.FetchType.LAZY;

@Entity
@Table(name="key_word_indication")
@AttributeOverrides({@AttributeOverride(name="id", column=@Column(name="id_key_word_indication"))})
@Getter
@Setter
@EqualsAndHashCode(callSuper = false, of={"id"})
@ToString(of = {"id"})
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KeyWordIndication extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @JsonIgnoreProperties({"opportunity", "indicationHistories", "user"})
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "id_indication", referencedColumnName = "id_indication")
    private Indication indication;

    @Column(nullable = false)
    private String word;

    private Boolean found;

}
