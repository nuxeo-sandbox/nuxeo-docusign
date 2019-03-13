package org.nuxeo.docusign.core.test;

import org.apache.commons.io.IOUtils;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
import org.nuxeo.ecm.core.api.impl.blob.AbstractBlob;
import org.nuxeo.ecm.core.api.impl.blob.ByteArrayBlob;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TestHelpers {

    public static final String EMAIL = System.getProperty("DSrecipient");
    public static final String CALLBACK_URL = System.getProperty("DScallback");

    // Test Helpers

    protected List<Blob> getBlobs() throws IOException {
        List<Blob> blobs = new ArrayList<>();
        {
            AbstractBlob blob = new ByteArrayBlob(
                    IOUtils.toByteArray(getClass().getResourceAsStream("/files/nyc.jpg")));
            blob.setFilename("nyc.jpg");
            blobs.add(blob);
        }
        {
            AbstractBlob blob = new ByteArrayBlob(
                    IOUtils.toByteArray(getClass().getResourceAsStream("/files/document.docx")));
            blob.setFilename("document.docx");
            blobs.add(blob);
        }
        return blobs;
    }

    protected DocumentModelList getDocs(CoreSession session) throws IOException {
        List<Blob> blobs = getBlobs();
        DocumentModelList docs = new DocumentModelListImpl();
        for (Blob blob: blobs) {
            DocumentModel doc = session.createDocumentModel("File");
            doc.setPropertyValue("file:content", (Serializable) blob);
            doc = session.createDocument(doc);
            docs.add(doc);
        }
        return docs;
    }
}
