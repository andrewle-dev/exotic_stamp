package metro.ExoticStamp.infra.mail;

import java.util.regex.Pattern;

/**
 * Minimal, dependency-free HTML-to-text conversion used to build the plain-text
 * alternative part of a multipart/alternative email. Spam filters score HTML-only
 * mail worse, so every HTML message is paired with this fallback.
 *
 * <p>This is intentionally lightweight (not a full HTML parser): it drops
 * script/style/head, turns common block/break tags into newlines, strips the
 * remaining tags, decodes a small set of HTML entities, and tidies whitespace.
 */
final class HtmlToPlainText {

    private static final Pattern SCRIPT_STYLE_HEAD =
            Pattern.compile("(?is)<(script|style|head)[^>]*>.*?</\\1>");
    private static final Pattern BREAK_TAGS =
            Pattern.compile("(?i)<br\\s*/?>|</p>|</div>|</tr>|</h[1-6]>|</li>");
    private static final Pattern ANY_TAG = Pattern.compile("(?s)<[^>]+>");
    private static final Pattern TRAILING_SPACES = Pattern.compile("[ \\t]+\\n");
    private static final Pattern MANY_BLANK_LINES = Pattern.compile("\\n{3,}");
    private static final Pattern REPEATED_SPACES = Pattern.compile("[ \\t]{2,}");

    private HtmlToPlainText() {
    }

    static String convert(String html) {
        if (html == null || html.isBlank()) {
            return "";
        }
        String text = SCRIPT_STYLE_HEAD.matcher(html).replaceAll("");
        text = BREAK_TAGS.matcher(text).replaceAll("\n");
        text = ANY_TAG.matcher(text).replaceAll("");
        text = decodeEntities(text);
        text = TRAILING_SPACES.matcher(text).replaceAll("\n");
        text = REPEATED_SPACES.matcher(text).replaceAll(" ");
        text = MANY_BLANK_LINES.matcher(text).replaceAll("\n\n");
        return text.trim();
    }

    private static String decodeEntities(String s) {
        return s
                .replace("&nbsp;", " ")
                .replace("&#39;", "'")
                .replace("&#x27;", "'")
                .replace("&quot;", "\"")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&");
    }
}
