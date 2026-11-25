# Rich Text Publisher Plugin

Now featuring pipeline plugin support.

**Security notice:** [**Stored XSS vulnerability**](https://www.jenkins.io/security/advisory/2022-06-30/#SECURITY-2332) is fixed in version 1.6 and later. The Jenkins Plugin Site warning may remain visible until its security metadata is updated.

A plugin adds a configurable post-build step which publishes arbitrary rich text at the Project and Build pages.
The following markup languages are supported: HTML, WikiText, Confluence. Build parameters or whole files from workspace may be embedded into text.

Copyright (c) 2013 Dmitry Korotkov. See LICENSE for further details.

# Usage

This plugin publishes rich text message for each build. Message for the latest build is also displayed on job main page.

Just add post-build action **Publish rich text message**, pick formatting language and enter message text.

Click **Advanced** button to configure separate messages for unstable and failed builds.

## Embedding build-time data into text messages

Build parameters, environment variables and files may be embedded into published message. After the message is published it will not change even if configured templates reference some files from workspace. All embedding is performed in build time, not in display time. Configuration changes do not affect previous builds either.

Build parameters/variables may be added into the text by using `${VARIABLE_NAME}` placeholders.

Environment variables can be embedded by using `${ENV:ENVIRONMENT_VAR_NAME}` format.

There are two supported placeholders for embedding files: `${FILE:FILENAME}` and `${FILE_SL:FILENAME}`.

The difference is that the first one embeds file as is and the second one skips all CR and LF characters (line breaks).

In both cases file contents are rendered using the same markup language as a part of the whole text.

File names are relative to the workspace.

## Publishing more than once in a build

Every `rtp` call and every Rich Text Publisher post-build step, adds its own rich text to the build. To show only the
latest text instead, for example to update a report while the build runs, set `replace`:

```groovy
rtp parserName: 'HTML', stableText: 'Step 2 of 3 done', replace: true
```

Rich Text Publisher 1.5 and 1.6 always replaced rich text published earlier in the same build
([JENKINS-72140](https://issues.jenkins.io/browse/JENKINS-72140)).

## Safe HTML formatting

Starting with Version 1.6, published rich text is sanitized by default. Scripts, event handlers, `javascript:` links, iframes and most inline
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

## Posting rich text messages on the Dashboard

Jenkins Dashboard View is provided by the Dashboard View plugin.

Rich Text Publisher plugin adds two types of dashboard portlets: "Rich Text" and "Rich text published within a build".

The first one publishes arbitrary rich text.

The other one publishes current rich text message for the specified build.

The latest build and the latest stable build may be configured as rich text source.

Note that "Rich text published within a build" portlet assumes that the build has "Publish rich text message" post-build step configured and executed at least once. Otherwise you will see empty portlet.

## Screenshots

### Job main page

![Job main page](https://raw.githubusercontent.com/jenkins-infra/plugins-wiki-docs/master/rich-text-publisher-plugin/docs/images/jobmain-highlighted.png)

### Build page

![Build page](https://raw.githubusercontent.com/jenkins-infra/plugins-wiki-docs/master/rich-text-publisher-plugin/docs/images/build-highlighted.png)

### Configuration

![Configuration](https://raw.githubusercontent.com/jenkins-infra/plugins-wiki-docs/master/rich-text-publisher-plugin/docs/images/config-highlighted.png)

### Dashboard View

![Dashboard View](https://raw.githubusercontent.com/jenkins-infra/plugins-wiki-docs/master/rich-text-publisher-plugin/docs/images/dashboard-highlighted.png)

## Change Log

### 1.8

- Fixed `nullAction` when its value is provided through a Pipeline variable rather than a string literal.

### 1.7

- Every `rtp` call and every Rich Text Publisher post-build step adds its own rich text to the build again, as before 1.5. (JENKINS-72140)
- Added "Replace rich text published earlier in the same build" option.
- Added Pipeline support for `replace: true`.
- Pipelines that call `rtp` more than once now show every published text by default.
- Use `replace: true` to keep the 1.5 and 1.6 behavior.

### 1.6

- Rich text is now sanitized by default, fixing SECURITY-2332 (CVE-2022-34786).
- Messages stored by builds before the upgrade are sanitized when displayed too.
- The rich text dashboard portlet escapes its error messages.
- Sanitizing keeps common formatting, tables (including border styles), absolute and relative links, and images.
- Scripts, event handlers, `javascript:` and `data:` URLs, iframes, `<font>`, CSS classes, `target` attributes, and non-border style are removed.
- Added administrative option **Allow unsafe HTML** to restore the previous rendering behavior when required.
- Requires Jenkins 2.479.3 or newer and Java 17 or newer.

### 1.5

- Added support for `Run#addOrReplaceAction()`.
- JENKINS-61336: Made Dashboard View integration optional.
- Updated POM references to use HTTPS URLs.
- Added Jenkinsfile.
- Upgraded plugin parent and build configuration.

### 1.4

- Added pipeline support.

### 1.3

- Updated Jenkins core version.

### 1.2

- Added support for Dashboard View.
- Arbitrary and build-specific text may be displayed on the dashboard.

### 1.1

- Bumped up mylyn library version.
- Now more Confluence tags are supported.

### 1.0

- First version released.
