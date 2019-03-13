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

package org.nuxeo.docusign.core.service.auth;

import com.docusign.esign.client.ApiClient;
import com.google.api.client.auth.oauth2.Credential;
import org.nuxeo.docusign.core.service.DocuSignCredentialFactory;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.platform.oauth2.providers.OAuth2ServiceProvider;
import org.nuxeo.ecm.platform.oauth2.providers.OAuth2ServiceProviderRegistry;
import org.nuxeo.runtime.api.Framework;


public class Oauth2CredentialFactory implements DocuSignCredentialFactory {

    @Override
    public void setCredential(ApiClient client, String username) {
        OAuth2ServiceProviderRegistry registry =
                Framework.getService(OAuth2ServiceProviderRegistry.class);
        OAuth2ServiceProvider provider = registry.getProvider("docusign");
        if (provider==null) throw new NuxeoException("Cannot find DocuSign Oauth2 provider");
        Credential credential = provider.loadCredential(username);
        if (credential==null) throw new NuxeoException("Cannot find DocuSign Credential for "+username);
        client.addDefaultHeader("Authorization", "Bearer " + credential.getAccessToken());
    }

}
