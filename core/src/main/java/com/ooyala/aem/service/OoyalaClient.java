package com.ooyala.aem.service;

import com.ooyala.aem.client.OoyalaClientException;
import com.ooyala.aem.client.request.OoyalaPostRequest;
import com.ooyala.aem.client.request.OoyalaPutRequest;
import com.ooyala.aem.client.request.OoyalaQueryRequest;
import com.ooyala.aem.client.request.OoyalaRequest;

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
 * Ooyala REST API client.
 */
public interface OoyalaClient {

    /**
     * Sends the request to Ooyala, expecting a JSON response.
     *
     * @param request An {@link OoyalaRequest}.
     * @return The JSON string returned by Ooyala's API.
     * @throws OoyalaClientException If unable to perform request or request parameters are invalid.
     */
    String request(OoyalaRequest request) throws OoyalaClientException;

    /**
     * Sends the request to Ooyala, expecting a JSON response.
     *
     * @param request An {@link OoyalaQueryRequest}.
     * @return The JSON string returned by Ooyala's API.
     * @throws OoyalaClientException If unable to perform request or request parameters are invalid.
     */
    String request(OoyalaQueryRequest request) throws OoyalaClientException;

    /**
     * Sends the {@link OoyalaPostRequest} request to Ooyala, expecting a JSON response.
     *
     * @param request An instance of {@link OoyalaPostRequest} class.
     * @return The JSON string returned by Ooyala's API.
     * @throws OoyalaClientException If unable to perform request.
     */
    String request(OoyalaPostRequest request) throws OoyalaClientException;

    /**
     * Sends the {@link OoyalaPutRequest} request to Ooyala, expecting a JSON response.
     *
     * @param request An instance of {@link OoyalaPutRequest} class.
     * @return The JSON string returned by Ooyala's API.
     * @throws OoyalaClientException If unable to perform request.
     */
    String request(OoyalaPutRequest request) throws OoyalaClientException;
}
