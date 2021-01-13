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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.docusign.core.adapter.DSAdapter;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.query.api.PageProvider;
import org.nuxeo.ecm.platform.query.api.PageProviderService;
import org.nuxeo.ecm.platform.query.nxql.CoreQueryDocumentPageProvider;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(FeaturesRunner.class)
@Features({PlatformFeature.class})
@Deploy("nuxeo-docusign-core")
public class TestEnvelopePageProvider {

    @Inject
    CoreSession session;

    @Test
    public void testPageProvider() {
        final String envelopeId = "testId";

        DocumentModel doc = session.createDocumentModel("File");
        doc.addFacet(DSAdapter.FACET);
        doc = session.createDocument(doc);

        DSAdapter adapter = doc.getAdapter(DSAdapter.class);
        adapter.setEnvelopeId(envelopeId);
        doc = session.saveDocument(doc);
        session.save();

        adapter = doc.getAdapter(DSAdapter.class);
        Assert.assertEquals(envelopeId,adapter.getEnvelopeId());

        PageProviderService pps = Framework.getService(PageProviderService.class);
        Map<String, Serializable> props = new HashMap<>();
        props.put(CoreQueryDocumentPageProvider.CORE_SESSION_PROPERTY, (Serializable) session);
        PageProvider<DocumentModel> pp =
                (PageProvider<DocumentModel>)
                        pps.getPageProvider("DocuSignEnvelopeDoc", null, 50L, 0L, props,envelopeId);
        List<DocumentModel> docs = pp.getCurrentPage();

        Assert.assertEquals("One document is returned",1,docs.size());

    }

}
