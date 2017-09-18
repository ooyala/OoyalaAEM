package com.ooyala.aem.assets.viewquery;

import com.ooyala.aem.service.OoyalaService;
import com.ooyala.aem.client.request.SearchType;
import com.ooyala.aem.service.OoyalaConfigurationService;
import org.apache.sling.api.SlingHttpServletRequest;

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
 * Factory class to construct {@link OoyalaViewQuery} object.
 */
public class OoyalaViewQueryFactory {
    private static final String SEARCH_BY_REQUEST_PARAMETER = "searchBy";

    private OoyalaViewQueryFactory() {}

    /**
     * Construct {@link OoyalaViewQuery} object depends on 'searchBy' request parameter.
     *
     * @param ooyalaService              - {@link OoyalaService} object.
     * @param ooyalaConfigurationService - {@link OoyalaConfigurationService} object.
     * @param slingHttpServletRequest    - {@link SlingHttpServletRequest} object.
     * @return a specific instance of {@link OoyalaViewQuery} object.
     */
    public static OoyalaViewQuery getViewQuery(final OoyalaService ooyalaService, final OoyalaConfigurationService ooyalaConfigurationService,
                                               final SlingHttpServletRequest slingHttpServletRequest) {
        String searchBy = slingHttpServletRequest.getParameter(SEARCH_BY_REQUEST_PARAMETER);
        SearchType searchType = SearchType.getSearchType(searchBy);

        if (SearchType.SEARCH_BY_PLAYLIST.equals(searchType)) {
            return new PlaylistViewQuery(ooyalaService, ooyalaConfigurationService, slingHttpServletRequest);
        } else {
            return new VideoViewQuery(ooyalaService, ooyalaConfigurationService, slingHttpServletRequest);
        }
    }
}
