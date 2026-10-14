# About

This plugin provides an integration between the Nuxeo Platform and <a href="https://docusign.com">DocuSign</a>. Documents are seamlessly sent to DocuSign for electronic signature. The plugin also supports DocuSign webhooks.

# Important Note

For the plugin to work without conflicting with `jackson` libraries deployed with Nuxeo, the marketplace package forces the installation of the `jackson-datatype-joda` library, required by DocuSign.

See `assembly.xml` at `nuxeo-docusign-marketplace/src/main/assemble`

# Requirements

Building requires the following software:

- git
- maven

Usage requires a valid, publicly accessible domain name for the Nuxeo instance. Be sure to apply it to `nuxeo.url` in `nuxeo.conf`:

```
nuxeo.url=https://<NUXEO_SERVER>/nuxeo
```

# Build

Note: you can install this plug-in directly [from the Marketplace](https://connect.nuxeo.com/nuxeo/site/marketplace/package/nuxeo-docusign). If you would like to build locally:

```
git clone https://github.com/nuxeo-sandbox/nuxeo-docusign
cd nuxeo-docusign
mvn clean install

# Optionally provide DocuSign credentials to exercise the live integration tests
# (without them, those integration tests are skipped)
mvn clean install -Dnuxeo-docusign-username=MY_USERNAME -Dnuxeo-docusign-password=MY_PASSWORD -Dnuxeo-docusign-integratorKey=MY_KEY -Dnuxeo-docusign-recipient=RECIPIENT_EMAIL -Dnuxeo-docusign-callback=MY_URL
```

# Install

If building locally, the Nuxeo Package is in `nuxeo-docusign-package/target/`; install it with `nuxeoctl mp-install`.

# Design Concepts

The main container for a DocuSign request is an **envelope**. An envelope can contain one or more files and can have one or more signers. DocuSign assigns the envelope an ID and assigns each file a position within the envelope.

When a document is sent with `SendToDocuSign`, the plug-in adds the `Docusign` facet to the document. This facet contributes the `docusign` schema, whose fields are used to associate the Nuxeo document with the DocuSign envelope and to store the signed result:

| Field | Description |
| --- | --- |
| `ds:envid` | The DocuSign envelope ID. |
| `ds:envposition` | The document's position within the envelope. This is used to match a returned signed file to the original document. |
| `ds:sender` | The username of the Nuxeo user or group associated with the request. The callback uses it to restore the user context while processing the result. |
| `ds:signedblob` | The signed file returned by DocuSign. |
| `ds:certificate` | Not currently used. |

The operations do not save documents. The caller must save the documents after sending them and after applying the signed blobs. If the document is not saved after sending, the facet and envelope metadata required to retrieve the signed result will not persist.

The plug-in also includes a page provider named `DocuSignEnvelopeDoc`, which locates documents that need to be updated during callback processing.

# Operations

The plug-in exposes some new Automation Operations:

## Services > SendToDocuSign

This operation sends the input to DocuSign and stores the generated envelope ID in the automation context variable named by `contextVariable`.

(note: this operation does not save the document)

Inputs:

* `signerEmails`: a list of email addresses for the signers.
* `subject`: the subject of the notification email sent to the signers.
* `contextVariable`: the name of the automation context variable where the generated envelope ID will be stored.
* `customFields` (optional): key/value properties added to the DocuSign envelope.
* `callbackUrl`: the URL DocuSign calls when the envelope status is `completed`.

Tip: `callbackUrl` should point to an Automation Chain or Automation Script that can handle the DocuSign callback; you can use `nuxeo.url` to construct the URL:

```
@{Env["nuxeo.url"]}/site/docusign/javascript.myCallbackScript
```

## Services > DSUpdateDocumentOp

This operation retrieves the signed blobs from DocuSign. It takes an envelope ID as input, downloads the signed blobs for that envelope, and updates the corresponding Nuxeo documents.

It returns the list of updated documents but does not save them.

(note: the caller must save the returned documents)

## javascript.dsDefaultCallback

This Automation Script is an example callback handler. To be clear: it is NOT called by default! You must wire it via the `callbackUrl` (see below).

This script takes care of saving the signed documents.

# Callback Handling

DocuSign calls the url provided in `callbackUrl` when the envelope status is `completed`. The callback URL should include the ID of an Automation Chain or Automation Script. This plugin includes an example Automation Script named `javascript.dsDefaultCallback` that you can use.

## Callback Parameters

* `params.envelopeId`: the DocuSign envelope ID.
* `params.envelopeStatus`: the status reported by DocuSign, such as `Completed`, `Declined`, or `Voided`.
* `params.envelopeSender`: the Nuxeo username associated with the request.
* `params.customFields`: the key/value properties supplied when the envelope was sent.

Additionally a context variable is set; the main use is to access the callback XML:

* `event`: contains a `DSEvent` object with:
  * `enveloppeId`: the DocuSign envelope ID.
  * `enveloppeStatus`: the envelope status.
  * `originalMessage`: the original DocuSign callback XML.
  * `sender`: the Nuxeo username from the `nuxeoUsername` custom field.
  * `customfields`: the other custom fields supplied with the envelope.

## Callback URL

To use the example callback, `javascript.dsDefaultCallback`, set `callbackUrl` to:

```
@{Env["nuxeo.url"]}/site/docusign/javascript.dsDefaultCallback
```

(you can use a shorthand form as well, `@{Env["nuxeo.url"]}/site/docusign`)

To use a custom Automation Chain or Automation Script, append its name to the callback endpoint. For example:

```
@{Env["nuxeo.url"]}/site/docusign/javascript.myCallbackScript
```

# Usage

A common use case for the DocuSign integration is as part of a workflow. A typical implementation involves the following:

* A workflow with a task that will send the document to DocuSign
* An automation chain/script that calls `Services > SendToDocuSign`
* An automation chain/script that handles the callback from DocuSign

## Example Automation Chain for Sending to DocuSign

```yaml
- Context.FetchDocument
- SendToDocuSign:
    signerEmails: "@{NodeVariables[\"signers\"]}"
    subject: "@{NodeVariables[\"subject\"]}"
    contextVariable: envelopeId
    callbackUrl: "@{Env[\"nuxeo.url\"]}/site/docusign/javascript.myCallbackScript"
    customFields:
      processId: "@{Context[\"workflowInstanceId\"]}"
      documentId: "@{Document.id}"
- Document.Save
```

## Example Automation Script for Handling the Callback

This example completes a workflow task along with storing the signed documents.

Nb. If you don't need the callback to complete a workflow task (or other custom logic), you can use the example callback instead by setting `callbackUrl` as described above.

```javascript
function run(input, params) {

  Auth.LoginAs(input, {
    'name': params.envelopeSender
  });

  // Get the document
  input = Repository.GetDocument(input, {
    'value': params.customFields.documentId
  });

  // Get the workflow task
  var query = "SELECT * FROM Document WHERE ecm:mixinType = 'Task' AND ecm:currentLifeCycleState NOT IN ('ended','cancelled') AND ecm:isProxy =0 AND nt:processId ='" + params.customFields.processId + "'";

  input = Repository.Query(input, {
    'query': query
  });

  if (params.envelopeStatus === 'Completed') {
    WorkflowTask.Complete(input, {
      'status': params.envelopeStatus
    });

    var docs = DSUpdateDocumentOp(input, {
      'envelopeId': params.envelopeId
    });

    Document.Save(docs, {});
  }
}
```

# Runtime Setup

Please refer to the included Word doc [DocusignSetup.docx](DocuSign-Setup.docx) for instructions on the runtime/end-user configuration.

# Support

**These features are not part of the Nuxeo Production platform.**

These solutions are provided for inspiration and we encourage customers to use them as code samples and learning resources.

This is a moving project (no API maintenance, no deprecation process, etc.) If any of these solutions are found to be useful for the Nuxeo Platform in general, they will be integrated directly into platform, not maintained here.

# License

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)

# About Nuxeo

More information is available at [https://www.hyland.com/en/solutions/products/nuxeo-platform](https://www.hyland.com/en/solutions/products/nuxeo-platform).
