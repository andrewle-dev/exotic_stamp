package metro.ExoticStamp.infra.mail;

import metro.ExoticStamp.infra.mail.queue.MailContentType;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class SmtpMailSenderAdapter implements MailSenderPort {

    private static final String ENCODING = "UTF-8";

    private final JavaMailSender mailSender;
    private final MailProperties mailProperties;

    @Override
    public void send(MailMessage message) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, ENCODING);
            String from = Objects.requireNonNullElse(mailProperties.getFrom(), message.from());
            helper.setFrom(Objects.requireNonNull(from, "Mail 'from' must be configured"));
            helper.setTo(Objects.requireNonNull(message.to(), "MailMessage.to must not be null"));
            helper.setSubject(Objects.requireNonNull(message.subject(), "MailMessage.subject must not be null"));
            helper.setText(
                    Objects.requireNonNull(message.body(), "MailMessage.body must not be null"),
                    message.contentType() == MailContentType.HTML
            );
            mailSender.send(mimeMessage);
        } catch (MessagingException | MailException e) {
            String causeMsg = e.getCause() != null ? e.getCause().getMessage() : null;
            String detail = (causeMsg == null || causeMsg.isBlank()) ? e.getMessage() : causeMsg;
            throw new RuntimeException("Failed to send mail via SMTP: " + detail, e);
        }
    }
}
