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
package org.overlord.sramp.ui.client.shared.services;

import org.jboss.errai.enterprise.client.jaxrs.api.interceptor.RestCallContext;
import org.jboss.errai.enterprise.client.jaxrs.api.interceptor.RestClientInterceptor;

/**
 * @author Brett Meyer.
 */
public class KeycloakBearerTokenInterceptor implements RestClientInterceptor {

    @Override
    public void aroundInvoke(RestCallContext context) {
        String bearerToken = getBearerToken();
        context.getRequestBuilder().setHeader("Authorization", bearerToken);

        context.proceed();
    }

    public static native String getBearerToken() /*-{
        $wnd.keycloak.updateToken(10);
        return "Bearer " + $wnd.keycloak.token;
    }-*/;
}
