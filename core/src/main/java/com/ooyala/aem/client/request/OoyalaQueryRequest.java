package com.ooyala.aem.client.request;

import java.util.Map;
import java.util.TreeMap;

import com.ooyala.aem.client.OoyalaApiCredential;

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
 * Provides additional query parameters to {@link OoyalaRequest} to support a
 * where clause.
 */
public class OoyalaQueryRequest extends OoyalaRequest {

    private final Map<String, String> queryParameters = new TreeMap<>();

    /**
     * Constructs an instance of OoyalaQueryRequest with the provided
     * credentials and the proper url, based on Ooyala's RESTful API (v2).
     *
     * @param credentials The {@link OoyalaApiCredential} object for authenticating the
     *                    API request.
     * @param method      The HTTP method to be used when connecting to Ooyala. Usually
     *                    GET.
     * @param requestPath The request path for the type of object expected in the API
     *                    response.
     */
    public OoyalaQueryRequest(OoyalaApiCredential credentials, HTTPMethod method, String requestPath) {
        super(credentials, method, requestPath);
    }

    /**
     * A getter for the queryParameters map.
     *
     * @return The current queryParameters map.
     */
    public Map<String, String> getQueryParameters() {
        return this.queryParameters;
    }

    /**
     * A method to set parameters to queryParameters map.
     *
     * @param paramName parameter name.
     * @param paramValue parameter value.
     */
    public void setQueryParameter(final String paramName, final String paramValue) {
        this.queryParameters.put(paramName, paramValue);
    }
}
