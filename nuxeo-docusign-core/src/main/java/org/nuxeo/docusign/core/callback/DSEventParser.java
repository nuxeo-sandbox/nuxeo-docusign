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

package org.nuxeo.docusign.core.callback;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.jaxen.JaxenException;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.XPath;
import org.jaxen.dom4j.Dom4jXPath;
import org.nuxeo.ecm.core.api.NuxeoException;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DSEventParser {

    public DSEvent parse(InputStream in){

        try {
            Document doc;
            SAXReader reader = new SAXReader();
            doc = reader.read(in);

            //see http://www.edankert.com/defaultnamespaces.html
            HashMap map = new HashMap();
            map.put("edx", "http://www.docusign.net/API/3.0");

            XPath xpath = new Dom4jXPath("//edx:EnvelopeStatus/edx:EnvelopeID");
            xpath.setNamespaceContext(new SimpleNamespaceContext(map));

            //Get Envelope ID
            List<Node> nodes = xpath.selectNodes(doc);
            String envelopeId = nodes.get(0).getText();

            //Get Envelope Status
            xpath = new Dom4jXPath("//edx:EnvelopeStatus/edx:Status");
            xpath.setNamespaceContext(new SimpleNamespaceContext(map));
            nodes = xpath.selectNodes(doc);
            String envelopeStatus = nodes.get(0).getText();

            //Get Sender && other custom fields
            String sender = null;
            Map<String,String> customFields = new HashMap<>();

                // Get Name Node
            xpath = new Dom4jXPath("//edx:EnvelopeStatus/edx:CustomFields/edx:CustomField/edx:Name");
            xpath.setNamespaceContext(new SimpleNamespaceContext(map));
            List<Node> names = xpath.selectNodes(doc);

                // Get Value Node
            xpath = new Dom4jXPath("//edx:EnvelopeStatus/edx:CustomFields/edx:CustomField/edx:Value");
            xpath.setNamespaceContext(new SimpleNamespaceContext(map));
            List<Node> values = xpath.selectNodes(doc);

            for(int i=0;i<names.size();i++) {
                if ("nuxeoUsername".equals(names.get(i).getText())) {
                    sender =values.get(i).getText();
                } else {
                    customFields.put(names.get(i).getText(),values.get(i).getText());
                }
            }

            return new DSEvent(envelopeId, envelopeStatus, doc.asXML(),sender,customFields);
        } catch (JaxenException | DocumentException e) {
            throw new NuxeoException(e);
        }
    }
}
