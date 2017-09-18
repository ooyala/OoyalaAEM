/**
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
(function($, ns, channel, window, undefined) {

    const self = {},
        name = 'Ooyala';

    self.loadAssets = function(query, lowerLimit, upperLimit) {
        const queryObj = {};

        if (query.indexOf('"') !== -1) {
            queryObj.query = query.substring(0, query.indexOf('"')).trim();
        } else {
            queryObj.query = query.trim();
        }

        query.split(/\s+/).forEach(function(item) {
            const resArr = /^"(\S+)":"(\S+)"$/.exec(item);
            if (resArr !== null) {
                if (queryObj[resArr[1]] === undefined) {
                    queryObj[resArr[1]] = resArr[2];
                } else {
                    queryObj[resArr[1]] = queryObj[resArr[1]] + ',' + resArr[2];
                }
            }
        });

        const param = {};

        if (queryObj.searchBy === undefined) {
            param.searchBy = '';
        } else {
            param.searchBy = queryObj.searchBy;
        }

        if (queryObj.query === undefined) {
            param.query = '';
        } else {
            param.query = queryObj.query;
        }

        param.limit =  lowerLimit + '..' + upperLimit;

        param.itemResourceType = getResourceTypeBySearchType(param.searchBy);
        param['_charset_'] = 'utf-8';

        return $.ajax({
            type: 'GET',
            dataType: 'html',
            url: '/bin/wcm/ooyalaViewHandler',
            data: param
        });

        // Returns single resource type in case of search by labels, title, description and metadata.
        // Returns different resource type in case of search by playlist.
        function getResourceTypeBySearchType(searchType) {
            if ('searchByPlaylist' === searchType) {
                return 'ooyala/components/content/ooyalaPlaylistAsset';
            }

            return 'ooyala/components/content/ooyalaAsset';
        }
    };

    // register as a asset tab
    ns.ui.assetFinder.register(name, self);

}(jQuery, window.Granite.author, jQuery(document), this));