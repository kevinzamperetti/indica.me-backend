package kzs.com.br.sistemaindica.controller;

import kzs.com.br.sistemaindica.entity.Candidature;
import kzs.com.br.sistemaindica.entity.Indication;
import kzs.com.br.sistemaindica.exception.MyFileNotFoundException;
import kzs.com.br.sistemaindica.payload.UploadFileResponse;
import kzs.com.br.sistemaindica.repository.CandidatureRepository;
import kzs.com.br.sistemaindica.repository.IndicationRepository;
import kzs.com.br.sistemaindica.service.impl.FileStorageServiceImpl;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    private final FileStorageServiceImpl fileStorageService;

    private final IndicationRepository indicationRepository;

    private final CandidatureRepository candidatureRepository;

    @PostMapping("/uploadFile")
    public UploadFileResponse uploadFile(@RequestParam("file") MultipartFile file) {
        String fileName = fileStorageService.storeFile(file);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/file/downloadFile/")
                .path(fileName)
                .toUriString();

        return new UploadFileResponse(fileName, fileDownloadUri,
                file.getContentType(), file.getSize());
    }

    @PostMapping("/uploadMultipleFiles")
    public List<UploadFileResponse> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
        return Arrays.asList(files)
                .stream()
                .map(file -> uploadFile(file))
                .collect(Collectors.toList());
    }

    @GetMapping("/indication/downloadFile/{fileName:.+}")
    public ResponseEntity<Resource> downloadFileIndication(@PathVariable String fileName, HttpServletRequest request) throws IOException {

        Resource resource = null;
        Indication indication = indicationRepository.findByFileNameAttachment(fileName);

        String contentType = indication.getFileTypeAttachment();
        if(contentType == null) {
            contentType = "application/octet-stream";
        }

        try {
            resource = fileStorageService.loadFileAsResource(fileName);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (MyFileNotFoundException e) {
            byte[] decoded = java.util.Base64.getDecoder().decode(indication.getAttachment());
            String filaPath = fileStorageService.getResourceByFileName(fileName).getFile().getPath();
            FileOutputStream fos = new FileOutputStream(filaPath);
            fos.write(decoded);
            fos.flush();
            fos.close();
            resource = fileStorageService.loadFileAsResource(fileName);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        }
    }

    @GetMapping("/candidature/downloadFile/{fileName:.+}")
    public ResponseEntity<Resource> downloadFileCandidature(@PathVariable String fileName, HttpServletRequest request) throws IOException {

        Resource resource = null;
        Candidature candidature = candidatureRepository.findByFileNameAttachment(fileName);

        String contentType = candidature.getFileTypeAttachment();
        if(contentType == null) {
            contentType = "application/octet-stream";
        }

        try {
            resource = fileStorageService.loadFileAsResource(fileName);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (MyFileNotFoundException e) {
            byte[] decoded = java.util.Base64.getDecoder().decode(candidature.getAttachment());
            String filaPath = fileStorageService.getResourceByFileName(fileName).getFile().getPath();
            FileOutputStream fos = new FileOutputStream(filaPath);
            fos.write(decoded);
            fos.flush();
            fos.close();
            resource = fileStorageService.loadFileAsResource(fileName);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        }
    }

    @GetMapping(path = "/teste/{fileName:.+}")
    public ResponseEntity<Boolean> teste(@PathVariable String fileName) throws IOException {

        //File file = new File("C:\\Projetos\\indica.me-backend\\attachments\\Curriculo_20200514_190348.pdf");

        Resource resource = fileStorageService.loadFileAsResource("Curriculo_20200514_190348.pdf");
        File file = resource.getFile();

        try (
                PDDocument document = PDDocument.load( file )) {

            if (!document.isEncrypted()) {

                PDFTextStripperByArea stripper = new PDFTextStripperByArea();
                stripper.setSortByPosition(true);

                PDFTextStripper tStripper = new PDFTextStripper();

                String pdfFileInText = tStripper.getText(document);

                String lines[] = pdfFileInText.split("\\r?\\n");
                List<String> words = new ArrayList<>();
                words.add("cooperativismo");
                words.add("voto");
                words.add("decisões");
                words.add("assembleia");
                words.add("prograMADOR delphi");

                words.forEach( word -> {
                    for (String line : lines) {
                        if (line.toUpperCase().contains(word.toUpperCase())) {
                            System.out.println("===> Palavra '" + word + "'. ENCONTRADA na linha em: " + line + "<===");
                            break;
                        }
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok().body(true);
    }

}