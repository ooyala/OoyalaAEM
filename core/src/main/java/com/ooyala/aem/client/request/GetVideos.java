package com.ooyala.aem.client.request;

import com.ooyala.aem.client.OoyalaApiCredential;
import com.ooyala.aem.client.OoyalaClientException;

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
 * An extension of {@link OoyalaRequest} for retrieving videos via Ooyala's RESTful API.
 */
public class GetVideos extends OoyalaQueryRequest {

    private static final String INCLUDE_PARAMETER_KEY = "include";
    private static final String STATUS_QUERY_PARAMETER = "status";

    /**
     * Constructs an instance of GetVideos with the provided credentials and the proper url,
     * based on Ooyala's RESTful API (v2).
     *
     * @param credentials The {@link OoyalaApiCredential} object for authenticating the API request.
     * @param includeLabels A boolean value indicating whether the response should include asset labels.
     * @param includeMetadata A boolean value indicating whether the response should include asset metadata.
     */
    public GetVideos(final OoyalaApiCredential credentials, final boolean includeLabels, final boolean includeMetadata) {
        super(credentials, HTTPMethod.GET, buildRequestPath(API_ASSETS));

        setParameter(ASSET_TYPE_PARAM, "video");

        if (includeLabels && includeMetadata) {
            setParameter(INCLUDE_PARAMETER_KEY, "labels,metadata");
        } else if (includeLabels) {
            setParameter(INCLUDE_PARAMETER_KEY, "labels");
        } else if (includeMetadata) {
            setParameter(INCLUDE_PARAMETER_KEY, "metadata");
        }

        setQueryParameter(STATUS_QUERY_PARAMETER, "'Live'");
    }

    /**
     * Adds query parameters.
     *
     * @param queryString    a query in String representation.
     * @param searchCriteria a String representation of {@link SearchType} object.
     * @throws OoyalaClientException If query string for metadata has an invalid format.
     */
    public void addQueryByType(final String queryString, final String searchCriteria) throws OoyalaClientException {
        SearchType type = SearchType.getSearchType(searchCriteria);
        String queryParameterName = type.getQueryParameterName();

        if (type == SearchType.SEARCH_BY_LABEL) {
            setQueryParameter(queryParameterName, queryString);
        } else if (type == SearchType.SEARCH_BY_TITLE || type == SearchType.SEARCH_BY_DESCRIPTION) {
            setQueryParameter(queryParameterName, String.format("'%s'", queryString));
        } else if (type == SearchType.SEARCH_BY_META) {
            addMetaQuery(queryString, queryParameterName);
        }
    }

    /**
     * Adds a query for custom asset metadata. Note: The metadata key must exist.
     *
     * @param metaQueryString The string to query Ooyala's API with. Should be in the form <key>:<value>.
     */
    private void addMetaQuery(final String metaQueryString, final String paramName) throws OoyalaClientException {
        String[] params = metaQueryString.split(":");

        if (params.length != 2) {
            throw new OoyalaClientException(String.format("Invalid query string '%s'. Query string should be in form 'key:value'", metaQueryString));
        } else {
            setQueryParameter(paramName + params[0], "'" + params[1] + "'");
        }
    }
}