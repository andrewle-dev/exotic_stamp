package metro.ExoticStamp.infra.mail.template;

public final class VerifyEmailTemplate {

    private VerifyEmailTemplate() {
    }

    public static String build(String username, String otp, String logoUrl) {
        String safeUser = escapeHtml(username);
        String safeLogo = escapeHtml(logoUrl);
        String otpBoxes = EmailOtpDigitBoxes.render(otp);

        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>Activate your account</title>
                </head>
                <body style="margin:0;padding:0;background:#eef1f5;font-family:Arial,Helvetica,sans-serif;">
                  <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background:#eef1f5;padding:32px 0;">
                    <tr>
                      <td align="center">
                        <table role="presentation" width="560" cellpadding="0" cellspacing="0" style="max-width:560px;width:100%%;background:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 2px 10px rgba(16,40,72,0.08);">
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
                            <td style="padding:36px 36px 8px;">
                              <h1 style="color:#13294B;font-size:21px;margin:0 0 16px;font-weight:bold;">Confirm your email to activate your account</h1>
                              <p style="color:#4A5568;font-size:15px;line-height:1.6;margin:0 0 12px;">Hi <b style="color:#13294B;">%s</b>,</p>
                              <p style="color:#4A5568;font-size:15px;line-height:1.6;margin:0 0 8px;">
                                Welcome aboard! Enter this code in the app to activate your account and start collecting stamps at every station.
                              </p>
                              <table role="presentation" cellpadding="0" cellspacing="0" style="margin:24px auto 24px;">
                                <tr>
                                  <td style="background:#FBF1EF;border:1px solid #F0D9D6;border-radius:10px;padding:18px 22px;">
                                    %s
                                  </td>
                                </tr>
                              </table>
                              <p style="color:#8895A7;font-size:13px;line-height:1.6;margin:0 0 12px;text-align:center;">
                                This code is for activating your account — not for resetting your password.
                              </p>
                              <hr style="border:none;border-top:1px solid #E7EBF0;margin:0 0 20px;">
                              <p style="color:#A3AEBB;font-size:12px;line-height:1.6;margin:0;">
                                This code expires in 10 minutes for your security. If you didn't create an Exotic Stamp account, you can safely ignore this email.
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
                """.formatted(safeLogo, safeUser, otpBoxes);
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
