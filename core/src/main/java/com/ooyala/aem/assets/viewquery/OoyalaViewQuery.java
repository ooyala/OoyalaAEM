package com.ooyala.aem.assets.viewquery;

import com.day.cq.wcm.core.contentfinder.Hit;
import com.day.cq.wcm.core.contentfinder.ViewQuery;
import com.ooyala.aem.client.OoyalaApiCredential;
import com.ooyala.aem.service.OoyalaService;
import com.ooyala.aem.client.OoyalaClientException;
import com.ooyala.aem.service.OoyalaConfigurationService;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
 * An abstract Ooyala-specific implementation of {@link ViewQuery} to build a collection of {@link Hit} objects via Backlot system.
 */
public abstract class OoyalaViewQuery implements ViewQuery {

    private static final Logger logger = LoggerFactory.getLogger(OoyalaViewQuery.class);

    protected OoyalaService ooyalaService;
    private OoyalaConfigurationService ooyalaConfigurationService;
    private SlingHttpServletRequest slingHttpServletRequest;

    private static final String LIMIT_REQUEST_PARAMETER = "limit";
    private static final String QUERY_REQUEST_PARAMETER = "query";
    private static final String SEARCH_BY_REQUEST_PARAMETER = "searchBy";

    protected static final String JSON_ITEMS_KEY = "items";
    protected static final String JSON_EMBED_CODE_KEY = "embed_code";
    protected static final String JSON_IMAGE_KEY = "preview_image_url";
    protected static final String JSON_UPDATED_AT_KEY = "updated_at";
    protected static final String JSON_DURATION_KEY = "duration";
    protected static final String JSON_PATH_KEY = "path";
    protected static final String JSON_ID_KEY = "id";
    protected static final String JSON_NAME_KEY = "name";
    protected static final String JSON_HITS_KEY = "hits";

    private static final int DEFAULT_LIMIT = 10;

    /**
     * Method to construct {@link OoyalaViewQuery} object.
     *
     * @param ooyalaService - {@link OoyalaService} object.
     * @param ooyalaConfigurationService - {@link OoyalaConfigurationService} object.
     * @param slingHttpServletRequest - {@link SlingHttpServletRequest} object.
     */
    public OoyalaViewQuery(final OoyalaService ooyalaService, final OoyalaConfigurationService ooyalaConfigurationService,
            final SlingHttpServletRequest slingHttpServletRequest) {

        this.ooyalaService = ooyalaService;
        this.ooyalaConfigurationService = ooyalaConfigurationService;
        this.slingHttpServletRequest = slingHttpServletRequest;
    }

    /**
     * Ooyala-specific implementation to build collection of {@link Hit} objects.
     *
     * @return a collection of {@link Hit} objects.
     */
    @Override
    public Collection<Hit> execute() {
        List<Hit> hitList = new ArrayList<>();

        String limit = slingHttpServletRequest.getParameter(LIMIT_REQUEST_PARAMETER);
        String query = slingHttpServletRequest.getParameter(QUERY_REQUEST_PARAMETER);
        String searchBy = slingHttpServletRequest.getParameter(SEARCH_BY_REQUEST_PARAMETER);

        OoyalaApiCredential credentials = ooyalaConfigurationService.getCredentials();

        int[] offsetAndLimit = getLimit(limit);

        try {
            JSONObject jsonObject = getJSON(executeQuery(credentials, searchBy, query, offsetAndLimit));

            if (jsonObject != null) {
                JSONArray jsonArray = filterJSONObject(jsonObject, query).getJSONArray(JSON_HITS_KEY);

                for (int i = 0; i < jsonArray.length(); i++) {
                    hitList.add(createHit(jsonArray.getJSONObject(i)));
                }
            }
        } catch (OoyalaClientException e) {
            logger.error("Unable to perform request", e);
        } catch (JSONException e) {
            logger.error("Unable to build JSON object from response", e);
        }

        return hitList;
    }

    /**
     * Filters JSON object if needed regarding the query parameter.
     *
     * @param jsonObject An initial JSON object.
     * @param query      A query string to filter by.
     * @return Filtered JSON object or initial JSON object if filtering is not needed.
     */
    protected abstract JSONObject filterJSONObject(final JSONObject jsonObject, final String query) throws JSONException;

    /**
     * An abstract method to execute query.
     *
     * @param ooyalaApiCredential an instance of {@link OoyalaApiCredential} class.
     * @param searchBy            search criteria.
     * @param query               query string.
     * @param offsetAndLimit      an array with query offset and limit.
     * @return raw response in String representation.
     * @throws OoyalaClientException if unable to perform request.
     */
    protected abstract String executeQuery(final OoyalaApiCredential ooyalaApiCredential, final String searchBy,
                                           final String query, final int[] offsetAndLimit) throws OoyalaClientException;

    /**
     * Manipulates the response JSON from Ooyala to fit the needs of built-in AEM functionality.
     *
     * @param response The raw JSON response from Ooyala.
     * @return A well formed JSON response.
     * @throws JSONException in case of JSON object constructing is failed.
     */
    protected abstract JSONObject getJSON(final String response) throws JSONException;

    /**
     * Builds {@link Hit} object with Ooyala specific fields from JSON object.
     *
     * @param jsonObject - initial JSON object.
     * @return {@link Hit} object - representation of JSON object.
     * @throws JSONException if JSON error is occurred.
     */
    protected abstract Hit createHit(final JSONObject jsonObject) throws JSONException;

    /**
     * Returns string property of JSON object if exists or empty string otherwise.
     *
     * @param jsonObject - JSON object with properties.
     * @param propName - property name.
     * @return string property if exists or empty string otherwise.
     * @throws JSONException if unable to get property from object.
     */
    protected String getPropValue(final JSONObject jsonObject, final String propName) throws JSONException {
        return jsonObject.has(propName) ? jsonObject.getString(propName) : StringUtils.EMPTY;
    }

    /**
     * Parses the limit and offset string from AEM into integers.
     *
     * @param limitString The limit parameter from request.
     * @return An integer array containing the offset and limit for the Ooyala request.
     */
    private int[] getLimit(final String limitString) {
        // limit string format : 10..20
        // [0] = offset
        // [1] = limit
        if (StringUtils.isNotEmpty(limitString)) {
            int offset = Integer.parseInt(limitString.substring(0, limitString.indexOf('.')));
            int limit = Integer.parseInt(limitString.substring(limitString.lastIndexOf('.') + 1, limitString.length())) - offset;

            return new int[]{offset, limit};
        }

        return new int[]{0, DEFAULT_LIMIT};
    }
}
