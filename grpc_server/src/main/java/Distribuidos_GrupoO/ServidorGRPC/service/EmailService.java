package Distribuidos_GrupoO.ServidorGRPC.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    public void enviarContrasena(String email, String contrasena) {
        try {
            logger.info("Intentando enviar email a {} con contraseña", email);
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setFrom("empujecomunitariotp@gmail.com");
            message.setSubject("Tu contraseña de acceso");
            message.setText("Hola,\n\nTu cuenta ha sido creada. Tu contraseña es: " + contrasena + "\n\nPor favor, utilizala para iniciar sesión.\n\nSaludos.");
            mailSender.send(message);
            logger.info("Email enviado exitosamente a {}", email);
        } catch (Exception e) {
            logger.error("Error al enviar email a {}: {}", email, e.getMessage(), e);
        }
    }
}
