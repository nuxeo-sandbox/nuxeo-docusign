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

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.restapi.test.RestServerFeature;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.ServletContainerFeature;

import javax.inject.Inject;
import java.io.IOException;

@RunWith(FeaturesRunner.class)
@Features({AutomationFeature.class, RestServerFeature.class})
@RepositoryConfig(cleanup = Granularity.METHOD)
@Deploy({
        "nuxeo-docusign-core",
        "org.nuxeo.ecm.platform.oauth",
        "nuxeo-docusign-core:OSGI-INF/test-chain-contrib.xml"
})
public class TestDocuSignCallback {

    public static final String BASE_URL = "http://localhost";

    @Inject
    protected ServletContainerFeature servletContainerFeature;

    @Test
    public void testReceiveCallback() throws IOException {
        int port = servletContainerFeature.getPort();
        String url = BASE_URL + ":"+port+"/docusign";
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(url);
        post.setEntity(new InputStreamEntity(getClass().getResourceAsStream("/files/callback.xml")));
        HttpResponse response = client.execute(post);
        Assert.assertEquals(200,response.getStatusLine().getStatusCode());
    }

    @Test
    public void testReceiveCallbackWithURLParam() throws IOException {
        int port = servletContainerFeature.getPort();
        String url = BASE_URL + ":"+port+"/docusign/javascript.workflow";
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(url);
        post.setEntity(new InputStreamEntity(getClass().getResourceAsStream("/files/callback.xml")));
        HttpResponse response = client.execute(post);
        Assert.assertEquals(200,response.getStatusLine().getStatusCode());
    }
}
