package kzs.com.br.sistemaindica.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import javax.persistence.*;

import static javax.persistence.FetchType.LAZY;

@Entity
@Table(name="key_word_candidature")
@AttributeOverrides({@AttributeOverride(name="id", column=@Column(name="id_key_word_candidature"))})
@Getter
@Setter
@EqualsAndHashCode(callSuper = false, of={"id"})
@ToString(of = {"id"})
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KeyWordCandidature extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @JsonIgnoreProperties({"opportunity", "candidatureHistories", "user"})
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "id_candidature", referencedColumnName = "id_candidature")
    private Candidature candidature;

    @Column(nullable = false)
    private String word;

    private Boolean found;

}
