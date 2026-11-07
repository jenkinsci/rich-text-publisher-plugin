package org.korosoft.jenkins.plugin.rtp.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.korosoft.jenkins.plugin.rtp.BuildRichTextAction;

import java.util.Locale;

class HTMLMarkupParserTest {
    @Test
    void removesUnsafeMarkupAndPreservesFormatting() {
        String result = HtmlSanitizer.sanitizeHtml("<p><b>Safe formatting</b></p>"
                + "<table border='1' cellpadding='4' cellspacing='0' style='border-collapse:collapse;border:1px solid #000;"
                + "background-image:url(javascript:alert(1))'><tr><td border='1' style='border:1px solid red'>cell</td></tr></table>"
                + "<script>alert('xss')</script><img src=x onerror=alert('xss')>"
                + "<a href='javascript:alert(1)'>unsafe link</a>");

        assertTrue(result.contains("<b>Safe formatting</b>"));
        assertTrue(result.contains("border=\"1\""));
        assertTrue(result.contains("cellpadding=\"4\""));
        assertTrue(result.contains("cellspacing=\"0\""));
        assertTrue(result.contains("border-collapse:collapse"));
        assertTrue(result.contains("border:1px solid #000"));
        assertTrue(result.contains("border:1px solid red"));
        assertTrue(result.contains("<td border=\"1\""));
        assertTrue(result.contains(">cell</td>"));
        assertFalse(result.contains("background-image"));
        assertFalse(result.contains("<script"));
        assertFalse(result.contains("alert('xss')"));
        assertFalse(result.contains("onerror"));
        assertFalse(result.contains("javascript:"));
    }

    @Test
    void sanitizesPreviouslyStoredBuildMessagesByDefault() {
        BuildRichTextAction action = new BuildRichTextAction(null,
                "<b>Safe formatting</b><script>alert('xss')</script>");

        assertEquals("<b>Safe formatting</b>", action.getRichText());
    }

    @Test
    void keepsRelativeLinksAndImages() {
        String result = HtmlSanitizer.sanitizeHtml("<a href=\"artifact/report/index.html\">report</a>"
                + "<a href=\"/job/example/1/console\">console</a>"
                + "<img src=\"artifact/badge.png\">"
                + "<a href=\"https://example.com/report\">absolute</a>");

        assertTrue(result.contains("<a href=\"artifact/report/index.html\">report</a>"));
        assertTrue(result.contains("<a href=\"/job/example/1/console\">console</a>"));
        assertTrue(result.contains("<img src=\"artifact/badge.png\">"));
        assertTrue(result.contains("<a href=\"https://example.com/report\">absolute</a>"));
    }

    @Test
    void removesUnsafeUrlSchemes() {
        String result = HtmlSanitizer.sanitizeHtml("<a href=\"javascript:alert(1)\">js</a>"
                + "<a href=\"JaVaScRiPt:alert(1)\">mixed case</a>"
                + "<img src=\"data:image/svg+xml;base64,PHN2Zz48L3N2Zz4=\">");

        assertFalse(result.toLowerCase(Locale.ROOT).contains("javascript:"));
        assertFalse(result.contains("data:"));
    }
}