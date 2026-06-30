package metro.ExoticStamp.infra.mail;

import metro.ExoticStamp.infra.mail.queue.MailContentType;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class SmtpMailSenderAdapter implements MailSenderPort {

    private static final String ENCODING = "UTF-8";
    private static final String LIST_UNSUBSCRIBE_HEADER = "List-Unsubscribe";
    private static final String LIST_UNSUBSCRIBE_POST_HEADER = "List-Unsubscribe-Post";
    private static final String ONE_CLICK = "List-Unsubscribe=One-Click";

    private final JavaMailSender mailSender;
    private final MailProperties mailProperties;

    @Override
    public void send(MailMessage message) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            boolean isHtml = message.contentType() == MailContentType.HTML;
            // multipart=true for HTML so we can attach a plain-text alternative (multipart/alternative)
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, isHtml, ENCODING);

            applyFrom(helper, message);
            applyReplyTo(helper);

            helper.setTo(Objects.requireNonNull(message.to(), "MailMessage.to must not be null"));
            helper.setSubject(Objects.requireNonNull(message.subject(), "MailMessage.subject must not be null"));

            String body = Objects.requireNonNull(message.body(), "MailMessage.body must not be null");
            if (isHtml) {
                // Order matters: plain-text first, HTML second — clients render the last supported part.
                helper.setText(HtmlToPlainText.convert(body), body);
            } else {
                helper.setText(body, false);
            }

            applyListUnsubscribe(mimeMessage);

            mailSender.send(mimeMessage);
        } catch (MessagingException | MailException | UnsupportedEncodingException e) {
            String causeMsg = e.getCause() != null ? e.getCause().getMessage() : null;
            String detail = (causeMsg == null || causeMsg.isBlank()) ? e.getMessage() : causeMsg;
            throw new RuntimeException("Failed to send mail via SMTP: " + detail, e);
        }
    }

    private void applyFrom(MimeMessageHelper helper, MailMessage message)
            throws MessagingException, UnsupportedEncodingException {
        String from = Objects.requireNonNullElse(mailProperties.getFrom(), message.from());
        Objects.requireNonNull(from, "Mail 'from' must be configured");
        String displayName = mailProperties.getDisplayName();
        if (StringUtils.hasText(displayName)) {
            helper.setFrom(from, displayName);
        } else {
            helper.setFrom(from);
        }
    }

    private void applyReplyTo(MimeMessageHelper helper) throws MessagingException {
        String replyTo = mailProperties.getReplyTo();
        if (StringUtils.hasText(replyTo)) {
            helper.setReplyTo(replyTo);
        }
    }

    private void applyListUnsubscribe(MimeMessage mimeMessage) throws MessagingException {
        String listUnsubscribe = mailProperties.getListUnsubscribe();
        if (StringUtils.hasText(listUnsubscribe)) {
            mimeMessage.setHeader(LIST_UNSUBSCRIBE_HEADER, listUnsubscribe);
            mimeMessage.setHeader(LIST_UNSUBSCRIBE_POST_HEADER, ONE_CLICK);
        }
    }
}
