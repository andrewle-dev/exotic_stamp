package metro.ExoticStamp.infra.mail;

import metro.ExoticStamp.infra.mail.queue.MailQueueService;
import metro.ExoticStamp.infra.mail.template.OtpEmailTemplate;
import metro.ExoticStamp.infra.mail.template.VerifyEmailTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final MailQueueService mailQueueService;
    private final MailProperties mailProperties;

    public void sendVerifyEmail(String toEmail, String username, String verifyUrl) {
        String body = VerifyEmailTemplate.build(username, verifyUrl, mailProperties.getLogoUrl());
        String dedupKey = "verify:" + toEmail;
        mailQueueService.enqueueHtmlMail(toEmail, MailSubjectConstants.VERIFY_ACCOUNT, body, dedupKey);
    }

    public void sendOtpEmail(String toEmail, String otp) {
        String body = OtpEmailTemplate.build(otp, mailProperties.getLogoUrl());
        mailQueueService.enqueueHtmlMail(toEmail, MailSubjectConstants.OTP_VERIFICATION_CODE, body);
    }
}
