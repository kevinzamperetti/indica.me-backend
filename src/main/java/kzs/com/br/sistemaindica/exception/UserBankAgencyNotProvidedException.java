package kzs.com.br.sistemaindica.exception;

import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ResponseStatus(BAD_REQUEST)
public class UserBankAgencyNotProvidedException extends RuntimeException {

    public UserBankAgencyNotProvidedException(String message) {
        super(message);
    }

}
