package com.ooyala.aem.assets.viewquery;

import com.day.cq.wcm.core.contentfinder.Hit;
import com.ooyala.aem.client.OoyalaApiCredential;
import com.ooyala.aem.client.OoyalaClientException;
import com.ooyala.aem.service.OoyalaConfigurationService;
import com.ooyala.aem.service.OoyalaService;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.ooyala.aem.assets.AssetConstants.*;

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
 * An Ooyala Playlist specific implementation of {@link OoyalaViewQuery} to build a collection of {@link Hit} objects via Backlot system.
 */
public class PlaylistViewQuery extends OoyalaViewQuery {

    /**
     * Method to construct {@link OoyalaViewQuery} object.
     *
     * @param ooyalaService              - {@link OoyalaService} object.
     * @param ooyalaConfigurationService - {@link OoyalaConfigurationService} object.
     * @param slingHttpServletRequest    - {@link SlingHttpServletRequest} object.
     */
    public PlaylistViewQuery(final OoyalaService ooyalaService, final OoyalaConfigurationService ooyalaConfigurationService,
                             final SlingHttpServletRequest slingHttpServletRequest) {
        super(ooyalaService, ooyalaConfigurationService, slingHttpServletRequest);
    }

    /**
     * Filters an initial JSON object by query string. The name of JSON object should starts with the query string.
     *
     * @param jsonObject An initial JSON object.
     * @param query      A query string to filter by.
     * @return filtered JSON object.
     * @throws JSONException If unable to perform operation under JSON object.
     */
    @Override
    protected JSONObject filterJSONObject(final JSONObject jsonObject, final String query) throws JSONException {
        if (StringUtils.isEmpty(query)) {
            return jsonObject;
        }

        JSONArray hits = jsonObject.getJSONArray(JSON_HITS_KEY);
        JSONArray resultHits = new JSONArray();

        for (int i = 0; i < hits.length(); i++) {
            JSONObject hit = hits.getJSONObject(i);

            String name = hit.getString(JSON_NAME_KEY);

            if (StringUtils.isNotEmpty(name) && StringUtils.startsWithIgnoreCase(name, query)) {
                resultHits.put(hit);
            }
        }

        return new JSONObject().put(JSON_HITS_KEY, resultHits);
    }

    /**
     * Ooyala Playlist specific method to execute query.
     *
     * @param ooyalaApiCredential an instance of {@link OoyalaApiCredential} class.
     * @param searchBy            search criteria.
     * @param query               query string.
     * @param offsetAndLimit      an array with query offset and limit.
     * @return raw response in String representation.
     * @throws OoyalaClientException if unable to perform request.
     */
    @Override
    protected String executeQuery(final OoyalaApiCredential ooyalaApiCredential, final String searchBy, final String query,
                                  final int[] offsetAndLimit) throws OoyalaClientException {
        return ooyalaService.getAllPlaylists(ooyalaApiCredential, offsetAndLimit[0], offsetAndLimit[1]);
    }

    /**
     * Manipulates the response JSON from Ooyala to fit the needs of built-in AEM functionality.
     * Ooyala Video specific implementation.
     *
     * @param response The raw JSON response from Ooyala.
     * @return A well formed JSON response.
     * @throws JSONException in case of JSON object constructing is failed.
     */
    @Override
    protected JSONObject getJSON(final String response) throws JSONException {
        if (StringUtils.isNotEmpty(response)) {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray items = jsonObject.getJSONArray(JSON_ITEMS_KEY);

            return new JSONObject().put(JSON_HITS_KEY, items);
        }

        return null;
    }

    /**
     * Builds {@link Hit} object with Ooyala Playlist specific fields from JSON object.
     *
     * @param jsonObject - initial JSON object.
     * @return {@link Hit} object - representation of JSON object.
     * @throws JSONException if JSON error is occurred.
     */
    @Override
    protected Hit createHit(final JSONObject jsonObject) throws JSONException {
        Hit hit = new Hit();

        hit.set(HIT_PATH_PROP, getPropValue(jsonObject, JSON_ID_KEY));
        hit.set(HIT_TITLE_PROP, getPropValue(jsonObject, JSON_NAME_KEY));
        hit.set(HIT_ADDITIONAL_PROP, "Playlist");
        hit.set(HIT_LAST_MODIFIED_PROP, getPropValue(jsonObject, JSON_UPDATED_AT_KEY));

        return hit;
    }
}
