/*
 * Copyright 2013 JBoss Inc
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
package org.overlord.sramp.repository.jcr.audit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.query.QueryResult;

import org.jboss.downloads.overlord.sramp._2013.auditing.AuditEntry;
import org.overlord.sramp.repository.audit.AuditEntrySet;
import org.overlord.sramp.repository.audit.AuditEntrySetImplementor;
import org.overlord.sramp.repository.jcr.JCRAbstractSet;
import org.overlord.sramp.repository.jcr.JCRRepositoryFactory;
import org.overlord.sramp.repository.jcr.mapper.JCRNodeToAuditEntryFactory;

/**
 * JCR implementation of the {@link AuditEntrySet} interface.  This implementation iterates over
 * a set of JCR nodes.  Each node must be an audit:auditEntry JCR node.
 * @author eric.wittmann@redhat.com
 */
public class JCRAuditEntrySet extends JCRAbstractSet implements AuditEntrySetImplementor, Iterator<AuditEntry> {

    /**
     * Constructor.
     * @param session
     * @param jcrQueryResult
     */
    public JCRAuditEntrySet(Session session, QueryResult jcrQueryResult) throws Exception {
        super(session, jcrQueryResult);
    }

    /**
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<AuditEntry> iterator() {
        return this;
    }

    /**
     * @see java.util.Iterator#next()
     */
    @Override
    public AuditEntry next() {
        Node jcrNode = nodes.next();
        return JCRNodeToAuditEntryFactory.createAuditEntry(session, jcrNode);
    }

    @Override
    public boolean hasNext() {
        return nodes.hasNext();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

}
