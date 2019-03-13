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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.docusign.core.service.DocuSignService;
import org.nuxeo.ecm.webengine.model.WebObject;
import org.nuxeo.ecm.webengine.model.impl.ModuleRoot;
import org.nuxeo.runtime.api.Framework;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;

/**
 * WebEngine module to handle the Docusign callback
 */
@Path("/docusign")
@WebObject(type = "docusign")
public class DSCallbackObject extends ModuleRoot {

    protected static final Log log = LogFactory.getLog(DSCallbackObject.class);

    @Context
    private HttpServletRequest request;

    @Path("/")
    @POST
    public Object doPost(@Context HttpServletRequest request) throws IOException {
        InputStream in = request.getInputStream();
        DocuSignService service = Framework.getService(DocuSignService.class);
        service.handleCallbackEvent(in,null);
        return Response.status(Response.Status.OK).build();
    }

    @Path("/{chain}")
    @POST
    public Object doPost(@Context HttpServletRequest request, @PathParam("chain") String chain)
            throws IOException {
        InputStream in = request.getInputStream();
        DocuSignService service = Framework.getService(DocuSignService.class);
        service.handleCallbackEvent(in,chain);
        return Response.status(Response.Status.OK).build();
    }


}
