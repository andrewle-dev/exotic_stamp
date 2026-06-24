package metro.ExoticStamp.infra.mail;

import metro.ExoticStamp.infra.mail.queue.MailQueueService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    @Mock private MailQueueService mailQueueService;
    @Mock private MailProperties mailProperties;

    @InjectMocks
    private MailService mailService;

    @Test
    void sendVerifyEmail_usesDedupKey() {
        when(mailProperties.getLogoUrl()).thenReturn("http://logo");
        mailService.sendVerifyEmail("u@test.com", "user1", "http://app/verify?token=abc");
        verify(mailQueueService).enqueueHtmlMail(
                eq("u@test.com"),
                anyString(),
                anyString(),
                eq("verify:u@test.com"));
    }

    @Test
    void sendOtpEmail_doesNotLeakFullJwt() {
        when(mailProperties.getLogoUrl()).thenReturn("http://logo");
        mailService.sendOtpEmail("u@test.com", "123456");
        verify(mailQueueService).enqueueHtmlMail(
                eq("u@test.com"),
                anyString(),
                anyString());
    }
}
