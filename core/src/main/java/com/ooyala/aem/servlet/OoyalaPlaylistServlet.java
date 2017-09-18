package com.ooyala.aem.servlet;

import com.ooyala.aem.client.OoyalaClientException;
import com.ooyala.aem.service.OoyalaService;
import com.ooyala.aem.service.OoyalaConfigurationService;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;

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

/**
 * The servlet for retrieving playlist information from Ooyala.
 */
@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.paths=/bin/wcm/ooyala/playlist"
        }
)
public class OoyalaPlaylistServlet extends SlingSafeMethodsServlet {

    private static final Logger log = LoggerFactory.getLogger(OoyalaPlaylistServlet.class);

    private static final String PLAYLIST_ID_REQUEST_PARAMETER = "playlistId";
    private static final String JSON_KEY_RESULT = "result";

    @Reference
    private transient OoyalaService ooyalaService;

    @Reference
    private transient OoyalaConfigurationService ooyalaConfigurationService;

    @Override
    protected void doGet(@Nonnull SlingHttpServletRequest request, @Nonnull SlingHttpServletResponse response) throws ServletException, IOException {
        String playlistId = request.getParameter(PLAYLIST_ID_REQUEST_PARAMETER);

        if (StringUtils.isNotEmpty(playlistId)) {
            response.setContentType("application/json");

            try {
                String playlistInfo = ooyalaService.getPlaylistInfo(ooyalaConfigurationService.getCredentials(), playlistId);

                updateResponse(response.getWriter(), playlistInfo);
            } catch (OoyalaClientException e) {
                log.warn("No playlist was found by id '{}' - use as a single video.", playlistId, e);
            } catch (JSONException e) {
                log.error("Unable to construct JSON object by response", e);
            }
        } else {
            log.warn(String.format("Request parameter '%s' is empty.", PLAYLIST_ID_REQUEST_PARAMETER));
        }
    }

    /**
     * Writes playlist information fo response in JSON representation.
     *
     * @param printWriter  The response writer where the JSON should be written.
     * @param playlistInfo The raw response from Ooyala with playlist information.
     * @throws JSONException Ff JSON object constructing is failed.
     */
    private void updateResponse(final PrintWriter printWriter, final String playlistInfo) throws JSONException {
        JSONWriter jsonWriter = new JSONWriter(printWriter);
        JSONObject jsonResults = new JSONObject(playlistInfo);

        jsonWriter.object();
        jsonWriter.key(JSON_KEY_RESULT).value(jsonResults);
        jsonWriter.endObject();
    }
}
