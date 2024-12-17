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
import org.nuxeo.docusign.core.service.DocuSignService;
import org.nuxeo.docusign.core.test.mock.MockCredentialFactory;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import jakarta.inject.Inject;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@RunWith(FeaturesRunner.class)
@Features({PlatformFeature.class})
@Deploy({
        "nuxeo-docusign-core",
        "org.nuxeo.ecm.platform.oauth",
        "nuxeo-docusign-core:OSGI-INF/test-credential-contrib.xml"
})
public class TestDocuSignService {

    @Inject
    DocuSignService service;

    @Inject
    CoreSession session;

    TestHelpers helper = new TestHelpers();


    @Test
    public void testSendOneBlob() throws Exception {
        Assume.assumeTrue("Test credential not set", MockCredentialFactory.credentialAreSet());

        Blob blob = helper.getBlobs().get(0);
        String id = service.send(
                session,
                Collections.singletonList(blob),
                "Test with one Blob",
                Collections.singletonList(TestHelpers.EMAIL),
                new HashMap<>(),
                null);
        Assert.assertNotNull(id);
    }

    @Test
    public void testSendTwoBlobs() throws Exception {
        Assume.assumeTrue("Test credential not set", MockCredentialFactory.credentialAreSet());

        List<Blob> blobs = helper.getBlobs();
        String id = service.send(
                session,
                blobs,
                "Test with Blobs File",
                Collections.singletonList(TestHelpers.EMAIL),
                new HashMap<>(),
                null);
        Assert.assertNotNull(id);
    }

    @Test
    public void testSendDocs() throws Exception {
        Assume.assumeTrue("Test credential not set", MockCredentialFactory.credentialAreSet());

        DocumentModelList docs = helper.getDocs(session);
        String id = service.send(
                session,
                docs,
                "Test with Two Docs And Two Signers",
                Collections.singletonList(TestHelpers.EMAIL),
                new HashMap<>(),
                null);
        Assert.assertNotNull(id);
    }

    @Test
    public void testSendOneBlobWithCustomCallback() throws Exception {
        Assume.assumeTrue("Test credential not set", MockCredentialFactory.credentialAreSet());

        Blob blob = helper.getBlobs().get(0);
        String id = service.send(
                session,
                Collections.singletonList(blob),
                "Test with one Blob and Custom Callback",
                Collections.singletonList(TestHelpers.EMAIL),
                new HashMap<>(),
                TestHelpers.CALLBACK_URL);
        Assert.assertNotNull(id);
    }

    @Test
    public void testSignedBlobs() throws Exception {
        Assume.assumeTrue("Test credential not set", MockCredentialFactory.credentialAreSet());

        Blob blob = helper.getBlobs().get(0);
        String id = service.send(
                session,
                Collections.singletonList(blob),
                "Test with one Blob",
                Collections.singletonList(TestHelpers.EMAIL),
                new HashMap<>(),
                null);
        Assert.assertNotNull(id);
        List<Blob> blobs = service.getSignedBlobs(session,id);
        Assert.assertEquals("Three Blobs are returned",2,blobs.size());
    }


    @Test
    public void testUpdateDocuments() throws Exception {
        Assume.assumeTrue("Test credential not set", MockCredentialFactory.credentialAreSet());

        DocumentModelList docs = helper.getDocs(session);
        String id = service.send(
                session,
                docs,
                "Test Update Documents",
                Collections.singletonList(TestHelpers.EMAIL),
                new HashMap<>(),
                null);

        session.saveDocuments(docs.toArray(new DocumentModel[0]));
        session.save();

        DocumentModelList updateDocuments = service.updateDocuments(session,id);
        Assert.assertEquals("Documents were updated",docs.size(),updateDocuments.size());
    }

}
