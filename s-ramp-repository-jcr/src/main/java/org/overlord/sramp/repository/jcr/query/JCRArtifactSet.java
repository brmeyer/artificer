/*
 * Copyright 2012 JBoss Inc
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
package org.overlord.sramp.repository.jcr.query;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.overlord.sramp.repository.jcr.JCRAbstractSet;
import org.overlord.sramp.repository.jcr.JCRNodeToArtifactFactory;
import org.overlord.sramp.repository.query.ArtifactSet;
import org.overlord.sramp.repository.query.ArtifactSetImplementor;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.query.QueryResult;
import java.util.Iterator;

/**
 * A JCR implementation of an {@link ArtifactSet}.
 *
 * @author eric.wittmann@redhat.com
 */
public class JCRArtifactSet extends JCRAbstractSet implements ArtifactSetImplementor, Iterator<BaseArtifactType> {

	private boolean logoutOnClose = true;

	/**
	 * Constructor.
	 * @param session
	 * @param jcrQueryResult
	 */
	public JCRArtifactSet(Session session, QueryResult jcrQueryResult) throws Exception {
		super(session, jcrQueryResult);
	}

    /**
     * Constructor.
     * @param session
     * @param jcrQueryResult
     * @param logoutOnClose
     */
    public JCRArtifactSet(Session session, QueryResult jcrQueryResult, boolean logoutOnClose) throws Exception {
        this(session, jcrQueryResult);
        this.logoutOnClose = logoutOnClose;
    }

	/**
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<BaseArtifactType> iterator() {
		return this;
	}

	@Override
	public void close() {
	    if (logoutOnClose)
	        super.close();
	}

	/**
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
		return nodes.hasNext();
	}

	/**
	 * @see java.util.Iterator#next()
	 */
	@Override
	public BaseArtifactType next() {
        Node jcrNode = nodes.next();
		return JCRNodeToArtifactFactory.createArtifact(this.session, jcrNode);
	}

	/**
	 * @see java.util.Iterator#remove()
	 */
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
