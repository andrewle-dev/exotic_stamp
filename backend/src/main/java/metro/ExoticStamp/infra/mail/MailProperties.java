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

    /**
     * Display name shown by mail clients next to the From address (e.g. "Exotic Travelers").
     * Configurable per environment; when blank, only the raw address is used.
     */
    @Value("${application.mail.display-name:Exotic Travelers}")
    private String displayName;

    /**
     * Optional Reply-To. When blank, replies fall back to the From address (a real monitored inbox),
     * which is preferable to a hard-bouncing noreply@ for sender reputation.
     */
    @Value("${application.mail.reply-to:}")
    private String replyTo;

    /**
     * Optional List-Unsubscribe header value (e.g. "&lt;mailto:unsubscribe@domain&gt;, &lt;https://domain/unsub&gt;").
     * Only emitted when configured; pairs with List-Unsubscribe-Post for one-click unsubscribe.
     */
    @Value("${application.mail.list-unsubscribe:}")
    private String listUnsubscribe;

    @Value("${application.mail.logo-url}")
    private String logoUrl;

    @Value("${application.frontend.dev}")
    private String frontendUrl;
}
