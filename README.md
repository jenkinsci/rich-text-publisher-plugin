# Rich Text Publisher Plugin

Now featuring pipeline plugin support

A plugin adds a configurable post-build step which publishes arbitrary rich text at the Project and Build pages.
The following markup languages are supported: HTML, WikiText, Confluence. Build parameters or whole files from workspace may be embedded into text.

Copyright (c) 2013 Dmitry Korotkov. See LICENSE for further details.

## Publishing more than once in a build

Every `rtp` call, and every Rich Text Publisher post-build step, adds its own rich text to the build. To show only the
latest text instead, for example to update a report while the build runs, set `replace`:

```groovy
rtp parserName: 'HTML', stableText: 'Step 2 of 3 done', replace: true
```

Rich Text Publisher 1.5 and 1.6 always replaced rich text published earlier in the same build
([JENKINS-72140](https://issues.jenkins.io/browse/JENKINS-72140)).

## Safe HTML formatting

Published rich text is sanitized by default. Scripts, event handlers, `javascript:` links, iframes and most inline
styling are removed. Common formatting, tables (including border styles), absolute and relative links, and images are
kept. `<font>`, `class`, `target` and `style` (except border styles on table elements) are stripped, so existing
reports may look different after upgrading.

The sanitizer also applies to messages stored by builds that ran before the upgrade.

To render rich text as is, an administrator can enable **Allow unsafe HTML** in **Manage Jenkins > Security**.
This lets anyone who can configure a job run JavaScript in the browser of anyone viewing the build
([SECURITY-2332](https://www.jenkins.io/security/advisory/2022-06-30/#SECURITY-2332)), so only enable it if every
user who can configure jobs is trusted.

With Configuration as Code:

```yaml
security:
  richTextPublisher:
    allowUnsafeHtml: true
```
