package com.ooyala.aem.client.request;

import java.util.*;

import com.ooyala.aem.client.OoyalaApiCredential;
import org.apache.commons.lang.StringUtils;

/*
 * Copyright (c) 2017, Ooyala, Inc.
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * •	Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * •	Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation 
 *     and/or other materials provided with the distribution.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE 
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT 
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */

/**
 * Base class for all Ooyala requests.
 */
public abstract class OoyalaRequest {

    static final String ASSET_TYPE_PARAM = "asset_type";
    static final String API_ASSETS = "assets";
    static final String API_LABELS = "labels";
    static final String API_PLAYERS = "players";
    static final String API_PLAYLISTS = "playlists";

    private static final String ROOT_API_ENDPOINT = "/v2";
    private static final String PATH_SEPARATOR = "/";

    private static final String API_KEY_PARAM = "api_key";

    private final OoyalaApiCredential credentials;
    private String requestPath = buildRequestPath(API_ASSETS);
    private final SortedMap<String, String> parameters = new TreeMap<>();

    private static final String REQUEST_TARGET = "https://api.ooyala.com";

    private final HTTPMethod method;

    /**
     * An enum of all available HTTP methods.
     */
    public enum HTTPMethod {
        GET, POST, PUT, HEAD, PATCH, DELETE, OPTIONS
    }

    /**
     * Constructs an instance of OoyalaRequest with the provided credentials and the proper url,
     * based on Ooyala's RESTful API (v2).
     *
     * @param credentials The {@link OoyalaApiCredential} object for authenticating the API request.
     * @param method      The HTTP method to be used when connecting to Ooyala. Usually GET.
     * @param requestPath The request path for the type of object expected in the API response.
     * @throws IllegalArgumentException Throws {@link IllegalArgumentException} if any parameters is null.
     */
    public OoyalaRequest(OoyalaApiCredential credentials, HTTPMethod method, String requestPath) {
        if (credentials == null) {
            throw new IllegalArgumentException("credentials cannot be null.");
        }
        if (StringUtils.isEmpty(credentials.getApiKey())) {
            throw new IllegalArgumentException("api_key cannot be null.");
        }
        if (StringUtils.isEmpty(credentials.getApiSecret())) {
            throw new IllegalArgumentException("apiSecret cannot be null.");
        }
        if (method == null) {
            throw new IllegalArgumentException("The argument 'method' cannot be null.");
        }
        if (StringUtils.isEmpty(requestPath)) {
            throw new IllegalArgumentException("The argument 'requestPath' cannot be null.");
        }

        this.credentials = credentials;
        this.method = method;
        this.requestPath = requestPath;
        setParameter(API_KEY_PARAM, credentials.getApiKey());
    }

    /**
     * A getter for the parameters map.
     *
     * @return The current parameters map.
     */
    public SortedMap<String, String> getParameters() {
        return this.parameters;
    }

    /**
     * Sets parameters to parameters map.
     *
     * @param paramName  - parameter name.
     * @param paramValue - parameter value.
     */
    public void setParameter(final String paramName, final String paramValue) {
        this.parameters.put(paramName, paramValue);
    }

    /**
     * A getter for the request credentials.
     *
     * @return The {@link OoyalaApiCredential} containing the current request credentials.
     */
    public OoyalaApiCredential getCredentials() {
        return credentials;
    }

    /**
     * A getter for the requestPath.
     *
     * @return The String representing the request's requestPath.
     */
    public String getRequestPath() {
        return requestPath;
    }

    /**
     * A getter for the request's {@link HTTPMethod}.
     *
     * @return The current {@link HTTPMethod}.
     */
    public HTTPMethod getMethod() {
        return method;
    }

    /**
     * A getter for the request's target.
     *
     * @return The string representing the request's target.
     */
    public String getRequestTarget() {
        return REQUEST_TARGET;
    }

    /**
     * A setter for the request's limit, the maximum results to be returned from the request.
     *
     * @param limit The integer limit.
     */
    public void setLimit(int limit) {
        setParameter("limit", String.valueOf(limit));
    }

    /**
     * A setter for the request's offest, used by the API in determining the sequence of results to return.
     *
     * @param offset The integer offset.
     */
    public void setOffset(int offset) {
        setParameter("offset", String.valueOf(offset));
    }

    @Override
    public String toString() {
        final int maxLen = 20;
        return "OoyalaRequest [credentials=" +
                credentials +
                ", parameters=" +
                toString(parameters.entrySet(), maxLen) + ", requestTarget=" + REQUEST_TARGET +
                ", requestPath=" + requestPath + ", method=" + method + "]";
    }

    /**
     * Builds request path by arguments based on Root API endpoint.
     * E.g. 'assets' -> '/v2/assets'
     *
     * @param args request composite items. Could be empty.
     * @return Root API endpoint if args is empty and composite request path by args otherwise based on Root API endpoint.
     */
    static String buildRequestPath(final String... args) {
        if (args.length > 0) {
            StringBuilder requestBuilder = new StringBuilder(ROOT_API_ENDPOINT);

            Arrays.stream(args).forEach(arg -> requestBuilder.append(PATH_SEPARATOR).append(arg));

            return requestBuilder.toString();
        }

        return ROOT_API_ENDPOINT;
    }

    private String toString(Collection<?> collection, int maxLen) {
        StringBuilder builder = new StringBuilder();
        int i = 0;

        builder.append("[");
        for (Iterator<?> iterator = collection.iterator(); iterator.hasNext() && i < maxLen; i++) {
            if (i > 0) {
                builder.append(", ");
            }

            builder.append(iterator.next());
        }
        builder.append("]");

        return builder.toString();
    }
}