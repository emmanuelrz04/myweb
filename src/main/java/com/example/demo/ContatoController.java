package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.HtmlUtils;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class ContatoController {

    @Autowired
    private JavaMailSender mailSender;
    
    private final Map<String, Instant> ultimoEnvio = new ConcurrentHashMap<>();

    @GetMapping("/contato")
    public String mostrarFormulario() {
        return "contato";
    }

    @PostMapping("/enviar")
    public String enviarMensagem(
            @RequestParam String nome,
            @RequestParam String email,
            @RequestParam String mensagem,
            RedirectAttributes redirectAttributes) {
        
        String chave = "email:" + email;
        
        if (ultimoEnvio.containsKey(chave) && 
            ultimoEnvio.get(chave).isAfter(Instant.now().minusSeconds(60))) {
            redirectAttributes.addFlashAttribute("erro", "Aguarde 1 minuto entre envios");
            return "redirect:/contato";
        }
        
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            redirectAttributes.addFlashAttribute("erro", "E-mail inválido! Use um formato como: usuario@dominio.com");
            return "redirect:/contato";
        }
        
        try {
            String nomeSeguro = HtmlUtils.htmlEscape(nome);
            String emailSeguro = HtmlUtils.htmlEscape(email);
            String mensagemSegura = HtmlUtils.htmlEscape(mensagem);
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo("emmanuelricardo04@gmail.com");
            message.setReplyTo(email);
            message.setSubject("Contato do Portfólio - " + nomeSeguro);
            message.setText("Nome: " + nomeSeguro + 
                           "\nE-mail: " + emailSeguro + 
                           "\n\nMensagem:\n" + mensagemSegura);
            
            mailSender.send(message);
            
            ultimoEnvio.put(chave, Instant.now());
            
            redirectAttributes.addFlashAttribute("sucesso", "Mensagem enviada com sucesso!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao enviar mensagem: " + e.getMessage());
        }
        
        return "redirect:/contato";
    }
}