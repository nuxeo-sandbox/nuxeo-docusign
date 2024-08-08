# About

This plugin provides an integration between the Nuxeo Platform and <a href="https://docusign.com">DocuSign</a>. Documents are seamlessly sent to DocuSign for electronic signature. The plugin also supports DocuSign webhooks.

# Important Note

For the plugin to work without conflicting with `jackson` libraries deployed with Nuxeo, the marketplace package forces the installaiton of the `jackson-datatype-joda` library, required by DocuSign.

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
# To run the tests, need to provide credentials
mvn clean install -Dnuxeo-docusign-username=MY_USERNAME -Dnuxeo-docusign-password=MY_PASSWORD -Dnuxeo-docusign-integratorKey=MY_KEY -Dnuxeo-docusign-recipient=RECIPIENT_EMAIL -Dnuxeo-docusign-callback=MY_URL
# Or skip the tests
mvn clean install -DskipTests
```

# Install

If building locally, the Nuxeo Package is in `nuxeo-docusign-marketplace/target/`

To install:

```
cd <nuxeo folder>/bin
./nuxeoctl mp-install -s <path-to-target-folder>/nuxeo-docusign-marketplace-<X.X>-SNAPSHOT.zip
```

Where `<X.X>` is the matching version that you built.

# Design Concepts

* The main container for the docusign request is called an "envelope". You may put one or several files in an envelope that must be signed by one or several signers
* The envelope has an ID and each file has a position (1,2 ...)
* The envelope ID and position are stored in the `docusign` schema for each document sent
* The plug-in adds a facet name `Docusign` to any document that will be sent to DocuSign (via the operation below). It adds the `docusign` schema to the document, which contains the necessary fields for the integration.
* The plug-in adds a page provider named `DocuSignEnvelopeDoc`. This page provider is used in order to locate documents that need to be updated (e.g. during the callback from Docusign after signing is complete)

# Operations

The plug-in exposes some new Automation Operations:

## Services > SendToDocuSign

(note: this operation does not save the document)

* contextVariable: the name of the context variable where the envelope ID will be stored.
* signerEmails: the signers email addresses
* Subject: the subject of the notification email sent to signers
* CallbackUrl: the url used by DocuSign to notify Nuxeo when the document have been signed, voided or declined:
 * The last part is the name of the automation chain to run
@{Env["nuxeo.url"]}site/docusign/javascript.wf_handle_callback
 * The chain will be provided with several params:
  * envelopeId
  * envelopeStatus
  * envelopeSender
  * customFields
  * event (the XML string sent by DocuSign)
* customFields: a list of key/value that DocuSign will add to callback messages (very convenient to store the context of the request, for example pass document and workflow task id's here)

## Services > DSUpdateDocumentOp

(note: this operation does not save the document)

This operation is used to retrieve the signed blobs from docusign. It takes an envelope ID as input, downloads the signed blobs of that envelope, and updates the corresponding Nuxeo documents.

Returns the list of updated documents.

# Usage

## Configuration

A typical implementation involves the following:

* A workflow with a task that will send the document to DocuSign
* Corresponding automation chain/script to use Services > SendToDocuSign, for example:

```yaml
- Context.FetchDocument
- SendToDocuSign:
    contextVariable: envelopeId
    signerEmails: "@{NodeVariables[\"signers\"]}"
    subject: "@{NodeVariables[\"subject\"]}"
    callbackUrl: "@{Env[\"nuxeo.url\"]}site/docusign/javascript.wf_Docusign_HandleCallback"
    customFields:
      processId: "@{Context[\"workflowInstanceId\"]}"
      documentId: "@{Document.id}"
- Document.Save
```

* Corresponding automation chain/script that will be called by DocuSign when the signing is complete
  * This automation should call Services > DSUpdateDocumentOp to retrieve the result
  * For example:

```javascript
function run(input, params) {

  Auth.LoginAs(input, {
    'name': params.envelopeSender
  });

  //Get Document
  input = Repository.GetDocument(input, {
    'value': params.customFields.documentId
  });

  //Get Workflow Task
  var query = "SELECT * FROM Document WHERE ecm:mixinType = 'Task' AND ecm:currentLifeCycleState NOT IN ('ended','cancelled') AND ecm:isProxy =0 AND nt:processId ='" + params.customFields.processId + "'";

  input = Repository.Query(input, {
    'query': query
  });

  WorkflowTask.Complete(input, {
    'status': params.envelopeStatus
  });

  var docs = DSUpdateDocumentOp(input, {
    'envelopeId': params.envelopeId
  });

  Document.Save(docs, {});
}
```

## Runtime Setup

Please refer to the included Word doc [DocusignSetup.docx](DocuSign-Setup.docx) for instructions on the runtime/end-user configuration.

# Support

**These features are not part of the Nuxeo Production platform.**

These solutions are provided for inspiration and we encourage customers to use them as code samples and learning resources.

This is a moving project (no API maintenance, no deprecation process, etc.) If any of these solutions are found to be useful for the Nuxeo Platform in general, they will be integrated directly into platform, not maintained here.

# License

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)

# About Nuxeo

Nuxeo Platform is an open source Content Services platform, written in Java. Data can be stored in both SQL & NoSQL databases.

The development of the Nuxeo Platform is mostly done by Nuxeo employees with an open development model.

The source code, documentation, roadmap, issue tracker, testing, benchmarks are all public.

Typically, Nuxeo users build different types of information management solutions for [document management](https://www.nuxeo.com/solutions/document-management/), [case management](https://www.nuxeo.com/solutions/case-management/), and [digital asset management](https://www.nuxeo.com/solutions/dam-digital-asset-management/), use cases. It uses schema-flexible metadata & content models that allows content to be repurposed to fulfill future use cases.

More information is available at [www.nuxeo.com](https://www.nuxeo.com).

