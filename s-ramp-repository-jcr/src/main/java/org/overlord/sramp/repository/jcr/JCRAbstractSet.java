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
package org.overlord.sramp.repository.jcr;

import org.overlord.sramp.repository.AbstractSetImplementor;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.query.QueryResult;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Brett Meyer.
 */
public abstract class JCRAbstractSet implements AbstractSetImplementor {
    protected Session session;
    protected QueryResult jcrQueryResult;

    protected Iterator<Node> nodes;

    protected long totalSize;
    protected long startIndex;
    protected long pageSize;

    public JCRAbstractSet(Session session, QueryResult jcrQueryResult) throws Exception {
        this.session = session;
        this.jcrQueryResult = jcrQueryResult;

        NodeIterator jcrNodes = jcrQueryResult.getNodes();
        nodes = jcrNodes;
        // Do this here, rather than in #size().  If #page is called, jcrNodes may be replaced.
        totalSize = jcrNodes.getSize();
        // init
        pageSize = totalSize;
    }

    @Override
    public long size() {
        return totalSize;
    }

    @Override
    public void reduceToPage(long startIndex, long endIndex) throws Exception {
        this.startIndex = startIndex;

        NodeIterator jcrNodes = jcrQueryResult.getNodes();
        // Get only the rows we're interested in.
        List<Node> pagedNodes = new ArrayList<Node>();
        int i = 0;
        while (jcrNodes.hasNext()) {
            Node node = jcrNodes.nextNode();
            if (i >= startIndex && i <= endIndex) {
                pagedNodes.add(node);
            }
            i++;
        }

        pageSize = pagedNodes.size();
        nodes = pagedNodes.iterator();
    }

    @Override
    public long pageSize() {
        return pageSize;
    }

    @Override
    public long startIndex() {
        return startIndex;
    }

    @Override
    public void close() {
        JCRRepositoryFactory.logoutQuietly(session);
    }
}
