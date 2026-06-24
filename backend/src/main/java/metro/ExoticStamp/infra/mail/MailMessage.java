package metro.ExoticStamp.infra.mail;

import metro.ExoticStamp.infra.mail.queue.MailContentType;

public record MailMessage(
        String from,
        String to,
        String subject,
        String body,
        MailContentType contentType
) {
}
