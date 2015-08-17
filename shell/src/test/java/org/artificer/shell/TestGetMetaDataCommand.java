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
package org.artificer.shell;

import org.artificer.shell.AbstractCommandTest;
import org.artificer.shell.core.GetMetaDataCommand;
import org.artificer.shell.core.QueryCommand;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Brett Meyer.
 */
public class TestGetMetaDataCommand extends AbstractCommandTest {

    @Test
    public void testGetMetaDataByUuid() throws Exception {
        prepare(GetMetaDataCommand.class);

        pushToOutput("getMetaData --uuid %s", artifact.getUuid());
        Mockito.verify(clientMock).getArtifactMetaData(artifact.getUuid());
        Assert.assertTrue(stream.toString().contains("Type: FooType"));
        Assert.assertTrue(stream.toString().contains("Model: ext"));
        Assert.assertTrue(stream.toString().contains("UUID: " + artifact.getUuid()));
    }

    @Test
    public void testGetMetaDataByFeed() throws Exception {
        prepare(GetMetaDataCommand.class, QueryCommand.class);

        pushToOutput("query /s-ramp");
        pushToOutput("getMetaData --feed %d", 1);
        Mockito.verify(clientMock).getArtifactMetaData(artifactType, artifact.getUuid());
        Assert.assertTrue(stream.toString().contains("Type: " + artifactType.getType()));
        Assert.assertTrue(stream.toString().contains("Model: " + artifactType.getModel()));
        Assert.assertTrue(stream.toString().contains("UUID: " + artifact.getUuid()));
    }
}
