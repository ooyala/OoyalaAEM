package com.ooyala.aem.servlet;

import com.ooyala.aem.service.OoyalaService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.json.JSONArray;
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
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

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
 * The main servlet for retrieving autocomplete suggestions from Ooyala.
 */
@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.paths=/bin/wcm/ooyala/suggestions"
        }
)
public class OoyalaSuggestionServlet extends SlingSafeMethodsServlet {

    private static final Logger log = LoggerFactory.getLogger(OoyalaSuggestionServlet.class);
    private static final String LABEL = "label";
    private static final String META = "meta";
    private static final int DEFAULT_LIMIT = 5;
    private static final int MIN_SEARCH_LENGTH = 2;
    private static final String NO_RESULTS = "No Suggestions";

    private static final String JSON_KEY_SUGGESTIONS = "suggestions";
    private static final String JSON_KEY_RESULTS = "results";
    private static final String JSON_KEY_VALUE = "value";
    private static final String JSON_KEY_TITLE = "title";
    private static final String JSON_KEY_NAME = "name";

    @Reference
    private transient OoyalaService ooyalaService;

    /**
     * Returns suggestions based on the query and type parameters in the request.
     */
    @Override
    protected void doGet(@Nonnull SlingHttpServletRequest request, @Nonnull SlingHttpServletResponse response) throws ServletException, IOException {
        final String query = request.getParameter("query");
        final String type = request.getParameter("type");

        log.debug("QUERY = " + query);
        log.debug("TYPE = " + type);

        if (query == null || query.trim().length() < MIN_SEARCH_LENGTH ||
                type == null || type.trim().length() == 0) {
            return;
        }

        try {
            response.setContentType("application/json");
            if (LABEL.equals(type)) {
                final String rawResults = ooyalaService.getLabels(0);

                if (StringUtils.isEmpty(rawResults)) {
                    writeEmptyJSONResponse(response.getWriter());
                } else {
                    writeLabelJSONResponse(response.getWriter(), query, rawResults, DEFAULT_LIMIT);
                }
            } else if (META.equals(type)) {
                final List<String> resultList = ooyalaService.getMetaKeys();

                if (CollectionUtils.isEmpty(resultList)) {
                    writeEmptyJSONResponse(response.getWriter());
                } else {
                    writeMetaJSONResponse(response.getWriter(), query, resultList, DEFAULT_LIMIT);
                }
            }
        } catch (JSONException e) {
            log.error("Unable to write JSON response", e);

            try {
                writeEmptyJSONResponse(response.getWriter());
            } catch (JSONException e2) {
                log.error("Unable to write JSON response", e2);
                response.sendError(500, e.getMessage());
            }
        }
    }

    /**
     * Writes empty response.
     *
     * @param writer The response writer where the JSON should be written.
     * @throws JSONException if JSON object constructing is failed.
     */
    private void writeEmptyJSONResponse(final PrintWriter writer) throws JSONException {
        JSONWriter jsonWriter = new JSONWriter(writer);
        jsonWriter.object();
        jsonWriter.key(JSON_KEY_SUGGESTIONS);
        jsonWriter.array();
        jsonWriter.object()
                .key(JSON_KEY_NAME).value(NO_RESULTS)
                .key(JSON_KEY_VALUE).value(StringUtils.EMPTY)
                .key(JSON_KEY_TITLE).value(StringUtils.EMPTY)
                .endObject();
        jsonWriter.endArray();
        jsonWriter.key(JSON_KEY_RESULTS).value(1);
        jsonWriter.endObject();
    }

    /**
     * Writes the matching metadata keys into a JSON for display in AEM.
     *
     * @param writer     The response writer where the JSON should be written.
     * @param query      The query to filter the metadata keys.
     * @param resultList The List of metadata keys to filter and write.
     * @param limit      The maximum number of results to return.
     * @throws JSONException if JSON object constructing is failed.
     */
    private void writeMetaJSONResponse(final PrintWriter writer, final String query, final List<String> resultList, int limit) throws JSONException {
        JSONWriter jsonWriter = new JSONWriter(writer);
        jsonWriter.object();
        jsonWriter.key(JSON_KEY_SUGGESTIONS);
        jsonWriter.array();

        Supplier<Stream<String>> resultSupplier = () -> resultList
                .stream()
                .filter(result -> StringUtils.isNotEmpty(result) && StringUtils.containsIgnoreCase(result.toLowerCase(), query.toLowerCase()))
                .limit(limit);

        resultSupplier.get().forEach(result -> {
            try {
                jsonWriter.object()
                        .key(JSON_KEY_NAME).value("Metadata Key")
                        .key(JSON_KEY_TITLE).value(result)
                        .key(JSON_KEY_VALUE).value(result + ":")
                        .endObject();
            } catch (JSONException e) {
                log.error("JSON object constructing is failed", e);
            }
        });

        jsonWriter.endArray();
        jsonWriter.key(JSON_KEY_RESULTS).value(resultSupplier.get().count());
        jsonWriter.endObject();
    }

    /**
     * Manipulates the JSON response from Ooyala to fit the needs of AEM.
     *
     * @param writer     The response writer where the JSON should be written.
     * @param query      The query to filter the label names.
     * @param rawResults The JSON response from Ooyala's API.
     * @param limit      The maximum number of results to return.
     * @throws JSONException if JSON object constructing is failed.
     */
    private void writeLabelJSONResponse(final PrintWriter writer, final String query, final String rawResults, final int limit) throws JSONException {
        int resultItemsCount = 0;
        JSONObject jsonResults = new JSONObject(rawResults);
        JSONArray labels = jsonResults.getJSONArray("items");
        JSONWriter jsonWriter = new JSONWriter(writer);
        jsonWriter.object();
        jsonWriter.key(JSON_KEY_SUGGESTIONS);
        jsonWriter.array();

        for (int i = 0; i < labels.length(); i++) {
            if (resultItemsCount == limit) {
                break;
            }

            JSONObject obj = labels.getJSONObject(i);
            final String name = obj.getString("name");
            if (StringUtils.isNotEmpty(name) && StringUtils.containsIgnoreCase(name, query)) {
                jsonWriter.object()
                        .key(JSON_KEY_NAME).value(obj.get("name"))
                        .key(JSON_KEY_TITLE).value(obj.get("full_name"))
                        .key(JSON_KEY_VALUE).value(obj.get("name"))
                        .endObject();
                resultItemsCount++;
            }
        }

        jsonWriter.endArray();
        jsonWriter.key(JSON_KEY_RESULTS).value(resultItemsCount);
        jsonWriter.endObject();
    }
}
