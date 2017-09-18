package com.ooyala.aem.models;

import com.day.cq.wcm.core.contentfinder.Hit;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

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
 * Sling Model to represent {@link Hit} object as Ooyala Asset.
 */
@Model(adaptables = SlingHttpServletRequest.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class OoyalaAssetModel {

    private static final Logger logger = LoggerFactory.getLogger(OoyalaAssetModel.class);

    private String imageUrl;
    private String title;
    private String additionalProperty;
    private String path;

    @Inject
    public OoyalaAssetModel(@Self SlingHttpServletRequest slingHttpServletRequest) {
        Object hitRequestAttr = slingHttpServletRequest.getAttribute(HIT_REQUEST_ATTR);

        if (hitRequestAttr != null) {
            Hit hit = (Hit) hitRequestAttr;

            imageUrl = (String) hit.get(HIT_IMAGE_URL_PROP);
            title = (String) hit.get(HIT_TITLE_PROP);
            additionalProperty = (String) hit.get(HIT_ADDITIONAL_PROP);
            path = (String) hit.get(HIT_PATH_PROP);
        } else {
            logger.error(String.format("Request contains no '%s' attribute.", HIT_REQUEST_ATTR));
        }
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getAdditionalProperty() {
        return additionalProperty;
    }

    public String getPath() {
        return path;
    }
}
