package kzs.com.br.sistemaindica.service.impl;

import kzs.com.br.sistemaindica.entity.*;
import kzs.com.br.sistemaindica.entity.dto.IndicationQuantityDto;
import kzs.com.br.sistemaindica.entity.dto.IndicationStatusDto;
import kzs.com.br.sistemaindica.entity.dto.IndicationUserQuantityDto;
import kzs.com.br.sistemaindica.enums.IndicationStatus;
import kzs.com.br.sistemaindica.exception.*;
import kzs.com.br.sistemaindica.payload.UploadFileResponse;
import kzs.com.br.sistemaindica.repository.*;
import kzs.com.br.sistemaindica.service.EmailService;
import kzs.com.br.sistemaindica.service.IndicationHistoryService;
import kzs.com.br.sistemaindica.service.IndicationService;
import kzs.com.br.sistemaindica.service.IndicationWinnerService;
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
public class IndicationServiceImpl implements IndicationService {

    private final IndicationRepository repository;

    private final OpportunityRepository opportunityRepository;

    private final UserRepository userRepository;

    private final KeyWordRepository keyWordRepository;

    private final KeyWordIndicationRepository keyWordIndicationRepository;

    private final IndicationHistoryService indicationHistoryService;

    private final IndicationWinnerService indicationWinnerService;

    private final FileStorageServiceImpl fileStorageService;

    private final EmailService emailService;

    @Override
    public List<Indication> findIndicationByStatus(IndicationStatus status) {
        return repository.findIndicationByStatus(status);
    }

    @Override
    public Indication findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IndicationIdNotFoundException("Indicação não encontrada"));
    }

    @Override
    public List<Indication> findByUser(Long id) {
        userRepository.findById(id)
                .orElseThrow(() -> new UserIdNotFoundException("Usuário não encontrado"));
        return repository.findByUser(id);
    }

    @Override
    @Transactional
    public Indication save(Indication indication) throws IOException {
        if (nonNull(indication.getId())) {
            throw new IndicationIdMustNotBeProvidedException("Id da Indicação não deve ser informado.");
        }
        verifyFields(indication);
        setOpportunity(indication);
        setUser(indication);

        String filePath = fileStorageService.getResourceByFileName(indication.getFileNameAttachment()).getFile().getPath();
        byte[] inFileBytes = Files.readAllBytes(Paths.get(filePath));
        byte[] encoded = java.util.Base64.getEncoder().encode(inFileBytes);
        indication.setAttachment(encoded);

        validateUserAndIndication(indication);
        indication.setCreationDate(LocalDate.now());
        checkIfTheIndicationAlreadyExists(indication);
        setKeyWordIndication(indication);

        Indication indicationSaved = repository.save(indication);
        setIndicationHistory(indicationSaved);
        findKeyWordInIndication(indication.getId());
        return indicationSaved;
    }

    @Override
    public UploadFileResponse uploadAttachment(@RequestParam("file") MultipartFile file) {
        String fileName = fileStorageService.storeFile(file);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/file/indication/downloadFile/")
                .path(fileName)
                .toUriString();

        return new UploadFileResponse(fileName, fileDownloadUri,
                file.getContentType(), file.getSize());
    }

    private void setOpportunity(Indication indication) {
        Opportunity opportunity = opportunityRepository.findById(indication.getOpportunity().getId())
                .orElseThrow(() -> new IndicationIdNotFoundException("Oportunidade não encontrada"));
        indication.setOpportunity(opportunity);
    }

    private void setUser(Indication indication) {
        User user = userRepository.findById(indication.getUser().getId())
                .orElseThrow(() -> new IndicationIdNotFoundException("Usuário não encontrado"));
        indication.setUser(user);
    }

    private void setKeyWordIndication(Indication indication) {
        List<KeyWord> keyWords = keyWordRepository.findByOpportunityId(indication.getOpportunity().getId());
        List<KeyWordIndication> list = new ArrayList<>();
        keyWords.forEach(keyWord -> {
            KeyWordIndication keyWordIndication = KeyWordIndication.builder()
                    .indication(indication)
                    .word(keyWord.getWord())
                    .found(false)
                    .build();
            keyWordIndicationRepository.save(keyWordIndication);
            list.add(keyWordIndication);
        });
        indication.setKeyWordIndications(list);
    }

    private void checkIfTheIndicationAlreadyExists(Indication indication) {
        if (repository.findByIndicationEmailOrIndicationNameOrIndicationPhoneNumber(
                indication.getIndicationEmail(), indication.getIndicationName(),
                indication.getIndicationPhoneNumber()).isPresent()) {
            throw new IndicationThisPersonAlreadyHasIndicationException("Esta pessoa já possui uma indicação");
        }
    }

    @Override
    public Indication edit(Indication indication) {
        if (isNull(indication.getId())) {
            throw new OpportunityIdNotProvidedException("Id da Oportunidade não informado");
        }
        findById(indication.getId());
        verifyFields(indication);
        return repository.save(indication);
    }

    @Override
    @Transactional
    public Indication updateStatus(IndicationStatusDto indicationStatusDto) {
        if (isNull(indicationStatusDto.getStatus())) {
            throw new IndicationStatusNotProvidedException("Situação para alteração da Indicação não informado");
        }
        Indication indication = repository.findById(indicationStatusDto.getId())
                .orElseThrow(() -> new OpportunityIdNotFoundException("Oportunidade não encontrada"));

        IndicationStatus previousStatus = indication.getStatus();

        indication.setStatus(indicationStatusDto.getStatus());
        Indication indicationSaved = repository.save(indication);

        setIndicationHistory(indicationSaved);

        if (IndicationStatus.HIRED.equals(indicationStatusDto.getStatus())) {
//            setIndicationWinner(indicationSaved); //se for salvar tem que tratar para se já existir registro excluir, se não da erro de id com mais de um registro
            if (indication.getOpportunity().getBonusLevel().getValue() > 0) {
                emailService.sendEmailWhenIndicationHired(indication.getUser().getEmail(), indication.getIndicationName());
            }

            indication.setStatus(IndicationStatus.SENDING_BONUS);
            indicationSaved = repository.save(indication);
            setIndicationHistory(indication);
        }
        if (IndicationStatus.BONUS_SENT.equals(indicationStatusDto.getStatus()) &&
                IndicationStatus.SENDING_BONUS.equals(previousStatus) &&
                indication.getOpportunity().getBonusLevel().getValue() > 0) {
            emailService.sendEmailWhenIndicationBonusSent(indication.getUser().getEmail(), indication.getIndicationName());
        }
        return indicationSaved;
    }

    private void setIndicationHistory(Indication indication) {
        indicationHistoryService.save(indication);
    }

    private void setIndicationWinner(Indication indication) {
        indicationWinnerService.save(indication);
    }

    @Override
    public void delete(Long id) {
        Indication indication = repository.findById(id)
                .orElseThrow(() -> new OpportunityIdNotFoundException("Oportunidade não encontrada"));
        if(!IndicationStatus.NEW.equals(indication.getStatus())) {
            throw new IndicationIsInProgressAndCannotBeDeletedException("Indicação está em andamento e não pode ser excluída");
        } else {
            repository.delete(indication);
        }
    }

    private void verifyFields(Indication indication) {
        if (isNull(indication.getUser())) {
            throw new IndicationUserNotProvidedException("Usuário da Indicação não informado");
        }
        if (isNull(indication.getUserDocumentNumber())) {
            throw new IndicationUserDocumentNumberNotProvidedException("CPF do Usuário não informado");
        }
        if (isNull(indication.getOpportunity())) {
            throw new IndicationOpportunityNotProvidedException("Oportunidade da Indicação não informada");
        }
        if (isNull(indication.getStatus())) {
            throw new IndicationStatusNotProvidedException("Situação da Indicação não informada");
        }
        if (isNull(indication.getIndicationName())) {
            throw new IndicationNameNotProvidedException("Nome do Indicado não informado");
        }
        if (isNull(indication.getIndicationPhoneNumber())) {
            throw new IndicationPhoneNumberNotProvidedException("Telefone do Indicado não informado");
        }
        if (isNull(indication.getIndicationEmail())) {
            throw new IndicationEmailNotProvidedException("E-mail do Indicado não informado");
        }
    }

    private void validateUserAndIndication(Indication indication) {
        if (indication.getUser().getName().equals(indication.getIndicationName())) {
            throw new IndicationCannotBePerformedForYouException("Não é possível realizar uma indicação para você mesmo");
        }
    }

    @Override
    public IndicationQuantityDto totalIndicationsByStatus() {
        return IndicationQuantityDto.builder()
                .qtyIndicationsNew(repository.countIndicationStatusNew())
                .qtyIndicationsInProgress(repository.countIndicationStatusInProgress())
                .qtyIndicationsHired(repository.countIndicationStatusHired())
                .qtyIndicationsDiscarded(repository.countIndicationStatusDiscarded())
                .build();
    }

    public IndicationUserQuantityDto totalIndicationsByUser(String userEmail) {
        return IndicationUserQuantityDto.builder()
                .qtyIndicationsInProgressByUser(repository.countIndicationsInProgressByUser(userEmail))
                .qtyIndicationsHiredByUser(repository.countIndicationsHiredByUser(userEmail))
                .qtyIndicationsDiscardedByUser(repository.countIndicationsDiscardedByUser(userEmail))
                .build();
    }

//    private void findKeyWordInIndication(Indication indication) throws IOException {
    @Override
    public void findKeyWordInIndication(Long id) throws IOException {
        Indication indication = findById(id);

        File file = null;
        Resource resource = fileStorageService.loadFileAsResource(indication.getFileNameAttachment());

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
                List<KeyWordIndication> words = indication.getKeyWordIndications();

                words.forEach( word -> {
                    for (String line : lines) {
                        if (line.toUpperCase().contains(word.getWord().toUpperCase())) {
                            word.setFound(true);
                            keyWordIndicationRepository.save(word);
                            break;
                        }
                    }
                });
            }
            updateStatusAfterFindKeyWord(indication);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Indication updateStatusAfterFindKeyWord(Indication indication) {
        Integer automaticEvaluationQuantity = indication.getOpportunity().getAutomaticEvaluationQuantity();
        Long qtykeyWordTrue = indication.getKeyWordIndications().stream().filter(w -> Boolean.TRUE.equals(w.getFound())).count();

        if (qtykeyWordTrue >= automaticEvaluationQuantity) {
            indication.setStatus(IndicationStatus.PRE_EVALUATION_OK);
        } else {
            indication.setStatus(IndicationStatus.PRE_EVALUATION_NOK);
        }

        Indication indicationSaved = repository.save(indication);
        setIndicationHistory(indicationSaved);

        return indicationSaved;
    }
}