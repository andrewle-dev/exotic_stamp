package metro.ExoticStamp.infra.mail.template;

public final class VerifyEmailTemplate {

    private VerifyEmailTemplate() {
    }

    public static String build(String username, String verifyUrl, String logoUrl) {
        String safeUser = escapeHtml(username);
        String safeUrl = escapeHtml(verifyUrl);
        String safeLogo = escapeHtml(logoUrl);

        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>Verify your account</title>
                </head>
                <body style="margin:0;padding:24px;font-family:Arial,Helvetica,sans-serif;background:#f5f5f5;">
                  <div style="max-width:560px;margin:0 auto;background:#ffffff;padding:32px;border-radius:8px;">
                    <div style="text-align:center;margin-bottom:24px;">
                      <img src="%s" alt="Logo" style="max-width:180px;height:auto;">
                    </div>
                    <h1 style="color:#333333;font-size:22px;margin:0 0 16px;">Welcome to Face Wash Fox System</h1>
                    <p style="color:#555555;font-size:15px;line-height:1.5;">Hi <b>%s</b>,</p>
                    <p style="color:#555555;font-size:15px;line-height:1.5;">We're excited to have you get started. Please confirm your account by pressing the button below.</p>
                    <p style="margin:28px 0;text-align:center;">
                      <a href="%s" style="background:#F17C54;color:#ffffff;font-weight:bold;text-decoration:none;border-radius:5px;padding:12px 24px;display:inline-block;">Validate Account</a>
                    </p>
                    <p style="color:#aaaaaa;font-size:12px;line-height:1.5;margin-top:32px;">This link expires in 15 minutes. This is an automated message, please do not reply.</p>
                  </div>
                </body>
                </html>
                """.formatted(safeLogo, safeUser, safeUrl);
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
