package org.apache.sshd.server.auth.preauth;

import org.apache.sshd.server.auth.AsyncAuthException;
import org.apache.sshd.server.auth.password.PasswordChangeRequiredException;
import org.apache.sshd.server.session.ServerSession;

public interface PreAuthenticator {

    @FunctionalInterface
    public interface AddPromptLambda {
        public void add(String str);
    }

    public boolean authenticate(String username, ServerSession session,
            AddPromptLambda addPrompt)
            throws PasswordChangeRequiredException, AsyncAuthException;

}
