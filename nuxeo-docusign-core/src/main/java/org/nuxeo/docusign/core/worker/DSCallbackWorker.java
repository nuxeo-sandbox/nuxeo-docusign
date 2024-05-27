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


package org.nuxeo.docusign.core.worker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nuxeo.docusign.core.callback.DSEvent;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationChain;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.work.AbstractWork;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.transaction.TransactionHelper;

public class DSCallbackWorker extends AbstractWork {

    private static final Logger log = LogManager.getLogger(DSCallbackWorker.class);

    private DSEvent event;
    private String chainName;

    public DSCallbackWorker(DSEvent event, String chainName) {
        super();
        this.event = event;
        this.chainName = chainName;
    }

    @Override
    public void work() {

        setProgress(Progress.PROGRESS_INDETERMINATE);
        setStatus("inProgress");

        if (!TransactionHelper.isTransactionActive()) {
            startTransaction();
        }

        openSystemSession();
        AutomationService as = Framework.getService(AutomationService.class);
        OperationContext octx = new OperationContext();
        octx.setCoreSession(session);
        octx.put("event",event);
        OperationChain chain = new OperationChain("DocuSignCallbackProcessingChain");
        chain.add(chainName).
                set("envelopeId",event.getEnveloppeId()).
                set("envelopeStatus",event.getEnveloppeStatus()).
                set("envelopeSender",event.getSender()).
                set("customFields",new Properties(event.getCustomfields()));
        try {
            as.run(octx, chain);
        } catch (OperationException e) {
            throw new NuxeoException(e);
        }

        TransactionHelper.commitOrRollbackTransaction();
        closeSession();

        setStatus("Done");
    }

    @Override
    public String getTitle() {
        return "DocusignCallbackWorker";
    }
}
