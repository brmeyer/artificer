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
package org.artificer.shell.core;

import org.artificer.atom.ArtificerAtomUtils;
import org.artificer.client.query.QueryResultSet;
import org.artificer.common.ArtifactType;
import org.artificer.common.ArtificerConstants;
import org.artificer.common.query.ArtifactSummary;
import org.artificer.shell.AbstractCommandTest;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.atom.Feed;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;

import java.util.UUID;

/**
 * @author Brett Meyer.
 */
public class TestGetMetaDataCommand extends AbstractCommandTest {

    @Test
    public void testGetMetaDataByUuid() throws Exception {
        prepare(GetMetaDataCommand.class);

        BaseArtifactType artifact = createArtifact();

        pushToOutput("getMetaData --uuid %s", artifact.getUuid());
        Mockito.verify(getClientMock()).getArtifactMetaData(artifact.getUuid());
        Assert.assertTrue(getStream().toString().contains("Type: FooType"));
        Assert.assertTrue(getStream().toString().contains("Model: ext"));
        Assert.assertTrue(getStream().toString().contains("UUID: " + artifact.getUuid()));
    }

    @Test
    public void testGetMetaDataByFeed() throws Exception {
        prepare(GetMetaDataCommand.class, QueryCommand.class);

        BaseArtifactType artifact = createArtifact();

        pushToOutput("query /s-ramp");
        System.out.println(getStream().toString());
        pushToOutput("getMetaData --feed %d", 1);
        Mockito.verify(getClientMock()).getArtifactMetaData(ArtifactType.valueOf(artifact), artifact.getUuid());
        Assert.assertTrue(getStream().toString().contains("Type: FooType"));
        Assert.assertTrue(getStream().toString().contains("Model: ext"));
        Assert.assertTrue(getStream().toString().contains("UUID: " + artifact.getUuid()));
    }

    // TODO: Generalize and move to the superclass!
    private BaseArtifactType createArtifact() throws Exception {
        String uuid = UUID.randomUUID().toString();

        BaseArtifactType artifact = ArtifactType.ExtendedArtifactType("FooType", false).newArtifactInstance();
        artifact.setUuid(uuid);

        // called by getMetaData
        Mockito.when(getClientMock().getArtifactMetaData(Mockito.contains(uuid))).thenReturn(artifact);
        Mockito.when(getClientMock().getArtifactMetaData(
                Mockito.any(ArtifactType.class), Mockito.contains(uuid))).thenReturn(artifact);

        // called by query (mock out a feed)
        ArtifactSummary summary = new ArtifactSummary();
        summary.setUuid(uuid);
        summary.setModel("ext");
        summary.setType("FooType");
        Feed feed = new Feed();
        feed.getExtensionAttributes().put(ArtificerConstants.SRAMP_ITEMS_PER_PAGE_QNAME, String.valueOf(1));
        feed.getExtensionAttributes().put(ArtificerConstants.SRAMP_START_INDEX_QNAME, String.valueOf(1));
        feed.getExtensionAttributes().put(ArtificerConstants.SRAMP_TOTAL_RESULTS_QNAME, String.valueOf(1));
        Entry entry = ArtificerAtomUtils.wrapArtifactSummary(summary);
        feed.getEntries().add(entry);
        QueryResultSet queryResult = new QueryResultSet(feed);
        Mockito.when(getClientMock().query(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(),
                Mockito.anyBoolean())).thenReturn(queryResult);

        return artifact;
    }
}
