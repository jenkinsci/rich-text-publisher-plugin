package org.korosoft.jenkins.plugin.rtp;

/*

The New BSD License

Copyright (c) 2011-2013, Dmitry Korotkov
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

- Redistributions of source code must retain the above copyright notice, this
  list of conditions and the following disclaimer.

- Redistributions in binary form must reproduce the above copyright notice, this
  list of conditions and the following disclaimer in the documentation and/or
  other materials provided with the distribution.

- Neither the name of the Jenkins RuSalad Plugin nor the names of its
  contributors may be used to endorse or promote products derived from this
  software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 */

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.plugins.view.dashboard.DashboardPortlet;
import hudson.util.ListBoxModel;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.HttpResponse;
import org.korosoft.jenkins.plugin.rtp.parser.HtmlSanitizer;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Rich text publisher
 *
 * @author Dmitry Korotkov
 * @since 1.0
 */
public class StaticTextPortlet extends DashboardPortlet {
    private static final Log log = LogFactory.getLog(StaticTextPortlet.class);
    private String text;
    private String richText;
    private String parserName;

    private transient MarkupParser markupParser;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        if (parserName != null) {
            this.richText = getMarkupParser().parse(text);
        }
    }

    public String getRichText() {
        return HtmlSanitizer.sanitize(richText);
    }

    public String getParserName() {
        return parserName;
    }

    public void setParserName(String parserName) {
        if (parserName == null || !DescriptorImpl.markupParsers.containsKey(parserName)) {
            parserName = "HTML";
        }
        this.parserName = parserName;
        this.markupParser = DescriptorImpl.markupParsers.get(parserName);
        if (text != null) {
            this.richText = getMarkupParser().parse(text);
        }
    }

    @DataBoundConstructor
    public StaticTextPortlet(String name, String text, String parserName) {
        super(name);
        this.text = text;
        setParserName(parserName);
        this.richText = getMarkupParser().parse(text);
    }

    private MarkupParser getMarkupParser() {
        if (markupParser == null) {
            markupParser = DescriptorImpl.markupParsers.get(parserName);
        }
        return markupParser;
    }

    @Extension(optional = true)
    public static final class DescriptorImpl extends Descriptor<DashboardPortlet> {

        private static transient Map<String, MarkupParser> markupParsers;
        private static transient List<String> markupParserNames;

        static {
            loadParsers();
        }

        private static void loadParsers() {
            Properties properties = new Properties();
            InputStream stream = DescriptorImpl.class.getResourceAsStream("/parsers.properties");
            try {
                properties.load(stream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                IOUtils.closeQuietly(stream);
            }

            markupParsers = new HashMap<String, MarkupParser>();
            markupParserNames = new ArrayList<String>();
            for (Object o : properties.values()) {
                try {
                    MarkupParser parser = (MarkupParser) Class.forName(o.toString()).newInstance();
                    String name = parser.getName();
                    markupParserNames.add(name);
                    markupParsers.put(name, parser);
                } catch (Exception e) {
                    log.error(e);
                }
            }
        }

        public HttpResponse doFillParserNameItems() {
            loadParsers();
            ListBoxModel model = new ListBoxModel();
            for (String name : markupParserNames) {
                model.add(name, name);
            }
            return model;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return Messages.richText();
        }

    }

}
