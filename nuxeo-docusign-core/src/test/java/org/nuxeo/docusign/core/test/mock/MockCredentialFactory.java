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

package org.nuxeo.docusign.core.test.mock;

import com.docusign.esign.client.ApiClient;
import org.nuxeo.docusign.core.service.DocuSignCredentialFactory;


public class MockCredentialFactory implements DocuSignCredentialFactory {

    @Override
    public void setCredential(ApiClient client, String username) {
        // initialize client for desired environment and add X-DocuSign-Authentication header
        username = System.getProperty("DSusername");
        String password = System.getProperty("DSpassword");
        String integratorKey = System.getProperty("DSintegratorKey");

        // configure 'X-DocuSign-Authentication' authentication header
        String authHeader =
                "{\"Username\":\"" + username + "\",\"Password\":\"" +
                        password + "\",\"IntegratorKey\":\"" + integratorKey + "\"}";
        client.addDefaultHeader("X-DocuSign-Authentication", authHeader);
    }


    public static boolean credentialAreSet() {
        String username = System.getProperty("DSusername");
        return username!=null && username.length()>0;
    }

}
