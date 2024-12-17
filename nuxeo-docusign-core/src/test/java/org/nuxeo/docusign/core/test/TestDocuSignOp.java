/*
 * (C) Copyright 2015-2016 Nuxeo SA (http://nuxeo.com/) and others.
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
 *     Michael Vachette
 */

package org.nuxeo.docusign.core.test;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.docusign.core.automation.DSGetSignedBlobsOp;
import org.nuxeo.docusign.core.automation.DSSendToDocuSignOp;
import org.nuxeo.docusign.core.automation.DSUpdateDocumentOp;
import org.nuxeo.docusign.core.service.DocuSignService;
import org.nuxeo.docusign.core.test.mock.MockCredentialFactory;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationChain;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.util.BlobList;
import org.nuxeo.ecm.automation.core.util.StringList;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import jakarta.inject.Inject;
import java.util.Collections;
import java.util.HashMap;

@RunWith(FeaturesRunner.class)
@Features({AutomationFeature.class})
@Deploy({
        "nuxeo-docusign-core",
        "org.nuxeo.ecm.platform.oauth",
        "nuxeo-docusign-core:OSGI-INF/test-credential-contrib.xml"
})
public class TestDocuSignOp {

    @Inject
    AutomationService as;

    @Inject
    DocuSignService dss;

    @Inject
    CoreSession session;

    TestHelpers helper = new TestHelpers();

    @Test
    public void testSendDoc() throws Exception {

        Assume.assumeTrue("Test credential not set", MockCredentialFactory.credentialAreSet());

        DocumentModel doc = helper.getDocs(session).get(0);
        OperationContext ctx = new OperationContext();
        ctx.setInput(doc);
        ctx.setCoreSession(session);
        OperationChain chain = new OperationChain("TestDocuSignOp");
        chain.add(DSSendToDocuSignOp.ID).
                set("signerEmails",new StringList(new String[]{TestHelpers.EMAIL})).
                set("subject","Automation Test").
                set("contextVariable","myVariable");
        as.run(ctx, chain);
        Assert.assertNotNull(ctx.get("myVariable"));
    }

    @Test
    public void testGetSignedBlobs() throws Exception {

        Assume.assumeTrue("Test credential not set", MockCredentialFactory.credentialAreSet());

        DocumentModel doc = helper.getDocs(session).get(0);
        String envelopeId = dss.send(
                session,
                new DocumentModelListImpl(Collections.singletonList(doc)),
                "Test Automation Get Signed Blobs",
                Collections.singletonList(TestHelpers.EMAIL),
                new HashMap<>(),
                null);
        session.saveDocument(doc);

        OperationContext ctx = new OperationContext();
        ctx.setCoreSession(session);
        OperationChain chain = new OperationChain("TestDocuSignOp");
        chain.add(DSGetSignedBlobsOp.ID).set("envelopeId",envelopeId);
        BlobList blobs = (BlobList) as.run(ctx, chain);
        Assert.assertNotNull(blobs);
        Assert.assertEquals(2,blobs.size());
    }


    @Test
    public void testUpdateDocuments() throws Exception {

        Assume.assumeTrue("Test credential not set", MockCredentialFactory.credentialAreSet());

        DocumentModel doc = helper.getDocs(session).get(0);
        String envelopeId = dss.send(
                session,
                new DocumentModelListImpl(Collections.singletonList(doc)),
                "Test Automation Update Documents",
                Collections.singletonList(TestHelpers.EMAIL),
                new HashMap<>(),
                null);
        session.saveDocument(doc);

        OperationContext ctx = new OperationContext();
        ctx.setCoreSession(session);
        OperationChain chain = new OperationChain("TestDocuSignOp");
        chain.add(DSUpdateDocumentOp.ID).set("envelopeId",envelopeId);
        DocumentModelList docs = (DocumentModelList) as.run(ctx, chain);
        Assert.assertNotNull(docs);
        //Assert.assertEquals(1,docs.size());
    }

}
