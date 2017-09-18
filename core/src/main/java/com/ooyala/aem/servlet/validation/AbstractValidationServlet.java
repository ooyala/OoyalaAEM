package com.ooyala.aem.servlet.validation;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
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
 * An abstract class for validation. Extends {@link SlingSafeMethodsServlet} class.
 * doGet method should be implemented.
 */
public abstract class AbstractValidationServlet extends SlingSafeMethodsServlet {

    private static final Logger log = LoggerFactory.getLogger(AbstractValidationServlet.class);

    private static final String JSON_RESPONSE_IS_VALID_KEY = "isValid";
    private static final String JSON_RESPONSE_MESSAGE_KEY = "message";
    private static final String JSON_RESPONSE_RESULTS_KEY = "results";

    @Override
    protected abstract void doGet(@Nonnull SlingHttpServletRequest request, @Nonnull SlingHttpServletResponse response) throws ServletException, IOException;

    /**
     * Writes response in JSON format.
     * Could be overridden since protected.
     *
     * @param printWriter The response writer where the JSON should be written.
     * @param isValid     a boolean representation of validity status.
     * @param message     error message in case of invalidity.
     */
    protected void updateResponse(final PrintWriter printWriter, final boolean isValid, final String message) {
        try {
            JSONWriter jsonWriter = new JSONWriter(printWriter);
            JSONObject jsonResults = new JSONObject();

            jsonResults.put(JSON_RESPONSE_IS_VALID_KEY, isValid);
            jsonResults.put(JSON_RESPONSE_MESSAGE_KEY, message);

            jsonWriter.object();
            jsonWriter.key(JSON_RESPONSE_RESULTS_KEY).value(jsonResults);
            jsonWriter.endObject();
        } catch (JSONException e) {
            log.error("JSON object construction failed", e);
        }
    }
}
