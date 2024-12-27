package edu.uea.acadmanage.util;

import java.util.Scanner;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GerarSenhas {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println("Informe a senha: ");
        String senha = new Scanner(System.in).nextLine();

        System.out.println("Senha Encoded: " + encoder.encode(senha));
        
    }
}
