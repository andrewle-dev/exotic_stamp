package metro.ExoticStamp.infra.mail.template;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class OtpEmailTemplate {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM, yyyy", Locale.ENGLISH);

    private OtpEmailTemplate() {
    }

    public static String build(String otp, String logoUrl) {
        String safeLogo = escapeHtml(logoUrl);
        String dateStr = LocalDate.now().format(DATE_FMT);

        StringBuilder otpSpans = new StringBuilder();
        for (char c : otp.toCharArray()) {
            otpSpans.append(
                    "<span class=\"otp-digit\" style=\"display:inline-block;margin:4px;padding:12px 16px;font-size:18px;font-weight:bold;color:#F17C54;border:2px solid #F17C54;border-radius:6px;background:#fff;\">")
                    .append(escapeHtml(String.valueOf(c)))
                    .append("</span>");
        }

        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>Password reset</title>
                  <style>
                    body { margin:0; padding:24px; font-family:Arial,Helvetica,sans-serif; background:#f5f5f5; }
                    .container { max-width:560px; margin:0 auto; background:#ffffff; padding:32px; border-radius:8px; }
                    .heading { color:#333333; font-size:20px; margin:0 0 16px; }
                    .body-text { color:#555555; font-size:15px; line-height:1.5; }
                    .footer { color:#aaaaaa; font-size:12px; line-height:1.5; margin-top:28px; }
                    @media (prefers-color-scheme: dark) {
                      body { background:#121212 !important; }
                      .container { background:#1e1e1e !important; }
                      .heading { color:#eeeeee !important; }
                      .body-text { color:#cccccc !important; }
                      .otp-digit { background:#2b2b2b !important; color:#F17C54 !important; border-color:#F17C54 !important; }
                    }
                    @media (max-width: 480px) {
                      .container { width:92%% !important; padding:20px !important; }
                      .otp-digit { padding:8px 12px !important; font-size:16px !important; }
                    }
                  </style>
                </head>
                <body>
                  <div class="container">
                    <div style="text-align:center;margin-bottom:24px;">
                      <img src="%s" alt="Logo" style="max-width:180px;height:auto;">
                    </div>
                    <h1 class="heading">Face Wash Fox System – Password Reset</h1>
                    <p class="body-text">You requested to reset your password on <b>%s</b>. Use the following 6-digit OTP:</p>
                    <p style="margin:20px 0;text-align:center;">%s</p>
                    <p class="footer">This OTP is valid for 5 minutes. Do not share it with anyone.</p>
                  </div>
                </body>
                </html>
                """.formatted(safeLogo, dateStr, otpSpans);
    }

    private static String escapeHtml(String input) {
        if (input == null) {
            return "";
        }
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }
}
