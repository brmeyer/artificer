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

import org.artificer.common.ArtifactType;
import org.artificer.shell.core.AddCommentCommand;
import org.artificer.shell.core.GetMetaDataCommand;
import org.artificer.shell.core.QueryCommand;
import org.artificer.shell.core.RefreshMetaDataCommand;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Comment;

/**
 * @author Brett Meyer.
 */
public class TestCoreCommands extends AbstractCommandTest {

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

    @Test
    public void testAddComment() throws Exception {
        prepare(GetMetaDataCommand.class, RefreshMetaDataCommand.class, AddCommentCommand.class);

        // create artifact and load into shell context using getMetaData
        pushToOutput("getMetaData --uuid %s", artifact.getUuid());

        // setup
        Mockito.when(clientMock.addComment(
                Mockito.anyString(), Mockito.any(ArtifactType.class), Mockito.anyString())).thenReturn(artifact);

        // add comment
        pushToOutput("addComment 'Comment Test'");

        // add comment to mock
        Comment comment = new Comment();
        comment.setText("Comment Test");
        artifact.getComment().add(comment);

        // verify
        Mockito.verify(clientMock).addComment(artifact.getUuid(), artifactType, "Comment Test");
        Assert.assertTrue(stream.toString().contains("Comment successfully created"));
        pushToOutput("refreshMetaData");
        Assert.assertTrue(stream.toString().contains(": Comment Test"));
    }
}
