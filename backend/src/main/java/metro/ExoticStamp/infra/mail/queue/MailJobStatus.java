package metro.ExoticStamp.infra.mail.queue;

public enum MailJobStatus {
    PENDING,
    PROCESSING,
    SENT,
    FAILED,
    DEAD
}
