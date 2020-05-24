package kzs.com.br.sistemaindica.service.impl;

import kzs.com.br.sistemaindica.enums.IndicationStatus;
import kzs.com.br.sistemaindica.service.IndicationStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IndicationStatusServiceImpl implements IndicationStatusService {

    @Override
    public IndicationStatus[] listAll() {
        return IndicationStatus.values();
    }
}