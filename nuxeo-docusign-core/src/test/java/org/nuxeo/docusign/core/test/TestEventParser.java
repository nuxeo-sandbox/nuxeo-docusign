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
package org.nuxeo.docusign.core.test;

import org.junit.Assert;
import org.junit.Test;
import org.nuxeo.docusign.core.callback.DSEvent;
import org.nuxeo.docusign.core.callback.DSEventParser;
import org.nuxeo.ecm.core.api.NuxeoException;

import java.io.InputStream;


public class TestEventParser {

    @Test
    public void testParseOK() {
        InputStream stream = getClass().getResourceAsStream("/files/callback.xml");
        DSEventParser parser = new DSEventParser();
        DSEvent event = parser.parse(stream);
        Assert.assertEquals("id","a5d3a19b-9c45-4997-bbac-a0970a2a1b49",event.getEnveloppeId());
        Assert.assertEquals("status","Completed",event.getEnveloppeStatus());
        Assert.assertEquals("sender","Administrator",event.getSender());
        Assert.assertEquals("customFields size",1,event.getCustomfields().size());
        Assert.assertEquals("customFields content","TestValue",event.getCustomfields().get("TestName"));
    }

    @Test
    public void testParseNOK() {
        InputStream stream = getClass().getResourceAsStream("This is not XML");
        DSEventParser parser = new DSEventParser();
        try {
            parser.parse(stream);
            Assert.fail();
        } catch (NuxeoException e) {
            Assert.assertTrue(true);
        }
    }
}