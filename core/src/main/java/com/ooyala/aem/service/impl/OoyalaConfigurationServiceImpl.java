package com.ooyala.aem.service.impl;

import com.ooyala.aem.client.OoyalaApiCredential;
import com.ooyala.aem.service.OoyalaConfigurationException;
import com.ooyala.aem.service.OoyalaConfigurationService;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.resource.observation.ResourceChange;
import org.apache.sling.api.resource.observation.ResourceChangeListener;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.ooyala.aem.service.impl.OoyalaConfigurationServiceImpl.OOYALA_CONFIG_PATH;

/*
 * Copyright (c) 2017, Ooyala, Inc.
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * •    Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * •    Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE 
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT 
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */

@Component(
        immediate = true,
        service = {OoyalaConfigurationService.class, ResourceChangeListener.class},
        property = {
                ResourceChangeListener.PATHS + "=" + OOYALA_CONFIG_PATH
        }
)
public class OoyalaConfigurationServiceImpl implements OoyalaConfigurationService, ResourceChangeListener {

    private static final Logger log = LoggerFactory.getLogger(OoyalaConfigurationServiceImpl.class);

    static final String OOYALA_CONFIG_PATH = "/etc/ooyala/ooyala-configuration/jcr:content";
    private static final String API_KEY = "apiKey";
    private static final String API_SECRET = "apiSecret";
    private static final String META_PATH = "metaKeys";

    private OoyalaApiCredential credentials;
    private List<String> metadataKeyCache = new CopyOnWriteArrayList<>();

    @Reference
    private SecurityHelper securityHelper;

    /**
     * Thread safe method for updating the API key cache.
     *
     * @throws OoyalaConfigurationException Throws an exception if the Ooyala configuration node cannot be found.
     */
    private synchronized void updateApiKeyCache(final ResourceResolver resourceResolver) throws OoyalaConfigurationException {
        log.debug("Updating Ooyala Api Key Cache.");

        Resource configResource = resourceResolver.getResource(OOYALA_CONFIG_PATH);

        if (configResource == null) {
            throw new OoyalaConfigurationException(String.format("The Ooyala configuration node %s is missing.", OOYALA_CONFIG_PATH));
        }

        ValueMap configResourceValueMap = configResource.adaptTo(ValueMap.class);

        if (configResourceValueMap == null) {
            throw new OoyalaConfigurationException("Unable to get properties of configuration node");
        }

        String apiKey = configResourceValueMap.get(API_KEY, String.class);
        String apiSecret = configResourceValueMap.get(API_SECRET, String.class);

        if (apiKey == null || apiSecret == null) {
            credentials = null;
            throw new OoyalaConfigurationException(String.format("The Ooyala configuration node %s is missing one or more properties. The required properties are: %s, %s", OOYALA_CONFIG_PATH, API_KEY, API_SECRET));
        }

        credentials = new OoyalaApiCredential(apiKey, apiSecret);

        log.debug("Ooyala Config Updated.\n");
    }

    /**
     * Thread safe method for updating the metadata key cache.
     *
     * @throws OoyalaConfigurationException Throws an exception if the Ooyala configuration node cannot be found.
     */
    private synchronized void updateMetadataKeyCache(final ResourceResolver resourceResolver) throws OoyalaConfigurationException {
        log.debug("Updating Ooyala Metadata Key Cache.");

        Resource configResource = resourceResolver.getResource(OOYALA_CONFIG_PATH);

        if (configResource == null) {
            throw new OoyalaConfigurationException(String.format("The Ooyala configuration node %s is missing.", OOYALA_CONFIG_PATH));
        }

        ValueMap configResourceValueMap = configResource.adaptTo(ValueMap.class);

        if (configResourceValueMap == null) {
            throw new OoyalaConfigurationException("Unable to get properties of configuration node");
        }

        String[] rawMetaKeys = configResourceValueMap.get(META_PATH, new String[0]);

        if (rawMetaKeys != null) {
            metadataKeyCache.clear();
            Collections.addAll(metadataKeyCache, rawMetaKeys);
        }

        log.debug("Ooyala Config Updated.\n");
    }

    @Override
    public List<String> getMetadataKeys() {
        return this.metadataKeyCache;
    }

    /**
     * The activate method for OSGi. Updates all caches.
     */
    @Activate
    protected void activate() {
        try (ResourceResolver resourceResolver = securityHelper.getServiceResourceResolver()) {
            updateApiKeyCache(resourceResolver);
            updateMetadataKeyCache(resourceResolver);
        } catch (OoyalaConfigurationException e) {
            log.error(e.getMessage(), e);
        } catch (LoginException e) {
            log.error("Unable to log into JCR.", e);
        }
    }

    @Override
    public OoyalaApiCredential getCredentials() {
        return isValidCredentials(credentials) ? credentials : null;
    }

    /**
     * Checks that API Key and Secret Key of a {@link OoyalaApiCredential} are not null and not empty.
     *
     * @param credentials an instance of {@link OoyalaApiCredential} class.
     * @return a boolean representation of credentials validity.
     */
    private boolean isValidCredentials(final OoyalaApiCredential credentials) {
        return credentials != null
                && StringUtils.isNotEmpty(credentials.getApiKey())
                && StringUtils.isNotEmpty(credentials.getApiSecret());
    }

    /**
     * Updates the API key and metadata caches when the Ooyala Configuration Node is updated.
     *
     * @param list - list of changes.
     */
    @Override
    public void onChange(@Nonnull List<ResourceChange> list) {
        list.forEach(resourceChange -> {
            log.debug("Handling event in path " + OOYALA_CONFIG_PATH);

            try (ResourceResolver resourceResolver = securityHelper.getServiceResourceResolver()) {
                updateApiKeyCache(resourceResolver);
                updateMetadataKeyCache(resourceResolver);
            } catch (OoyalaConfigurationException e) {
                log.error(e.getMessage(), e);
            } catch (LoginException e) {
                log.error("Unable to log into JCR.", e);
            }
        });
    }
}
