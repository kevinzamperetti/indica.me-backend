package kzs.com.br.sistemaindica.service.impl;

import kzs.com.br.sistemaindica.entity.*;
import kzs.com.br.sistemaindica.entity.dto.CandidatureQuantityDto;
import kzs.com.br.sistemaindica.entity.dto.CandidatureStatusDto;
import kzs.com.br.sistemaindica.enums.CandidatureStatus;
import kzs.com.br.sistemaindica.exception.*;
import kzs.com.br.sistemaindica.payload.UploadFileResponse;
import kzs.com.br.sistemaindica.repository.*;
import kzs.com.br.sistemaindica.service.CandidatureHistoryService;
import kzs.com.br.sistemaindica.service.CandidatureService;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
public class CandidatureServiceImpl implements CandidatureService {

    private final CandidatureRepository repository;

    private final OpportunityRepository opportunityRepository;

    private final UserRepository userRepository;

    private final KeyWordRepository keyWordRepository;

    private final KeyWordCandidatureRepository keyWordCandidatureRepository;

    private final CandidatureHistoryService candidatureHistoryService;

    private final FileStorageServiceImpl fileStorageService;

    @Override
    public List<Candidature> findCandidatureByStatus(CandidatureStatus status) {
        return repository.findCandidatureByStatus(status);
    }

    @Override
    public Candidature findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new CandidatureIdNotFoundException("Candidatura não encontrado"));
    }

    @Override
    public List<Candidature> findByUser(Long id) {
        userRepository.findById(id)
                .orElseThrow(() -> new UserIdNotFoundException("Usuário não encontrado"));
        return repository.findByUser(id);
    }

    @Override
    @Transactional
    public Candidature save(Candidature candidature) throws IOException {
        if (nonNull(candidature.getId())) {
            throw new CandidatureIdMustNotBeProvidedException("Id da Candidatura não deve ser informado");
        }
        verifyFields(candidature);
        setOpportunity(candidature);
        setUser(candidature);

        String filePath = fileStorageService.getResourceByFileName(candidature.getFileNameAttachment()).getFile().getPath();
        byte[] inFileBytes = Files.readAllBytes(Paths.get(filePath));
        byte[] encoded = java.util.Base64.getEncoder().encode(inFileBytes);
        candidature.setAttachment(encoded);

        candidature.setCreationDate(LocalDate.now());
        checkIfTheCandidatureAlreadyExists(candidature);
        setKeyWordCandidature(candidature);

        Candidature candidatureSaved = repository.save(candidature);
        setCandidatureHistory(candidatureSaved);
        findKeyWordInCandidature(candidature.getId());
        return candidatureSaved;
    }

    @Override
    public UploadFileResponse uploadAttachment(@RequestParam("file") MultipartFile file) {
        String fileName = fileStorageService.storeFile(file);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/file/candidature/downloadFile/")
                .path(fileName)
                .toUriString();

        return new UploadFileResponse(fileName, fileDownloadUri,
                file.getContentType(), file.getSize());
    }

    private void setOpportunity(Candidature candidature) {
        Opportunity opportunity = opportunityRepository.findById(candidature.getOpportunity().getId())
                .orElseThrow(() -> new CandidatureIdNotFoundException("Candidatura não encontrada"));
        candidature.setOpportunity(opportunity);
    }

    private void setUser(Candidature candidature) {
        User user = userRepository.findById(candidature.getUser().getId())
                .orElseThrow(() -> new CandidatureIdNotFoundException("Usuário não encontrado"));
        candidature.setUser(user);
    }

    private void setKeyWordCandidature(Candidature candidature) {
        List<KeyWord> keyWords = keyWordRepository.findByOpportunityId(candidature.getOpportunity().getId());
        List<KeyWordCandidature> list = new ArrayList<>();
        keyWords.forEach(keyWord -> {
            KeyWordCandidature keyWordCandidature = KeyWordCandidature.builder()
                    .candidature(candidature)
                    .word(keyWord.getWord())
                    .found(false)
                    .build();
            keyWordCandidatureRepository.save(keyWordCandidature);
            list.add(keyWordCandidature);
        });
        candidature.setKeyWordCandidaturies(list);
    }

    private void checkIfTheCandidatureAlreadyExists(Candidature candidature) {
        if (repository.findByCandidatureEmailOrIndicationNameOrIndicationPhoneNumber(
                candidature.getCandidateEmail(), candidature.getCandidateName(),
                candidature.getCandidatePhoneNumber()).isPresent()) {
            throw new CandidatureYouAreAlreadyAppliedForThisOpportunityException("Você já está participando de uma candidatura");
        }
    }

    @Override
    public Candidature edit(Candidature candidature) {
        if (isNull(candidature.getId())) {
            throw new OpportunityIdNotProvidedException("Id da Oportunidade não informado");
        }
        findById(candidature.getId());
        verifyFields(candidature);
        return repository.save(candidature);
    }

    @Override
    @Transactional
    public Candidature updateStatus(CandidatureStatusDto candidatureStatusDto) {
        if (isNull(candidatureStatusDto.getStatus())) {
            throw new CandidatureStatusNotProvidedException("Situação para alteração da Candidatura não informado");
        }
        Candidature candidature = repository.findById(candidatureStatusDto.getId())
                .orElseThrow(() -> new OpportunityIdNotFoundException("Oportunidade não encontrada"));

        candidature.setStatus(candidatureStatusDto.getStatus());

        Candidature candidatureSaved = repository.save(candidature);
        setCandidatureHistory(candidatureSaved);
        return candidatureSaved;
    }

    private void setCandidatureHistory(Candidature candidature) {
        candidatureHistoryService.save(candidature);
    }


    @Override
    public void delete(Long id) {
        Candidature candidature = repository.findById(id)
                .orElseThrow(() -> new OpportunityIdNotFoundException("Oportunidade não encontrada"));
        if(!CandidatureStatus.NEW.equals(candidature.getStatus())) {
            throw new CandidatureIsInProgressAndCannotBeDeletedException("Candidatura está em andamento e não pode ser excluída");
        } else {
            repository.delete(candidature);
        }
    }

    private void verifyFields(Candidature candidature) {
        if (isNull(candidature.getUser())) {
            throw new CandidatureUserNotProvidedException("Usuário da Candidatura não informado");
        }
        if (isNull(candidature.getCandidateDocumentNumber())) {
            throw new CandidatureCandidateDocumentNumberNotProvidedException("CPF do Candidato não informado");
        }
        if (isNull(candidature.getOpportunity())) {
            throw new CandidatureOpportunityNotProvidedException("Oportunidade da Candidatura não informada");
        }
        if (isNull(candidature.getStatus())) {
            throw new CandidatureStatusNotProvidedException("Situação da Candidatura não informada");
        }
        if (isNull(candidature.getCandidateName())) {
            throw new CandidatureCandidateNameNotProvidedException("Nome do Candidato não informado");
        }
        if (isNull(candidature.getCandidatePhoneNumber())) {
            throw new CandidatureCandidatePhoneNumberNotProvidedException("Telefone do Candidato não informado");
        }
        if (isNull(candidature.getCandidateEmail())) {
            throw new CandidatureCandidateEmailNotProvidedException("E-mail do Candidato não informado");
        }
    }

    @Override
    public CandidatureQuantityDto totalCandidaturiesByStatus() {
        return CandidatureQuantityDto.builder()
                .qtyCandidaturiesNew(repository.countCandidatureStatusNew())
                .qtyCandidaturiesInProgress(repository.countCandidatureStatusInProgress())
                .qtyCandidaturiesHired(repository.countCandidatureStatusHired())
                .qtyCandidaturiesDiscarded(repository.countCandidatureStatusDiscarded())
                .build();
    }

    @Override
    public void findKeyWordInCandidature(Long id) throws IOException {
        Candidature candidature = findById(id);

        File file = null;
        Resource resource = fileStorageService.loadFileAsResource(candidature.getFileNameAttachment());

        if (nonNull(resource)) {
            file = resource.getFile();
        }

        try (PDDocument document = PDDocument.load( file )) {
            if (!document.isEncrypted()) {
                PDFTextStripperByArea stripper = new PDFTextStripperByArea();
                stripper.setSortByPosition(true);

                PDFTextStripper tStripper = new PDFTextStripper();

                String pdfFileInText = tStripper.getText(document);

                String lines[] = pdfFileInText.split("\\r?\\n");
                List<KeyWordCandidature> words = candidature.getKeyWordCandidaturies();

                words.forEach( word -> {
                    for (String line : lines) {
                        if (line.toUpperCase().contains(word.getWord().toUpperCase())) {
                            word.setFound(true);
                            keyWordCandidatureRepository.save(word);
                            break;
                        }
                    }
                });
            }
            updateStatusAfterFindKeyWord(candidature);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Candidature updateStatusAfterFindKeyWord(Candidature candidature) {
        Integer automaticEvaluationQuantity = candidature.getOpportunity().getAutomaticEvaluationQuantity();
        Long qtykeyWordTrue = candidature.getKeyWordCandidaturies().stream().filter(w -> Boolean.TRUE.equals(w.getFound())).count();

        if (qtykeyWordTrue >= automaticEvaluationQuantity) {
            candidature.setStatus(CandidatureStatus.PRE_EVALUATION_OK);
        } else {
            candidature.setStatus(CandidatureStatus.PRE_EVALUATION_NOK);
        }

        Candidature candidatureSaved = repository.save(candidature);
        setCandidatureHistory(candidatureSaved);

        return candidatureSaved;
    }
}