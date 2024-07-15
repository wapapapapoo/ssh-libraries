/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package one.iolab.app.sshdconfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.sshd.common.RuntimeSshException;
import org.apache.sshd.common.SshConstants;
import org.apache.sshd.common.SshException;
import org.apache.sshd.common.util.GenericUtils;
import org.apache.sshd.common.util.logging.AbstractLoggingBean;
import org.apache.sshd.server.auth.keyboard.InteractiveChallenge;
import org.apache.sshd.server.auth.keyboard.KeyboardInteractiveAuthenticator;
import org.apache.sshd.server.auth.keyboard.PromptEntry;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.preauth.PreAuthenticator;
import org.apache.sshd.server.session.ServerSession;

/**
 * Provides a default implementation for
 * {@link KeyboardInteractiveAuthenticator} where it prompts for the password.
 *
 * @author <a href="mailto:dev@mina.apache.org">Apache MINA SSHD Project</a>
 */
public class MyKeyboardInteractiveAuthenticator
        extends AbstractLoggingBean
        implements KeyboardInteractiveAuthenticator {

    public MyKeyboardInteractiveAuthenticator() {
        super();
    }

    private void disconnectImmediately(ServerSession session, String message) {
        try {
            session.disconnect(SshConstants.SSH2_DISCONNECT_HOST_AUTHENTICATION_FAILED, message);
        } catch (IOException e) {
            session.close(true);
        }
    }

    @Override
    public InteractiveChallenge generateChallenge(
            ServerSession session, String username, String lang, String subMethods)
            throws Exception {

        PreAuthenticator preAuth = new MyPreAuthenticator();

        List<PromptEntry> prompts = new ArrayList<>();

        try {
            preAuth.authenticate(username, session, (String str) -> {
                prompts.add(new PromptEntry(str, true));
            });
        } catch (Exception e) {
            warn("doPreAuth({}@{}) failed ({}) to process preauth: {}",
                    username, session, e.getClass().getSimpleName(), e.getMessage(), e);
            return null;
        }

        InteractiveChallenge challenge = new InteractiveChallenge();
        challenge.setInteractionName("");
        challenge.setInteractionInstruction("");
        challenge.setLanguageTag("zh-CN");
        for (PromptEntry prompt : prompts) {
            challenge.addPrompt(prompt);
        }
        return challenge;
    }

    @Override
    public boolean authenticate(ServerSession session, String username, List<String> responses) throws Exception {
        PasswordAuthenticator auth = new MyPasswordAuthenticator();

        int numResp = GenericUtils.size(responses);
        if (numResp == 0) {
            throw new SshException("Mismatched number of responses");
        }

        try {
            if (auth.authenticate(username, responses.get(0), session)) {
                return true;
            } else {
                disconnectImmediately(session, "\r\nAuthenticate Failed.");
                return false;
            }
        } catch (Error e) {
            warn("authenticate({})[{}] failed ({}) to consult password authenticator: {}",
                    session, username, e.getClass().getSimpleName(), e.getMessage(), e);
            throw new RuntimeSshException(e);
        }
    }
}
