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

package org.nuxeo.docusign.core.automation;

import org.nuxeo.docusign.core.service.DocuSignService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.util.BlobList;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.automation.core.util.StringList;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;

import java.util.Arrays;


@Operation(
        id = DSSendToDocuSignOp.ID,
        category = Constants.CAT_SERVICES,
        label = "DocuSign: Send To DocuSign",
        description = "Send the document to DocuSign for electronic signature")
public class DSSendToDocuSignOp {

    public static final String ID = "SendToDocuSign";

    @Context
    protected CoreSession session;

    @Context
    protected OperationContext ctx;

    @Context
    protected DocuSignService service;

    @Param(
            name = "signerEmails",
            description= "A StringList of email addresses",
            required = true)
    protected StringList signerEmails;

    @Param(
            name = "subject",
            description= "The subject of the email sent to the signers",
            required = true)
    protected String subject;

    @Param(
            name = "contextVariable",
            description= "The name of the context variable where the envelope ID will be stored",
            required = true)
    protected String contextVariable;

    @Param(
            name = "customFields",
            description= "Custom Fields to be added to the envelope",
            required = false)
    protected Properties customFields = new Properties();

    @Param(
            name = "callbackUrl",
            description= "The callback URL DocuSign will use for this envelope",
            required = false)
    protected String callbackUrl;

    @OperationMethod
    public DocumentModel run(DocumentModel doc) throws Exception {
        DocumentModelList docs = new DocumentModelListImpl();
        docs.add(doc);
        String envelopeId = service.send(session,docs,subject, signerEmails,
                customFields,callbackUrl);
        ctx.put(contextVariable,envelopeId);
        return doc;
    }

    @OperationMethod
    public DocumentModelList run(DocumentModelList docs) throws Exception {
        String envelopeId = service.send(session,docs,subject, signerEmails,
                customFields,callbackUrl);
        ctx.put(contextVariable,envelopeId);
        return docs;
    }

    @OperationMethod
    public Blob run(Blob blob) throws Exception {
        String envelopeId = service.send(session, Arrays.asList(blob),subject,
                signerEmails,customFields,callbackUrl);
        ctx.put(contextVariable,envelopeId);
        return blob;
    }

    @OperationMethod
    public BlobList run(BlobList blobs) throws Exception {
        String envelopeId = service.send(session,blobs,subject, signerEmails,
                customFields,callbackUrl);
        ctx.put(contextVariable,envelopeId);
        return blobs;
    }

}