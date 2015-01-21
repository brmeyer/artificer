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
package org.overlord.sramp.repository;

/**
 * Additional methods needed by implementors of the result sets.
 *
 * @author Brett Meyer
 */
public interface AbstractSetImplementor extends AbstractSet {

    /**
     * After the query has been executed, this is called by the REST/EJB service to page the results.
     */
    public void reduceToPage(long startIndex, long endIndex) throws Exception;

}
