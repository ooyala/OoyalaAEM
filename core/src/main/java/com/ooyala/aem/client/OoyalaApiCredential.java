package com.ooyala.aem.client;

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
 * A container class for API Key and Secret for the Ooyala v2 RESTful API.
 */
public class OoyalaApiCredential {

    private String apiKey;
    private String apiSecret;

    /**
     * Constructs an instance of this class with the provided apiKey and apiSecret. No validation is performed.
     *
     * @param apiKey The Ooyala API key, obtained from Ooyala Backlot.
     * @param apiSecret The Ooyala API secret, obtained from Ooyala Backlot.
     */
    public OoyalaApiCredential(String apiKey, String apiSecret) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }

    /**
     * A getter for the API key.
     *
     * @return The current API key.
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * A getter for the API secret.
     *
     * @return The current API secret.
     */
    public String getApiSecret() {
        return apiSecret;
    }

    @Override
    public String toString() {
        return "OoyalaApiCredential [apiKey=" + apiKey + ", apiSecret=" + "****************************************" + "]";
    }
}
