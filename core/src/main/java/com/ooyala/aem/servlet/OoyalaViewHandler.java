package com.ooyala.aem.servlet;

import com.day.cq.commons.feed.StringResponseWrapper;
import com.day.cq.wcm.core.contentfinder.Hit;
import com.day.cq.wcm.core.contentfinder.ViewHandler;
import com.day.cq.wcm.core.contentfinder.ViewQuery;
import com.ooyala.aem.assets.AssetConstants;
import com.ooyala.aem.assets.viewquery.OoyalaViewQuery;
import com.ooyala.aem.assets.viewquery.OoyalaViewQueryFactory;
import com.ooyala.aem.service.OoyalaService;
import com.ooyala.aem.service.OoyalaConfigurationService;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestDispatcherOptions;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.Session;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Collection;

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
 * Ooyala's implementation of {@link ViewHandler} to use specific {@link ViewQuery} implementation.
 */
@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.paths=/bin/wcm/ooyalaViewHandler"
        }
)
public class OoyalaViewHandler extends ViewHandler {

    @Reference
    private transient OoyalaService ooyalaService;

    @Reference
    private transient OoyalaConfigurationService ooyalaConfigurationService;

    /**
     * Overridden parent's method to return OoyalaViewQuery.
     *
     * @param slingHttpServletRequest - servlet request.
     * @param session - JCR session.
     * @param queryString query in String representation.
     * @return {@link OoyalaViewQuery}
     * @throws Exception if any error is occurred.
     */
    @Override
    protected ViewQuery createQuery(SlingHttpServletRequest slingHttpServletRequest, Session session, String queryString) throws Exception {
        return OoyalaViewQueryFactory.getViewQuery(ooyalaService, ooyalaConfigurationService, slingHttpServletRequest);
    }

    /**
     * Ooyala's specific implementation to generate Coral Card representation of {@link Hit} objects.
     *
     * @param hits - a collection of {@link Hit} objects.
     * @param request - servlet request.
     * @param response - servlet response.
     * @return String representation of HTML response.
     * @throws ServletException - if servlet error occurred.
     * @throws IOException - if I/O error occurred.
     */
    @Override
    protected String generateHtmlOutput(Collection<Hit> hits, SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {

        StringResponseWrapper stringResponseWrapper = new StringResponseWrapper(response);

        RequestDispatcherOptions requestDispatcherOptions = new RequestDispatcherOptions(null);

        requestDispatcherOptions.setForceResourceType(request.getParameter("itemResourceType"));

        for (Hit hit: hits) {
            Resource syntheticResource = request.getResourceResolver().getResource("/etc/ooyala/synthetic-resource/synthetic-resource");

            if (syntheticResource != null) {
                request.setAttribute(AssetConstants.HIT_REQUEST_ATTR, hit);

                RequestDispatcher dispatcher = request.getRequestDispatcher(syntheticResource.getPath() + ".html", requestDispatcherOptions);

                if (dispatcher != null) {
                    dispatcher.include(request, stringResponseWrapper);
                    request.removeAttribute(AssetConstants.HIT_REQUEST_ATTR);
                }
            }
        }

        return stringResponseWrapper.getString();
    }
}
