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
package org.overlord.sramp.ui.server.servlets;

import org.overlord.sramp.ui.server.api.KeycloakBearerTokenAuthenticationProvider;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Brett Meyer.
 */
public abstract class AbstractServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // set by hidden form field
        setBearerToken(req.getParameter("Authorization"));
    }

    public void setBearerToken(String bearerToken) {
        KeycloakBearerTokenAuthenticationProvider.setBearerToken(bearerToken);
    }

    protected void cleanup() {
        KeycloakBearerTokenAuthenticationProvider.clearBearerToken();
    }
}
