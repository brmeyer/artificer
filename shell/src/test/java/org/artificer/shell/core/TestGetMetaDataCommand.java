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

import org.artificer.shell.AbstractCommandTest;
import org.junit.Test;

import java.util.UUID;

import static org.mockito.Mockito.verify;

/**
 * @author Brett Meyer.
 */
public class TestGetMetaDataCommand extends AbstractCommandTest {

    @Test
    public void testGetMetaData() throws Exception {
        prepare(GetMetaDataCommand.class);

        String uuid = UUID.randomUUID().toString();

        pushToOutput("getMetaData --uuid %s", uuid);
        verify(getClientMock()).getArtifactMetaData(uuid);
    }
}
