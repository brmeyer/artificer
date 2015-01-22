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
package org.overlord.sramp.server.core.api;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.StoredQuery;

import java.util.List;

/**
 * @author Brett Meyer.
 */
public interface QueryService extends AbstractService {

    public List<BaseArtifactType> query(String query) throws Exception;

    public PagedResult<BaseArtifactType> query(String query, Integer startPage, Integer startIndex, Integer count,
            String orderBy, Boolean ascending) throws Exception;

    public StoredQuery createStoredQuery(StoredQuery storedQuery) throws Exception;

    public void updateStoredQuery(String queryName, StoredQuery storedQuery) throws Exception;

    public StoredQuery getStoredQuery(String queryName) throws Exception;

    public List<StoredQuery> getStoredQueries() throws Exception;

    public void deleteStoredQuery(String queryName) throws Exception;
}
