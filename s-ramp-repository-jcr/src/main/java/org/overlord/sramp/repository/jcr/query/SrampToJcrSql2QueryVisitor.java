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

import org.overlord.sramp.common.SrampException;
import org.overlord.sramp.common.query.xpath.visitors.XPathVisitor;

import javax.jcr.query.Query;

/**
 * Visitor used to produce a JCR SQL2 query from an S-RAMP xpath query.
 *
 * @author eric.wittmann@redhat.com
 */
public interface SrampToJcrSql2QueryVisitor extends XPathVisitor {

    public void setOrder(String order);

    public void setOrderAscending(boolean orderAscending);

    public void setLimitCount(int limitCount);

    public void setLimitOffset(int limitOffset);

    public Query buildQuery() throws SrampException;

}