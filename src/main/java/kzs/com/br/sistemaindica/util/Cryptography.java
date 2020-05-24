package kzs.com.br.sistemaindica.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class Cryptography {

    public static String cryptographyPassword(String password){
        return new BCryptPasswordEncoder( 6 ).encode( password );
    }
}
