package kzs.com.br.sistemaindica.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import kzs.com.br.sistemaindica.enums.CandidatureStatus;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static javax.persistence.FetchType.LAZY;

@Entity
@Table(name="candidature")
@AttributeOverrides({@AttributeOverride(name="id", column=@Column(name="id_candidature"))})
@Getter
@Setter
@EqualsAndHashCode(callSuper = false, of={"name"})
@ToString(of = {"id"})
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Candidature extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @JsonIgnoreProperties({"indications", "indicationWinners"})
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "id_user", referencedColumnName = "id_user")
    private User user;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "id_opportunity", referencedColumnName = "id_opportunity")
    private Opportunity opportunity;

    @JsonIgnore
    @OneToMany(mappedBy = "candidature", fetch = LAZY)
    private List<KeyWordCandidature> keyWordCandidaturies;

    @JsonIgnore
    @OneToMany(mappedBy = "candidature", fetch = LAZY)
    private Set<CandidatureHistory> candidatureHistories;

    @JsonIgnore
    private byte[] attachment;

    @Column(name = "file_name_attachment")
    private String fileNameAttachment;

    @Column(name = "file_downloadUri_attachment")
    private String fileDownloadUriAttachment;

    @Column(name = "file_type_attachment")
    private String fileTypeAttachment;

    @Column(name = "creation_date")
    private LocalDate creationDate;

    @Column(name = "candidate_name", nullable = false)
    private String candidateName;

    @Column(name = "candidate_phone_number", nullable = false)
    private String candidatePhoneNumber;

    @Column(name = "candidate_email", nullable = false)
    private String candidateEmail;

    @Column(name = "candidate_document_number", nullable = false)
    private String candidateDocumentNumber;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CandidatureStatus status;

}
