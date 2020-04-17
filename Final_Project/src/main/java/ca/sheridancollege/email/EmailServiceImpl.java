package ca.sheridancollege.email;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Component
public class EmailServiceImpl {

    @Autowired
    private JavaMailSender emailSender;

    public void sendSimpleMessagee(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        emailSender.send(message);
    }

    @Autowired
    private TemplateEngine templateEngine;

    public void sendMailWithInline(String to, String subject, String name,
            String messagebody, String footer) throws MessagingException {

        final Context ctx = new Context();
        ctx.setVariable("name", name);
        ctx.setVariable("message", messagebody);
        ctx.setVariable("footer", footer);

        final MimeMessage mimeMessage = this.emailSender.createMimeMessage();
        final MimeMessageHelper message = 
                new MimeMessageHelper(mimeMessage, true, "UTF-8");

        message.setTo(to);
        message.setSubject(subject);
        message.setFrom("researvaTest34049@gmail.com");

        final String htmlContent = this.templateEngine.process("emailTemplate.html", ctx);

        message.setText(htmlContent, true);

        this.emailSender.send(mimeMessage);
    }
}