package metro.ExoticStamp.infra.mail.template;

public final class EmailOtpDigitBoxes {

    private EmailOtpDigitBoxes() {
    }

    public static String render(String otp) {
        if (otp == null || otp.isEmpty()) {
            return "";
        }
        StringBuilder otpSpans = new StringBuilder();
        for (char c : otp.toCharArray()) {
            otpSpans.append(
                    "<span style=\"display:inline-block;width:32px;height:32px;line-height:32px;margin:3px;"
                  + "font-size:18px;font-weight:bold;color:#13294B;border:1.5px solid #D2393C;border-radius:6px;"
                  + "background:#ffffff;text-align:center;\">")
                    .append(escapeHtml(String.valueOf(c)))
                    .append("</span>");
        }
        return otpSpans.toString();
    }

    private static String escapeHtml(String input) {
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }
}
