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

import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModelList;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface DocuSignService {

    /** Send the blobs to DocuSign for eletronic signature
     *
     * @param session
     * @param blobs the blobs to sign
     * @param subject the subject of the email sent to signers
     * @param signerEmails the list of signer emails
     * @param customFields the list of custom fields to add in the request
     * @param callbackUrl the callback URL that DocuSign will use to notify the system
     * @return the envelope ID (DocuSign ID)
     * @throws Exception
     */
    String send(CoreSession session, List<Blob> blobs, String subject,
                List<String> signerEmails, Map<String,String> customFields,
                String callbackUrl) throws Exception;


    /** Send the blobs to DocuSign for eletronic signature
     *
     * @param session
     * @param docs the document to sign. The main file (file:content)
     * @param subject the subject of the email sent to signers
     * @param signerEmails the list of signer emails
     * @param customFields the list of custom fields to add in the request
     * @param callbackUrl the callback URL that DocuSign will use to notify the system
     * @return the envelope ID (DocuSign ID)
     * @throws Exception
     */
    String send(CoreSession session, DocumentModelList docs, String subject,
                List<String> signerEmails, Map<String,String> customFields,
                String callbackUrl) throws Exception;


    /** Get the signed documents from an envelope
     *
     * @param session
     * @param envelopeId The docuSign ID
     * @return a list of pdf blobs
     * @throws Exception
     */
    List<Blob> getSignedBlobs(CoreSession session, String envelopeId) throws Exception;


    /** Update the documents corresponding to the envelope ID (get signed versions, ...)
     *
     * @param session
     * @param envelopeId The docuSign ID
     * @return The list of updated documents
     * @throws Exception
     */
    DocumentModelList updateDocuments(CoreSession session, String envelopeId) throws Exception;


    /**
     *
     * @param stream the content of the event sent by DocuSign
     * @param chainName the automation chain to run
     */
    void handleCallbackEvent(InputStream stream,String chainName);


}
