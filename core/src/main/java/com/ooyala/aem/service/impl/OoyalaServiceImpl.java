package com.ooyala.aem.service.impl;

import com.ooyala.aem.client.*;
import com.ooyala.aem.client.request.*;
import com.ooyala.aem.service.OoyalaClient;
import com.ooyala.aem.service.OoyalaConfigurationService;
import com.ooyala.aem.service.OoyalaService;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

@Component(
        immediate = true,
        service = OoyalaService.class
)
public class OoyalaServiceImpl implements OoyalaService {

    private static final Logger logger = LoggerFactory.getLogger(OoyalaServiceImpl.class);

    private static final String PERFORM_REQUEST_ERROR_MESSAGE = "Unable to perform request";

    @Reference
    private OoyalaConfigurationService configService;

    @Reference
    private OoyalaClient ooyalaClient;

    @Override
    public List<String> getMetaKeys() {
        return configService.getMetadataKeys();
    }

    @Override
    public String getAllVideos(final OoyalaApiCredential credentials, final int offset, final int limit) throws OoyalaClientException {
        return getVideos(credentials, StringUtils.EMPTY, StringUtils.EMPTY, offset, limit);
    }

    @Override
    public String getVideos(final OoyalaApiCredential credentials, final String searchBy, final String queryString, final int offset, final int limit)
            throws OoyalaClientException {

        if (queryString == null) {
            throw new NullPointerException("queryString is null");
        }

        GetVideos getVideosRequest = new GetVideos(credentials, false, false);

        getVideosRequest.addQueryByType(queryString, searchBy);

        if (limit > 0) {
            getVideosRequest.setLimit(limit);
        }
        if (offset > 0) {
            getVideosRequest.setOffset(offset);
        }

        return ooyalaClient.request(getVideosRequest);
    }

    @Override
    public String getVideo(final OoyalaApiCredential ooyalaApiCredentials, final String name) throws OoyalaClientException {
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("Name parameter is not set");
        }

        GetVideo getVideoRequest = new GetVideo(ooyalaApiCredentials, name);

        return ooyalaClient.request(getVideoRequest);
    }

    @Override
    public String getLabels(final int limit) {
        OoyalaApiCredential credentials = configService.getCredentials();

        if (credentials != null) {
            try {
                GetLabels request = new GetLabels(credentials);
                if (limit > 0) {
                    request.setLimit(limit);
                } else {
                    request.setLimit(500);
                }

                return ooyalaClient.request(request);
            } catch (OoyalaClientException e) {
                logger.error(PERFORM_REQUEST_ERROR_MESSAGE, e);
            }
        }

        return null;
    }

    @Override
    public String getAllPlayers() {
        try {
            GetPlayers getPlayersRequest = new GetPlayers(configService.getCredentials());
            return ooyalaClient.request(getPlayersRequest);
        } catch (OoyalaClientException e) {
            logger.error(PERFORM_REQUEST_ERROR_MESSAGE, e);
        }

        return null;
    }

    @Override
    public String initUpload(final JSONObject json) throws JSONException {
        try {
            OoyalaApiCredential credentials = configService.getCredentials();
            if (isValidForUploadPost(json) && credentials != null) {
                OoyalaPostRequest request = new OoyalaPostRequest(credentials);
                request.setBody(json);
                String postResponse = ooyalaClient.request(request);

                if (StringUtils.isNotEmpty(postResponse)) {
                    JSONObject postResponseJson = new JSONObject(postResponse);
                    GetAssetUploadingURL urlRequest = new GetAssetUploadingURL(credentials, postResponseJson.getString("embed_code"));
                    String getResponse = ooyalaClient.request(urlRequest);
                    JSONArray array = new JSONArray(getResponse);
                    postResponseJson = postResponseJson.put("uploading_urls", array);
                    return postResponseJson.toString();
                }
            }
        } catch (OoyalaClientException e) {
            logger.error(PERFORM_REQUEST_ERROR_MESSAGE, e);
        }

        return null;
    }

    @Override
    public String setUploadStatus(final JSONObject json, final String assetId, final String requestAction) {
        try {
            OoyalaApiCredential credentials = configService.getCredentials();
            OoyalaPutRequest request = new OoyalaPutRequest(credentials, assetId + "/" + requestAction);
            request.setBody(json);
            final String setUploadStatusResponse = ooyalaClient.request(request);

            if (StringUtils.isNotEmpty(setUploadStatusResponse)) {
                return setUploadStatusResponse;
            }
        } catch (OoyalaClientException e) {
            logger.error(PERFORM_REQUEST_ERROR_MESSAGE, e);
        }

        return null;
    }

    /**
     * Validates that JSON object contains all required keys.
     *
     * @param json object to validate.
     * @return true if JSON object is valid and false otherwise.
     */
    private boolean isValidForUploadPost(final JSONObject json) {
        return json.has("name") && json.has("asset_type") && json.has("file_name") && json.has("file_size");
    }

    @Override
    public String getAllPlaylists(final OoyalaApiCredential ooyalaApiCredential, final int offset, final int limit) throws OoyalaClientException {
        GetPlaylistsRequest getPlaylistsRequest = new GetPlaylistsRequest(ooyalaApiCredential);

        if (limit > 0) {
            getPlaylistsRequest.setLimit(limit);
        }

        if (offset > 0) {
            getPlaylistsRequest.setOffset(offset);
        }

        return ooyalaClient.request(getPlaylistsRequest);
    }

    @Override
    public String getPlaylistInfo(final OoyalaApiCredential ooyalaApiCredential, final String playlistId) throws OoyalaClientException {
        GetPlaylistInfoRequest getPlaylistInfoRequest = new GetPlaylistInfoRequest(ooyalaApiCredential, playlistId);

        return ooyalaClient.request(getPlaylistInfoRequest);
    }
}
