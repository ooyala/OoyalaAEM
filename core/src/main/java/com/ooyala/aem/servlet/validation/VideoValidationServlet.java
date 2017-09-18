package com.ooyala.aem.servlet.validation;

import com.ooyala.aem.client.OoyalaClientException;
import com.ooyala.aem.service.OoyalaConfigurationService;
import com.ooyala.aem.service.OoyalaService;
import org.apache.commons.lang.CharEncoding;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;

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
 * An extension of {@link AbstractValidationServlet} class with video name-specific validation implementation.
 * Request example: /bin/wcm/ooyala/validation/video?name=Lorem
 */
@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.paths=/bin/wcm/ooyala/validation/video"
        }
)
public class VideoValidationServlet extends AbstractValidationServlet {

    private static final Logger log = LoggerFactory.getLogger(VideoValidationServlet.class);

    private static final String NAME_REQUEST_PARAMETER = "name";
    private static final String JSON_ITEMS_KEY = "items";

    @Reference
    private transient OoyalaService ooyalaService;

    @Reference
    private transient OoyalaConfigurationService configurationService;

    @Override
    protected void doGet(@Nonnull SlingHttpServletRequest request, @Nonnull SlingHttpServletResponse response) throws ServletException, IOException {
        String nameRequestParameter = request.getParameter(NAME_REQUEST_PARAMETER);

        boolean isValid = true;
        String message = StringUtils.EMPTY;

        if (StringUtils.isEmpty(nameRequestParameter)) {
            isValid = false;
            message = "Name request parameter is not set";
        } else {
            try {
                byte[] nameRequestParameterBytes = nameRequestParameter.getBytes(CharEncoding.ISO_8859_1);
                nameRequestParameter = new String(nameRequestParameterBytes, CharEncoding.UTF_8);
                String videoResponse = ooyalaService.getVideo(configurationService.getCredentials(), nameRequestParameter);

                JSONObject jsonResponse = new JSONObject(videoResponse);
                JSONArray items = jsonResponse.getJSONArray(JSON_ITEMS_KEY);

                isValid = isValid(items, nameRequestParameter);

                if (!isValid) {
                    message = "Current name already exists. Please specify a unique file name.";
                }
            } catch (OoyalaClientException e) {
                log.error("Unable to perform request", e);
            } catch (JSONException e) {
                log.error("JSON object construction failed", e);
            }
        }

        updateResponse(response.getWriter(), isValid, message);
    }

    /**
     * Checks is response from Backlot contains an element with the same name.
     *
     * @param responseItems An items from Backlot in JSON array representation.
     * @param videoName     The name of a video to check.
     * @return True if no item were found with same name and false otherwise.
     * @throws JSONException If unable to get JSON object from JSON array.
     */
    private boolean isValid(final JSONArray responseItems, final String videoName) throws JSONException {
        if (responseItems.length() == 0) {
            return true;
        }

        for (int i = 0; i < responseItems.length(); i++) {
            JSONObject jsonObject = responseItems.getJSONObject(i);

            if (StringUtils.equals(jsonObject.getString(NAME_REQUEST_PARAMETER), videoName)) {
                return false;
            }
        }

        return true;
    }
}
