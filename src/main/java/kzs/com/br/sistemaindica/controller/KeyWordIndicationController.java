package kzs.com.br.sistemaindica.controller;

import kzs.com.br.sistemaindica.entity.KeyWordIndication;
import kzs.com.br.sistemaindica.repository.KeyWordIndicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/keyWordIndication")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class KeyWordIndicationController {

    private final KeyWordIndicationRepository repository;

    @GetMapping(path = "/{id}")
    public ResponseEntity<List<KeyWordIndication>> findByIndicationId(@PathVariable(value = "id") Long id) {
        return ResponseEntity.ok(repository.findByIndicationId(id));
    }

}
