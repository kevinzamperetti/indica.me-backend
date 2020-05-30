package kzs.com.br.sistemaindica.controller;

import kzs.com.br.sistemaindica.entity.KeyWordCandidature;
import kzs.com.br.sistemaindica.repository.KeyWordCandidatureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/keyWordCandidature")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class KeyWordCandidatureController {

    private final KeyWordCandidatureRepository repository;

    @GetMapping(path = "/{id}")
    public ResponseEntity<List<KeyWordCandidature>> findByIndicationId(@PathVariable(value = "id") Long id) {
        return ResponseEntity.ok(repository.findByCandidatureId(id));
    }

}
