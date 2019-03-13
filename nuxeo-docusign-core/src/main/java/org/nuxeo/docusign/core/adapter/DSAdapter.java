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

package org.nuxeo.docusign.core.adapter;

import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;

import java.io.Serializable;

public class DSAdapter {

    public static final String FACET = "Docusign";
    public static final String ENVELOPE_ID_XPATH = "ds:envid";
    public static final String ENVELOPE_POSITION_XPATH = "ds:envposition";
    public static final String ENVELOPE_SENDER_XPATH = "ds:sender";
    public static final String SIGNED_BLOB_XPATH = "ds:signedblob";

    private DocumentModel doc;

    public DSAdapter(DocumentModel doc) {
        this.doc = doc;
    }

    public String getEnvelopeId() {
        return (String) doc.getPropertyValue(ENVELOPE_ID_XPATH);
    }

    public long getEnvelopePosition() {
        return (long) doc.getPropertyValue(ENVELOPE_POSITION_XPATH);
    }

    public Blob getSignedBlob() {
        return (Blob) doc.getPropertyValue(SIGNED_BLOB_XPATH);
    }

    public String getSender() {
        return (String) doc.getPropertyValue(ENVELOPE_SENDER_XPATH);
    }

    public void setEnvelopeId(String envelopeId) {
        doc.setPropertyValue(ENVELOPE_ID_XPATH, envelopeId);
    }

    public void setEnvelopePosition(long position) {
        doc.setPropertyValue(ENVELOPE_POSITION_XPATH, position);
    }

    public void setSignedBlob(Blob blob) {
        doc.setPropertyValue(SIGNED_BLOB_XPATH, (Serializable) blob);
    }

    public void setSender(String sender) {
        doc.setPropertyValue(ENVELOPE_SENDER_XPATH, sender);
    }

}
