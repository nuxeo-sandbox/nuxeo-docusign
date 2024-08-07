/*
 * (C) Copyright 2016 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *
 */


package org.nuxeo.docusign.core.service;

import com.docusign.esign.api.AuthenticationApi;
import com.docusign.esign.api.EnvelopesApi;
import com.docusign.esign.client.ApiClient;
import com.docusign.esign.model.*;
import com.google.api.client.util.Base64;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nuxeo.docusign.core.adapter.DSAdapter;
import org.nuxeo.docusign.core.callback.DSEvent;
import org.nuxeo.docusign.core.callback.DSEventParser;
import org.nuxeo.docusign.core.worker.DSCallbackWorker;
import org.nuxeo.ecm.core.api.*;
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
import org.nuxeo.ecm.core.api.impl.blob.AbstractBlob;
import org.nuxeo.ecm.core.api.impl.blob.ByteArrayBlob;
import org.nuxeo.ecm.core.work.api.WorkManager;
import org.nuxeo.ecm.platform.query.api.PageProvider;
import org.nuxeo.ecm.platform.query.api.PageProviderService;
import org.nuxeo.ecm.platform.query.nxql.CoreQueryDocumentPageProvider;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.api.login.NuxeoLoginContext;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.DefaultComponent;

import java.io.InputStream;
import java.io.Serializable;
import java.util.*;
import java.util.List;

public class DocuSignServiceImpl extends DefaultComponent implements DocuSignService {

    private static final Logger log = LogManager.getLogger(DocuSignServiceImpl.class);

    protected static final String CONFIG_EXT_POINT = "configuration";

    protected DocuSignDescriptor config = null;


    @Override
    public void registerContribution(Object contribution, String extensionPoint,
                                     ComponentInstance contributor) {
        if (CONFIG_EXT_POINT.equals(extensionPoint)) {
            config = (DocuSignDescriptor) contribution;
        }
    }

    @Override
    public void unregisterContribution(Object contribution, String extensionPoint,
                                       ComponentInstance contributor) {
    }

    @Override
    public String send(CoreSession session, List<Blob> blobs, String subject,
                       List<String> signerEmails, Map<String,String> customFields,
                       String callbackUrl) throws Exception {

        ApiClient client = getClient(session);
        AuthenticationApi authApi = new AuthenticationApi(client);
        LoginInformation loginInfo = authApi.login();
        // parse first account ID (user might belong to multiple accounts)
        String accountId = loginInfo.getLoginAccounts().get(0).getAccountId();

        // create a new envelope to manage the signature request
        EnvelopeDefinition envDef = new EnvelopeDefinition();
        envDef.setEmailSubject(subject);

        //Set Signers
        List<Signer> signers = new ArrayList<>();
        {
            int i=1;
            for (String email : signerEmails) {
                Signer signer = new Signer();
                signer.setName(email);
                signer.setEmail(email);
                signer.setRecipientId(""+i++);
                signers.add(signer);
            }
        }

        Recipients recipients = new Recipients();
        recipients.setSigners(signers);
        envDef.setRecipients(recipients);

        //Set Documents
        List<Document> documents = new ArrayList<>();
        {
            int i = 1;
            for (Blob blob : blobs) {
                String base64 = Base64.encodeBase64String(IOUtils.toByteArray(blob.getStream()));
                documents.add(
                        new Document().name(blob.getFilename()).documentId("" + i++).
                                documentBase64(base64).fileExtension(
                                FilenameUtils.getExtension(blob.getFilename())));
            }
        }
        envDef.setDocuments(documents);

        //set sender username && custom fields
        TextCustomField usernameField = new TextCustomField();
        usernameField.setName("nuxeoUsername");
        usernameField.setValue(session.getPrincipal().getName());

        List<TextCustomField> customFieldList = new ArrayList<>();
        customFieldList.add(usernameField);
        for (Map.Entry<String, String> entry : customFields.entrySet()) {
            TextCustomField customField = new TextCustomField().name(entry.getKey()).value(entry.getValue());
            customFieldList.add(customField);
        }

        CustomFields fields = new CustomFields();
        fields.setTextCustomFields(customFieldList);
        envDef.setCustomFields(fields);

        // send the envelope by setting |status| to "sent". To save as a draft set to "created"
        envDef.setStatus("sent");

        //Configure Event Notification
        if (callbackUrl!=null && callbackUrl.length()>0) {
            EventNotification notification = new EventNotification();
            notification.setUrl(callbackUrl);
            notification.setEnvelopeEvents(
                    Collections.singletonList(new EnvelopeEvent().envelopeEventStatusCode("completed").includeDocuments("false")));
            notification.setLoggingEnabled("true");
            notification.setRequireAcknowledgment("true");
            envDef.setEventNotification(notification);
        }

        // instantiate a new EnvelopesApi object
        EnvelopesApi envelopesApi = new EnvelopesApi(client);
        // call the createEnvelope() API
        EnvelopeSummary envelopeSummary = envelopesApi.createEnvelope(accountId, envDef);
        return envelopeSummary.getEnvelopeId();
    }

    @Override
    public String send(CoreSession session, DocumentModelList docs,
                       String subject, List<String> signerEmails,  Map<String,String> customFields,
                       String callbackUrl) throws Exception {
        List<Blob> blobs = new ArrayList<>();
        for(DocumentModel doc: docs) {
            Blob blob = (Blob) doc.getPropertyValue("file:content");
            if (blob!=null) blobs.add(blob);
        }
        String envelopeId = send(session,blobs,subject,signerEmails,customFields,callbackUrl);

        CoreInstance.doPrivileged(session.getRepositoryName(), (CoreSession s) -> {
            int position =1;
            for(DocumentModel doc: docs) {
                Blob blob = (Blob) doc.getPropertyValue("file:content");
                if (blob!=null) {
                    doc.addFacet(DSAdapter.FACET);
                    DSAdapter adapter = doc.getAdapter(DSAdapter.class);
                    adapter.setEnvelopeId(envelopeId);
                    adapter.setEnvelopePosition(position++);
                    adapter.setSender(session.getPrincipal().getName());
                    s.saveDocument(doc);
                }
            }
            s.save();
        });
        /*
        for(DocumentModel doc: docs) {
            Blob blob = (Blob) doc.getPropertyValue("file:content");
            if (blob!=null) {
                doc.addFacet(DSAdapter.FACET);
                DSAdapter adapter = doc.getAdapter(DSAdapter.class);
                adapter.setEnvelopeId(envelopeId);
                adapter.setEnvelopePosition(position++);
                adapter.setSender(session.getPrincipal().getName());
                session.saveDocument(doc);
                
            }
        }*/

        return envelopeId;
    }

    @Override
    public List<Blob> getSignedBlobs(CoreSession session, String envelopeId) throws Exception {
        //set Client
        ApiClient client = getClient(session);
        EnvelopesApi envelopesApi = new EnvelopesApi(client);

        // parse first account ID (user might belong to multiple accounts)
        AuthenticationApi authApi = new AuthenticationApi(client);
        LoginInformation loginInfo = authApi.login();
        String accountId = loginInfo.getLoginAccounts().get(0).getAccountId();

        //Get list of Docs
        EnvelopeDocumentsResult envelopeDocs = envelopesApi.listDocuments(accountId,envelopeId);

        //fetch signed version
        List<Blob> blobs = new ArrayList<>();
        for(EnvelopeDocument doc : envelopeDocs.getEnvelopeDocuments()) {
            byte[] bytes = envelopesApi.getDocument(accountId,envelopeId,doc.getDocumentId());
            AbstractBlob blob = new ByteArrayBlob(bytes,"application/pdf");
            blob.setFilename(doc.getDocumentId());
            blobs.add(blob);
        }
        return blobs;
    }

    @Override
    public DocumentModelList updateDocuments(CoreSession session, String envelopeId) throws Exception {

        //get Documents
        PageProviderService pps = Framework.getService(PageProviderService.class);
        Map<String, Serializable> props = new HashMap<>();
        props.put(CoreQueryDocumentPageProvider.CORE_SESSION_PROPERTY, (Serializable) session);
        PageProvider<DocumentModel> pp =
                (PageProvider<DocumentModel>)
                        pps.getPageProvider("DocuSignEnvelopeDoc", null, 50L, 0L, props,envelopeId);
        List<DocumentModel> docs = pp.getCurrentPage();

        if (docs.size()==0) {
            return new DocumentModelListImpl();
        }

        //get Blobs
        List<Blob> blobs = getSignedBlobs(session,envelopeId);
        if (blobs.size()==0) {
            throw new NuxeoException("Could noy fetch blobs for envelopeId "+envelopeId);
        }

        Map<String,Blob> blobsByName = new HashMap<>();
        for (Blob blob: blobs) {
            blobsByName.put(blob.getFilename(),blob);
        }

        //update docs;
        for (DocumentModel doc: docs) {
            Blob original = (Blob) doc.getPropertyValue("file:content");
            String originalFilename = original.getFilename();
            String targetFilename =  FilenameUtils.removeExtension(originalFilename)+".pdf";
            DSAdapter adapter = doc.getAdapter(DSAdapter.class);
            long position = adapter.getEnvelopePosition();
            Blob blob = blobsByName.get(""+position);
            blob.setFilename(targetFilename);
            adapter.setSignedBlob(blob);
        }

        return new DocumentModelListImpl(docs);
    }

    @Override
    public void handleCallbackEvent(InputStream stream,String chainName) {
        DSEventParser parser = new DSEventParser();
        DSEvent event = parser.parse(stream);
        WorkManager wm = Framework.getService(WorkManager.class);
        if (chainName==null) chainName = config.getDefaultCallbackChain();
        wm.schedule(new DSCallbackWorker(event,chainName));
    }

    private ApiClient getClient(CoreSession session) {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(config.getBasePath());
        //Set Credentials
        config.getCredentialFactoryClass().setCredential(apiClient,session.getPrincipal().getName());
        return apiClient;
    }

}

