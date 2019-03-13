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

import java.io.Serializable;
import java.util.Map;

public class DSEvent implements Serializable {

    String enveloppeId;
    String enveloppeStatus;
    String originalMessage;
    String sender;
    Map<String,String> customfields;

    public DSEvent(String enveloppeId, String enveloppeStatus, String originalMessage,
                   String sender, Map<String, String> customfields) {
        this.enveloppeId = enveloppeId;
        this.enveloppeStatus = enveloppeStatus;
        this.originalMessage = originalMessage;
        this.sender = sender;
        this.customfields = customfields;
    }

    public String getEnveloppeId() {
        return enveloppeId;
    }

    public void setEnveloppeId(String enveloppeId) {
        this.enveloppeId = enveloppeId;
    }

    public String getEnveloppeStatus() {
        return enveloppeStatus;
    }

    public void setEnveloppeStatus(String enveloppeStatus) {
        this.enveloppeStatus = enveloppeStatus;
    }

    public String getOriginalMessage() {
        return originalMessage;
    }

    public void setOriginalMessage(String originalMessage) {
        this.originalMessage = originalMessage;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public Map<String, String> getCustomfields() {
        return customfields;
    }

    public void setCustomfields(Map<String, String> customfields) {
        this.customfields = customfields;
    }
}
