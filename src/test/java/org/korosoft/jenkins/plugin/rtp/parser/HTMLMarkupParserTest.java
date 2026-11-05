package org.korosoft.jenkins.plugin.rtp.parser;

import org.junit.Assert;
import org.junit.Test;
import org.korosoft.jenkins.plugin.rtp.BuildRichTextAction;

import java.util.Locale;

public class HTMLMarkupParserTest {
    @Test
    public void removesUnsafeMarkupAndPreservesFormatting() {
        String result = HtmlSanitizer.sanitizeHtml("<p><b>Safe formatting</b></p>"
                + "<table border='1' cellpadding='4' cellspacing='0' style='border-collapse:collapse;border:1px solid #000;"
                + "background-image:url(javascript:alert(1))'><tr><td border='1' style='border:1px solid red'>cell</td></tr></table>"
                + "<script>alert('xss')</script><img src=x onerror=alert('xss')>"
                + "<a href='javascript:alert(1)'>unsafe link</a>");

        Assert.assertTrue(result.contains("<b>Safe formatting</b>"));
        Assert.assertTrue(result.contains("border=\"1\""));
        Assert.assertTrue(result.contains("cellpadding=\"4\""));
        Assert.assertTrue(result.contains("cellspacing=\"0\""));
        Assert.assertTrue(result.contains("border-collapse:collapse"));
        Assert.assertTrue(result.contains("border:1px solid #000"));
        Assert.assertTrue(result.contains("border:1px solid red"));
        Assert.assertTrue(result.contains("<td border=\"1\""));
        Assert.assertTrue(result.contains(">cell</td>"));
        Assert.assertFalse(result.contains("background-image"));
        Assert.assertFalse(result.contains("<script"));
        Assert.assertFalse(result.contains("alert('xss')"));
        Assert.assertFalse(result.contains("onerror"));
        Assert.assertFalse(result.contains("javascript:"));
    }

    @Test
    public void sanitizesPreviouslyStoredBuildMessagesByDefault() {
        BuildRichTextAction action = new BuildRichTextAction(null,
                "<b>Safe formatting</b><script>alert('xss')</script>");

        Assert.assertEquals("<b>Safe formatting</b>", action.getRichText());
    }

    @Test
    public void keepsRelativeLinksAndImages() {
        String result = HtmlSanitizer.sanitizeHtml("<a href=\"artifact/report/index.html\">report</a>"
                + "<a href=\"/job/example/1/console\">console</a>"
                + "<img src=\"artifact/badge.png\">"
                + "<a href=\"https://example.com/report\">absolute</a>");

        Assert.assertTrue(result.contains("<a href=\"artifact/report/index.html\">report</a>"));
        Assert.assertTrue(result.contains("<a href=\"/job/example/1/console\">console</a>"));
        Assert.assertTrue(result.contains("<img src=\"artifact/badge.png\">"));
        Assert.assertTrue(result.contains("<a href=\"https://example.com/report\">absolute</a>"));
    }

    @Test
    public void removesUnsafeUrlSchemes() {
        String result = HtmlSanitizer.sanitizeHtml("<a href=\"javascript:alert(1)\">js</a>"
                + "<a href=\"JaVaScRiPt:alert(1)\">mixed case</a>"
                + "<img src=\"data:image/svg+xml;base64,PHN2Zz48L3N2Zz4=\">");

        Assert.assertFalse(result.toLowerCase(Locale.ROOT).contains("javascript:"));
        Assert.assertFalse(result.contains("data:"));
    }
}