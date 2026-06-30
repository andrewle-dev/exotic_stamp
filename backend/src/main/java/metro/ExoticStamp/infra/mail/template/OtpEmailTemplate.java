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
        String otpBoxes = EmailOtpDigitBoxes.render(otp);

        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>Password reset</title>
                  <style>
                    @media (max-width: 480px) {
                      .container { width: 92%% !important; padding: 0 !important; }
                      .body-pad { padding: 28px 22px 8px !important; }
                    }
                    @media (prefers-color-scheme: dark) {
                      body { background:#121212 !important; }
                      .container { background:#1e1e1e !important; }
                      .heading { color:#eeeeee !important; }
                      .body-text { color:#cccccc !important; }
                    }
                  </style>
                </head>
                <body style="margin:0;padding:0;background:#eef1f5;font-family:Arial,Helvetica,sans-serif;">
                  <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background:#eef1f5;padding:32px 0;">
                    <tr>
                      <td align="center">
                        <table role="presentation" class="container" width="560" cellpadding="0" cellspacing="0" style="max-width:560px;width:100%%;background:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 2px 10px rgba(16,40,72,0.08);">
                          <tr>
                            <td style="background:#13294B;padding:28px 32px;text-align:center;">
                              <img src="%s" alt="Exotic Stamp" width="56" height="56" style="display:block;margin:0 auto 10px;border-radius:50%%;background:#ffffff;padding:4px;">
                              <div style="color:#ffffff;font-size:18px;font-weight:bold;letter-spacing:0.5px;">EXOTIC STAMP</div>
                            </td>
                          </tr>
                          <tr>
                            <td style="height:4px;background:linear-gradient(90deg,#13294B 0%%,#D2393C 100%%);font-size:0;line-height:0;">&nbsp;</td>
                          </tr>
                          <tr>
                            <td class="body-pad" style="padding:36px 36px 8px;">
                              <h1 class="heading" style="color:#13294B;font-size:20px;margin:0 0 16px;font-weight:bold;">Password reset request</h1>
                              <p class="body-text" style="color:#4A5568;font-size:15px;line-height:1.6;margin:0 0 8px;">
                                You requested to reset your password on <b style="color:#13294B;">%s</b>. Use the code below to continue — it's valid for 5 minutes.
                              </p>
                              <table role="presentation" cellpadding="0" cellspacing="0" style="margin:24px auto 24px;">
                                <tr>
                                  <td style="background:#FBF1EF;border:1px solid #F0D9D6;border-radius:10px;padding:18px 22px;">
                                    %s
                                  </td>
                                </tr>
                              </table>
                              <p style="color:#8895A7;font-size:13px;line-height:1.6;margin:0 0 28px;text-align:center;">
                                Didn't request this? You can safely ignore this email — your password won't be changed.
                              </p>
                              <hr style="border:none;border-top:1px solid #E7EBF0;margin:0 0 20px;">
                              <p style="color:#A3AEBB;font-size:12px;line-height:1.6;margin:0;">
                                For your security, never share this code with anyone — including someone claiming to be from Exotic Stamp support.
                              </p>
                            </td>
                          </tr>
                          <tr>
                            <td style="background:#F6F8FA;padding:22px 32px;text-align:center;">
                              <p style="color:#A3AEBB;font-size:12px;margin:0 0 4px;">© 2026 Exotic Stamp. All rights reserved.</p>
                              <p style="color:#C2CAD3;font-size:11px;margin:0;">This is an automated message — please do not reply directly to this email.</p>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(safeLogo, dateStr, otpBoxes);
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
