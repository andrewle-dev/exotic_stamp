package metro.ExoticStamp.infra.mail;

public interface MailSenderPort {

    void send(MailMessage message) throws Exception;
}
