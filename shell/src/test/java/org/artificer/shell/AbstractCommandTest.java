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

import org.artificer.client.ArtificerAtomApiClient;
import org.jboss.aesh.console.AeshConsole;
import org.jboss.aesh.console.AeshConsoleBuilder;
import org.jboss.aesh.console.AeshContext;
import org.jboss.aesh.console.Config;
import org.jboss.aesh.console.command.Command;
import org.jboss.aesh.console.command.registry.AeshCommandRegistryBuilder;
import org.jboss.aesh.console.command.registry.CommandRegistry;
import org.jboss.aesh.console.settings.Settings;
import org.jboss.aesh.console.settings.SettingsBuilder;
import org.jboss.aesh.terminal.TestTerminal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;

import static org.mockito.Mockito.mock;

/**
 * Taken and modified from aesh-example's AeshTestCommons.
 *
 * TODO: This should be replaced after https://issues.jboss.org/browse/AESH-326!
 *
 * @author Brett Meyer.
 */
public class AbstractCommandTest {

    private PipedOutputStream pos;
    private PipedInputStream pis;
    private ByteArrayOutputStream stream;
    private Settings settings;
    private AeshConsole aeshConsole;
    private CommandRegistry registry;

    private ArtificerAtomApiClient clientMock;
    private ArtificerContext aeshContext;

    public AbstractCommandTest() {
        pos = new PipedOutputStream();
        try {
            pis = new PipedInputStream(pos);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        stream = new ByteArrayOutputStream();

        clientMock = mock(ArtificerAtomApiClient.class);

        settings = new SettingsBuilder()
                .terminal(new TestTerminal())
                .inputStream(pis)
                .outputStream(new PrintStream(stream))
                .logging(true)
                .aeshContext(getAeshContext())
                .create();
    }

    protected void prepare(Class<? extends Command>... commands) throws Exception {
        registry = new AeshCommandRegistryBuilder()
                .commands(commands)
                .create();
        AeshConsoleBuilder consoleBuilder = new AeshConsoleBuilder()
                .settings(settings)
                .commandRegistry(registry);
        aeshConsole = consoleBuilder.create();
        aeshConsole.start();
        stream.flush();
    }

    // TODO: Added arguments
    protected void pushToOutput(String command, String... args) throws IOException {
        String literalCommand = String.format(command, args);
        pos.write((literalCommand).getBytes());
        pos.write(Config.getLineSeparator().getBytes());
        pos.flush();
        smallPause();
    }

    protected void smallPause() {
        try {
            Thread.sleep(100);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // TODO: This was not a part of AeshTestCommons, but should be added to it so subclasses can override.  Note the
    protected AeshContext getAeshContext() {
        if (aeshContext == null) {
            aeshContext = new ArtificerContext();
            aeshContext.setClient(clientMock);
        }
        return aeshContext;
    }

    protected ArtificerAtomApiClient getClientMock() {
        return clientMock;
    }
}
