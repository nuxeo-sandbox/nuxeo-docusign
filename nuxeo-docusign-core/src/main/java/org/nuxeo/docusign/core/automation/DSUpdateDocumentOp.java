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
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModelList;


@Operation(
        id = DSUpdateDocumentOp.ID,
        category = Constants.CAT_SERVICES,
        label = "DocuSign: Update Documents",
        description = "Get the signed blobs for the given envelope ID and set those in the corresponding documents")
public class DSUpdateDocumentOp {

    public static final String ID = "DSUpdateDocumentOp";

    @Context
    protected CoreSession session;

    @Context
    protected OperationContext ctx;

    @Context
    protected DocuSignService service;

    @Param(
            name = "envelopeId",
            description= "The DocuSign envelope ID",
            required = true)
    protected String envelopeId;

    @OperationMethod
    public DocumentModelList run() throws Exception {
        return service.updateDocuments(session,envelopeId);
    }

}