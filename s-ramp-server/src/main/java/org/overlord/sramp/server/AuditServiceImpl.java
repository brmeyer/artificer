/*
 * Copyright 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.overlord.sramp.server;

import org.jboss.downloads.overlord.sramp._2013.auditing.AuditEntry;
import org.overlord.sramp.repository.AuditManager;
import org.overlord.sramp.repository.audit.AuditEntrySet;
import org.overlord.sramp.repository.audit.AuditEntrySetImplementor;
import org.overlord.sramp.server.core.api.AuditService;

import javax.ejb.Remote;
import javax.ejb.Stateful;

/**
 * @author Brett Meyer.
 */
@Stateful(name = "AuditService")
@Remote(AuditService.class)
public class AuditServiceImpl extends AbstractServiceImpl implements AuditService {

    @Override
    public AuditEntry create(String artifactUuid, AuditEntry auditEntry) throws Exception {
        AuditManager auditManager = auditManager();
        return auditManager.addAuditEntry(artifactUuid, auditEntry);
    }

    @Override
    public AuditEntry get(String artifactUuid, String auditEntryUuid) throws Exception {
        AuditManager auditManager = auditManager();
        return auditManager.getArtifactAuditEntry(artifactUuid, auditEntryUuid);
    }

    @Override
    public AuditEntrySet queryByArtifact(String artifactUuid, Integer startPage, Integer startIndex, Integer count)
            throws Exception {
        AuditManager auditManager = auditManager();
        AuditEntrySetImplementor results = (AuditEntrySetImplementor) auditManager.getArtifactAuditEntries(artifactUuid);
        doPaging(results, startPage, startIndex, count);
        return results;
    }

    @Override
    public AuditEntrySet queryByUser(String username, Integer startPage, Integer startIndex, Integer count)
            throws Exception {
        AuditManager auditManager = auditManager();
        AuditEntrySetImplementor results = (AuditEntrySetImplementor) auditManager.getUserAuditEntries(username);
        doPaging(results, startPage, startIndex, count);
        return results;
    }

    private void doPaging(AuditEntrySetImplementor results, Integer startPage, Integer startIndex, Integer count)
            throws Exception {
        if (startIndex == null && startPage != null) {
            int c = count != null ? count.intValue() : 100;
            startIndex = (startPage.intValue() - 1) * c;
        }
        if (startIndex == null)
            startIndex = 0;
        if (count == null)
            count = 100;
        int startIdx = startIndex;
        int endIdx = startIdx + count - 1;
        results.reduceToPage(startIdx, endIdx);
    }
}
