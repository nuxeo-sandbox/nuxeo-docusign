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

package org.nuxeo.docusign.core.service;

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XObject;
import org.nuxeo.ecm.core.api.NuxeoException;

@XObject("configuration")
public class DocuSignDescriptor {

    @XNode("credentialClass")
    protected Class<? extends DocuSignCredentialFactory> credentialFactoryClass;

    @XNode("basePath")
    protected String basePath;

    @XNode("defaultCallbackChain")
    protected String defaultCallbackChain;


    public DocuSignCredentialFactory getCredentialFactoryClass() {
        try {
            return credentialFactoryClass.getConstructor().newInstance();
        } catch (Exception e) {
            throw new NuxeoException(e);
        }
    }

    public String getBasePath() {
        return basePath;
    }

    public String getDefaultCallbackChain() {
        return defaultCallbackChain;
    }
}
