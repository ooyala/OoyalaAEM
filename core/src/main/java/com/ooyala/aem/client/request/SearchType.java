package com.ooyala.aem.client.request;

import org.apache.commons.lang.StringUtils;

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
 * Enumeration with set of supported search types.
 */
public enum SearchType {
    SEARCH_BY_LABEL("searchByLabel", "label"),
    SEARCH_BY_TITLE("searchByTitle", "name"),
    SEARCH_BY_DESCRIPTION("searchByDescription", "description"),
    SEARCH_BY_META("searchByMeta", "metadata."),
    SEARCH_BY_PLAYLIST("searchByPlaylist", StringUtils.EMPTY),
    UNSPECIFIED(StringUtils.EMPTY, StringUtils.EMPTY);

    private String type;
    private String queryParameter;

    SearchType(String type, String queryParameter) {
        this.type = type;
        this.queryParameter = queryParameter;
    }

    public String getType() {
        return type;
    }

    public String getQueryParameterName() {
        return queryParameter;
    }

    public static SearchType getSearchType(final String type) {
        for (SearchType searchType : SearchType.values()) {
            if (searchType.getType().equals(type)) {
                return searchType;
            }
        }

        throw new IllegalArgumentException(String.format("Unknown search type '%s'", type));
    }
}
