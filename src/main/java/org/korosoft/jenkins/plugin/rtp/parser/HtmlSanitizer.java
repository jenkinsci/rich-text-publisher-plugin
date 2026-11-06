package org.korosoft.jenkins.plugin.rtp.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import org.korosoft.jenkins.plugin.rtp.RichTextPublisherConfiguration;

import java.util.Locale;

public final class HtmlSanitizer {
    private static final String BASE_URI = "https://jenkins.invalid/";
    private static final Safelist SAFE_HTML = Safelist.relaxed()
            .preserveRelativeLinks(true)
            .addAttributes("table", "border", "cellpadding", "cellspacing", "style")
            .addAttributes("tr", "style")
            .addAttributes("td", "border", "style")
            .addAttributes("th", "border", "style");

    private HtmlSanitizer() {
    }

    public static String sanitize(String html) {
        if (html == null || RichTextPublisherConfiguration.isUnsafeHtmlAllowed()) {
            return html;
        }
        return sanitizeHtml(html);
    }

    static String sanitizeHtml(String html) {
        if (html == null) {
            return null;
        }

        Document document = Jsoup.parseBodyFragment(html);
        for (Element element : document.getAllElements()) {
            if (element.hasAttr("style")) {
                String safeStyle = isTableElement(element) ? sanitizeTableStyle(element.attr("style")) : "";
                if (safeStyle.isEmpty()) {
                    element.removeAttr("style");
                } else {
                    element.attr("style", safeStyle);
                }
            }
        }
        return Jsoup.clean(document.body().html(), BASE_URI, SAFE_HTML);
    }

    private static boolean isTableElement(Element element) {
        String tagName = element.normalName();
        return "table".equals(tagName) || "tr".equals(tagName) || "td".equals(tagName) || "th".equals(tagName);
    }

    private static String sanitizeTableStyle(String style) {
        StringBuilder safeStyle = new StringBuilder();
        for (String declaration : style.split(";")) {
            int separator = declaration.indexOf(':');
            if (separator < 1) {
                continue;
            }
            String property = declaration.substring(0, separator).trim().toLowerCase(Locale.ROOT);
            String value = declaration.substring(separator + 1).trim().toLowerCase(Locale.ROOT);
            if (isSafeBorderDeclaration(property, value)) {
                safeStyle.append(property).append(':').append(value).append(';');
            }
        }
        return safeStyle.toString();
    }

    private static boolean isSafeBorderDeclaration(String property, String value) {
        if ("border-collapse".equals(property)) {
            return "collapse".equals(value) || "separate".equals(value);
        }
        if ("border-spacing".equals(property)) {
            return hasSafeValues(value, 2, HtmlSanitizer::isSafeLength);
        }
        if (isBorderShorthand(property)) {
            return hasSafeValues(value, 3, HtmlSanitizer::isSafeBorderPart);
        }
        if (isBorderComponent(property, "width")) {
            return hasSafeValues(value, 4, HtmlSanitizer::isSafeLength);
        }
        if (isBorderComponent(property, "style")) {
            return hasSafeValues(value, 4, HtmlSanitizer::isSafeBorderStyle);
        }
        if (isBorderComponent(property, "color")) {
            return hasSafeValues(value, 4, HtmlSanitizer::isSafeColor);
        }
        return false;
    }

    private static boolean isBorderShorthand(String property) {
        return "border".equals(property) || property.matches("border-(top|right|bottom|left)");
    }

    private static boolean isBorderComponent(String property, String component) {
        return ("border-" + component).equals(property)
                || property.matches("border-(top|right|bottom|left)-" + component);
    }

    private static boolean hasSafeValues(String value, int maximumParts, java.util.function.Predicate<String> validator) {
        String[] parts = value.split("\\s+");
        if (parts.length == 0 || parts.length > maximumParts) {
            return false;
        }
        for (String part : parts) {
            if (!validator.test(part)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isSafeBorderPart(String value) {
        return isSafeLength(value) || isSafeBorderStyle(value) || isSafeColor(value);
    }

    private static boolean isSafeLength(String value) {
        return value.matches("(?i)(0|[0-9]+(?:\\.[0-9]+)?(?:px|pt|em|rem)|thin|medium|thick)");
    }

    private static boolean isSafeBorderStyle(String value) {
        return value.matches("(?i)(none|hidden|dotted|dashed|solid|double|groove|ridge|inset|outset)");
    }

    private static boolean isSafeColor(String value) {
        return value.matches("(?i)(#[0-9a-f]{3,8}|[a-z]{1,24})");
    }
}
