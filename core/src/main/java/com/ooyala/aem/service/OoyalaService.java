package com.ooyala.aem.service;

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

import com.ooyala.aem.client.OoyalaApiCredential;
import com.ooyala.aem.client.OoyalaClientException;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Provides high level access to the Ooyala RESTful API.
 */
public interface OoyalaService {

    /**
     * A getter for the list of metadata keys.
     *
     * @return The list of metadata keys from the configuration service.
     */
    List<String> getMetaKeys();

    String getAllVideos(OoyalaApiCredential credentials, int offset, int limit) throws OoyalaClientException;

    /**
     * Constructs a request for videos, based on the parameters, and sends it to Ooyala.
     *
     * @param credentials The {@link OoyalaApiCredential} object for authenticating the API request.
     * @param searchBy    The type of search to be performed. Available options are "searchByTitle", "searchByLabel", "searchByDescription",
     *                    "searchByMeta".
     * @param queryString The query string to execute, in conjunction with the searchBy parameter.
     * @param offset      The request's offest, used by the API in determining the sequence of results to return.
     * @param limit       The request's limit, the maximum results to be returned from the request.
     * @return The JSON string returned by Ooyala's API.
     * @throws OoyalaClientException If unable to perform request
     */
    String getVideos(OoyalaApiCredential credentials, String searchBy, String queryString, int offset, int limit)
            throws OoyalaClientException;

    /**
     * Constructs a request for video information, based on the video name, and sends it to Ooyala.
     *
     * @param ooyalaApiCredentials The {@link OoyalaApiCredential} object for authenticating the API request.
     * @param name                 A name of a video to get an information about from Backlot.
     * @return The JSON string returned by Ooyala's API.
     * @throws OoyalaClientException If unable to perform request.
     */
    String getVideo(OoyalaApiCredential ooyalaApiCredentials, String name) throws OoyalaClientException;

    /**
     * Constructs a request for labels, without any query, and sends it to Ooyala.
     *
     * @param limit The request's limit, the maximum results to be returned from the request.
     * @return The JSON string returned by Ooyala's API.
     */
    String getLabels(int limit);

    /**
     * Constructs a request for players, without any query, and sends it to Ooyala.
     *
     * @return The JSON string returned by Ooyala's API.
     */
    String getAllPlayers();

    /**
     * Proxies the uploader post request to the Ooyala API using the current credentials.
     *
     * @param json The raw JSON object to send as the body.
     * @return string representation of JSON response.
     * @throws JSONException in case of unable to create JSON object by string
     */
    String initUpload(JSONObject json) throws JSONException;

    /**
     * Set upload status for an asset.
     *
     * @param json          The request body in JSON format representation.
     * @param assetId       The embed code of an asset.
     * @param requestAction The request action.
     * @return The response of the request from Backlot.
     */
    String setUploadStatus(JSONObject json, String assetId, String requestAction);

    /**
     * Constructs a request for playlists and sends it to Ooyala.
     *
     * @param ooyalaApiCredential The {@link OoyalaApiCredential} object for authenticating the API request.
     * @param offset              The request's offest, used by the API in determining the sequence of results to return.
     * @param limit               The request's limit, the maximum results to be returned from the request.
     * @return raw Ooyala JSON response in String representation.
     * @throws OoyalaClientException If unable to perform request.
     */
    String getAllPlaylists(OoyalaApiCredential ooyalaApiCredential, int offset, int limit) throws OoyalaClientException;

    /**
     * Constructs a request for playlist information and sends it to Ooyala.
     *
     * @param ooyalaApiCredential The {@link OoyalaApiCredential} object for authenticating the API request.
     * @param playlistId          The playlist id to get info for.
     * @return Playlist information from Ooyala.
     * @throws OoyalaClientException If unable to perform request.
     */
    String getPlaylistInfo(OoyalaApiCredential ooyalaApiCredential, String playlistId) throws OoyalaClientException;
}
