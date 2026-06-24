package metro.ExoticStamp.infra.mail;

/**
 * Subjects for queued mail — kept in one place (no inline magic strings in {@link MailService}).
 */
public final class MailSubjectConstants {

    public static final String VERIFY_ACCOUNT = "Verify your MetricsX account";
    public static final String OTP_VERIFICATION_CODE = "Your MetricsX verification code";

    private MailSubjectConstants() {
    }
}
