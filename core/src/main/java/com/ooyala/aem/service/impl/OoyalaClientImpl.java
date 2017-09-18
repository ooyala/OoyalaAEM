package com.ooyala.aem.service.impl;

import com.ooyala.aem.client.OoyalaApiCredential;
import com.ooyala.aem.client.OoyalaClientException;
import com.ooyala.aem.client.request.OoyalaPostRequest;
import com.ooyala.aem.client.request.OoyalaPutRequest;
import com.ooyala.aem.client.request.OoyalaQueryRequest;
import com.ooyala.aem.client.request.OoyalaRequest;
import com.ooyala.aem.service.OoyalaClient;
import com.sun.jersey.api.client.*;
import com.sun.jersey.core.util.Base64;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.CharEncoding;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;

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

@Component(
        immediate = true,
        service = OoyalaClient.class
)
public class OoyalaClientImpl implements OoyalaClient {

    private static final Logger log = LoggerFactory.getLogger(OoyalaClientImpl.class);

    private static final String EXPIRES_PARAMETER = "expires";

    private MessageDigest digest;
    private Client client;

    @Activate
    protected void init() {
        try {
            digest = MessageDigest.getInstance("SHA-256");
            client = Client.create();
            client.setConnectTimeout(15000); //15 second timeout
            client.setReadTimeout(60000);    //60 second timeout
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm is not found by MessageDigest", e);
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public String request(final OoyalaRequest request) throws OoyalaClientException {
        return request(
                request.getRequestTarget(),
                request.getCredentials(),
                request.getMethod().name(),
                request.getRequestPath(),
                request.getParameters(),
                Collections.emptyMap(),
                null);
    }

    @Override
    public String request(final OoyalaQueryRequest request) throws OoyalaClientException {
        return request(
                request.getRequestTarget(),
                request.getCredentials(),
                request.getMethod().name(),
                request.getRequestPath(),
                request.getParameters(),
                request.getQueryParameters(),
                null);
    }

    @Override
    public String request(final OoyalaPostRequest request) throws OoyalaClientException {
        return request(
                request.getRequestTarget(),
                request.getCredentials(),
                request.getMethod().name(),
                request.getRequestPath(),
                request.getParameters(),
                null,
                request.getBody());
    }

    @Override
    public String request(final OoyalaPutRequest request) throws OoyalaClientException {
        return request(
                request.getRequestTarget(),
                request.getCredentials(),
                request.getMethod().name(),
                request.getRequestPath(),
                request.getParameters(),
                null,
                request.getBody());
    }

    /**
     * Sends the request to Ooyala, expecting a JSON response.
     *
     * @param requestTarget   The target to use for the request.
     * @param credentials     The {@link OoyalaApiCredential} object for authenticating the API request.
     * @param method          The HTTP method to be used when connecting to Ooyala. Usually GET.
     * @param requestPath     The request path for the type of object expected in the API response.
     * @param parameters      The request parameters.
     * @param queryParameters The request query parameters.
     * @param body            The body of a request in JSON format.
     * @return The JSON string returned by Ooyala's API.
     * @throws OoyalaClientException If unable to perform request.
     */
    private String request(final String requestTarget, final OoyalaApiCredential credentials, final String method, final String requestPath,
                            final SortedMap<String, String> parameters, final Map<String, String> queryParameters, final JSONObject body) throws OoyalaClientException {

        if (!parameters.containsKey(EXPIRES_PARAMETER)) {
            parameters.put(EXPIRES_PARAMETER, Long.toString((System.currentTimeMillis() / 1000) + 60));
        }

        final StringBuilder requestBuilder = new StringBuilder(requestTarget).append(requestPath).append("?");
        final String signature = getSignature(credentials, method, requestPath, parameters, queryParameters, body);

        if (StringUtils.isEmpty(signature)) {
            throw new OoyalaClientException("Signature is null or empty");
        }

        final String parameterString = getParameters(parameters, queryParameters, signature);
        final String request = requestBuilder.append(parameterString).toString();
        log.debug("Ooyala: request URL = " + request);

        long start = System.nanoTime();
        final WebResource webResource = client.resource(request);
        log.debug("Ooyala: request took " + (System.nanoTime() - start) + " ns.");

        final WebResource.Builder builder = webResource.type(MediaType.APPLICATION_JSON_TYPE);
        ClientResponse clientResponse = null;

        if (body != null) {
            if (StringUtils.equals(method, "POST")) {
                clientResponse = builder.post(ClientResponse.class, body.toString());
            } else if (StringUtils.equals(method, "PUT")) {
                clientResponse = builder.put(ClientResponse.class, body.toString());
            }
        } else {
            clientResponse = builder.get(ClientResponse.class);
        }

        if (clientResponse == null) {
            throw new OoyalaClientException("Client response is null");
        }

        if (clientResponse.getStatus() != 200) {
            throw new OoyalaClientException(String.format("HTTP response code from REST request : %s %s", clientResponse.getStatus(), clientResponse.getEntity(String.class)));
        }

        return clientResponse.getEntity(String.class);
    }

    /**
     * Builds a signature for authenticating the request with Ooyala. See Ooyala documentation at http://api.ooyala.com/docs/v2 for more info.
     * Note: This method assumes that no parameters from the parameters map or the queryParameters map will occur alphabetically after the word "where".
     *
     * @param credentials     An {@link OoyalaApiCredential} to obtain API Secret from to use for digesting.
     * @param method          The HTTP method to be used when connecting to Ooyala.
     * @param requestPath     The request path for the type of object expected in the API response.
     * @param parameters      The request parameters.
     * @param queryParameters The request queryParameters.
     * @param body            The body of a request in JSON format.
     * @return The string digest of all parameters concatenated.
     */
    private String getSignature(final OoyalaApiCredential credentials, final String method, final String requestPath,
                                final SortedMap<String, String> parameters, final Map<String, String> queryParameters, final JSONObject body) {
        final StringBuilder parametersBuilder = new StringBuilder();
        parametersBuilder.append(credentials.getApiSecret()).append(method).append(requestPath);
        parameters.forEach((key, value) -> parametersBuilder.append(key).append("=").append(value));

        if (body != null) {
            parametersBuilder.append(body.toString());
        }

        if (MapUtils.isNotEmpty(queryParameters)) {
            parametersBuilder.append("where=");
            applyQueryParameters(parametersBuilder, queryParameters);
        }

        try {
            return generateURLEncodedSignature(parametersBuilder.toString());
        } catch (CloneNotSupportedException cnse) {//these exceptions are highly unlikely to ocurr, so lets just rethrow them as RTE, in case they do happen
            log.error("Unable to clone object", cnse);
            throw new RuntimeException(cnse);
        } catch (UnsupportedEncodingException uee) {
            log.error("Encoding is not supported", uee);
        }

        return null;
    }

    /**
     * Applies query parameters to parameter builder.
     *
     * @param parametersBuilder A StringBuilder representation of parameters to generate signature string.
     * @param queryParameters   The request queryParameters.
     */
    private void applyQueryParameters(final StringBuilder parametersBuilder, final Map<String, String> queryParameters) {
        Iterator<Map.Entry<String,String>> iterator = queryParameters.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();

            String key = entry.getKey();
            String value = entry.getValue();

            log.debug("key = " + key + ", value = " + value);

            if (!StringUtils.equals(key, "label")) {
                parametersBuilder.append(key).append("=").append(value);
            } else {
                log.debug("WENT TO LABELS INCLUDE");
                parametersBuilder.append("labels INCLUDES " + "'").append(value).append("'");
            }
            parametersBuilder.append(iterator.hasNext() ? " AND " : StringUtils.EMPTY);
        }
    }

    /**
     * Concatenates the parameters, queryParameters, and signature into a url string.
     *
     * @param parameters      The request parameters.
     * @param queryParameters The request queryParameters, might be null.
     * @param signature       The signature.
     * @return A URL encoded string of all parameters, queryParameters, and the signature.
     */
    private String getParameters(final SortedMap<String, String> parameters, final Map<String, String> queryParameters, final String signature) {
        final StringBuilder parameterBuilder = new StringBuilder();
        parameterBuilder.append("signature").append("=").append(signature);
        parameters.forEach((key, value) -> parameterBuilder.append("&").append(key).append("=").append(value));

        if (MapUtils.isNotEmpty(queryParameters)) {
            parameterBuilder.append("&where=");
            parseQueryParameters(queryParameters, parameterBuilder);
        }

        return parameterBuilder.toString();
    }

    /**
     * Parse query parameters from request query to string builder.
     *
     * @param queryParameters  The request queryParameters.
     * @param parameterBuilder String builder with result.
     */
    private void parseQueryParameters(final Map<String, String> queryParameters, final StringBuilder parameterBuilder) {
        Iterator<Map.Entry<String,String>> iterator = queryParameters.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String,String> entry = iterator.next();

            String key = entry.getKey();
            String value = entry.getValue();

            log.debug("key = " + key + ", value = " + value);

            try {
                if (!StringUtils.equals(key, "label")) {
                    parameterBuilder.append(key).append("=").append(URLEncoder.encode(value, CharEncoding.UTF_8));
                } else {
                    log.debug("WENT TO LABELS INCLUDE");
                    parameterBuilder.append("labels%20INCLUDES%20" + "'").append(URLEncoder.encode(value, CharEncoding.UTF_8)).append("'");
                }
                parameterBuilder.append(iterator.hasNext() ? "%20AND%20" : StringUtils.EMPTY);
            } catch (UnsupportedEncodingException e) {
                log.error("Encoding is not supported", e);
            }
        }
    }

    /**
     * URL encodes the query and digest for transportation.
     *
     * @param query The query string.
     * @return The URL encoded query string.
     * @throws CloneNotSupportedException if close is not supported my implementation.
     * @throws UnsupportedEncodingException if charset is not supported.
     */
    private String generateURLEncodedSignature(final String query) throws CloneNotSupportedException, UnsupportedEncodingException {
        final MessageDigest digester = (MessageDigest) digest.clone();
        digester.reset();

        final String urlEncodedSignature = URLEncoder.encode(new String(Base64.encode(digester.digest(query.getBytes(CharEncoding.UTF_8))), CharEncoding.UTF_8).substring(0, 43), CharEncoding.UTF_8);

        log.debug("SIGNATURE = " + urlEncodedSignature);

        return urlEncodedSignature;
    }
}
