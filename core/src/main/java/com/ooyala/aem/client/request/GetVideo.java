package com.ooyala.aem.client.request;

import com.ooyala.aem.client.OoyalaApiCredential;

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
 * An extension of {@link OoyalaRequest} for retrieving video information via Ooyala's RESTful API by video's name.
 */
public class GetVideo extends OoyalaQueryRequest {

    private static final String NAME_QUERY_PARAMETER = "name";

    /**
     * Constructs an instance of GetVideo with the provided credentials and the proper url,
     * based on Ooyala's RESTful API (v2).
     *
     * @param credentials The {@link OoyalaApiCredential} object for authenticating the API request.
     * @param name A name of a video to get an information about from Backlot.
     */
    public GetVideo(final OoyalaApiCredential credentials, final String name) {
        super(credentials, HTTPMethod.GET, buildRequestPath(API_ASSETS));

        setParameter(ASSET_TYPE_PARAM, "video");
        setQueryParameter(NAME_QUERY_PARAMETER, String.format("'%s'", name));
    }
}
