package edu.uea.acadmanage.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Envia um e-mail simples para o destinatário.
     *
     * @param to      Endereço de e-mail do destinatário.
     * @param subject Assunto do e-mail.
     * @param text    Corpo do e-mail.
     */
    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            message.setFrom("no-reply@tecnocomp.uea.edu.br"); // Endereço de envio padrão
            mailSender.send(message);
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao enviar e-mail para " + to, e);
        }
    }
}
