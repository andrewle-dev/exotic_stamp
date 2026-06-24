package metro.ExoticStamp.infra.mail;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class MailProperties {

    @Value("${application.mail.from}")
    private String from;

    @Value("${application.mail.logo-url}")
    private String logoUrl;

    @Value("${application.frontend.dev}")
    private String frontendUrl;
}
