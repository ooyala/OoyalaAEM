package com.ooyala.aem.servlet;

import com.ooyala.aem.service.OoyalaService;
import org.apache.commons.lang.CharEncoding;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        service = Servlet.class,
        property = {
                "sling.servlet.paths=/bin/ooyala/upload/assets"
        }
)
public class UploadProxyServlet extends SlingAllMethodsServlet {
    private static final Logger logger = LoggerFactory.getLogger(UploadProxyServlet.class);

    private static final long serialVersionUID = -3738183473154158783L;

    private final Pattern assetIdPattern = Pattern.compile("^(.*?assets/)(.*?)/(.*)$");

    private static final String UPLOAD_STATUS_ACTION = "upload_status";

    @Reference
    private transient OoyalaService ooyalaService;

    @Override
    protected void doPost(@Nonnull SlingHttpServletRequest request, @Nonnull SlingHttpServletResponse response) throws ServletException, IOException {
        final String requestURI = request.getRequestURI();
        final String requestAction = getActionFromPath(requestURI);

        try {
            final StringBuilder stringBuilder = new StringBuilder();
            final InputStream inputStream = request.getInputStream();

            if (inputStream != null) {
                final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, CharEncoding.UTF_8));
                final char[] charBuffer = new char[128];
                int bytesRead;
                while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
                    stringBuilder.append(charBuffer, 0, bytesRead);
                }
            }

            JSONObject requestJson = new JSONObject(stringBuilder.toString());

            if (StringUtils.isNotEmpty(requestAction) && UPLOAD_STATUS_ACTION.equals(requestAction)) {
                final String assetId = getAssetIdFromPath(requestURI);
                final String uploadStatusResponse = ooyalaService.setUploadStatus(requestJson, assetId, requestAction);
                if (StringUtils.isNotEmpty(uploadStatusResponse)) {
                    response.setStatus(200);
                    response.getWriter().write(uploadStatusResponse);
                } else {
                    response.setStatus(500);
                }
            } else {
                final String initUploadResponse = ooyalaService.initUpload(requestJson);
                if (StringUtils.isNotEmpty(initUploadResponse)) {
                    response.setStatus(200);
                    response.getWriter().write(initUploadResponse);
                } else {
                    response.setStatus(500);
                }
            }
        } catch (JSONException e) {
            logger.error("JSON object creation failed", e);
            response.sendError(500);
        }
    }

    private String getActionFromPath(final String path) {
        final Matcher matcher = assetIdPattern.matcher(path);
        if (matcher.matches()) {
            return matcher.group(3);
        } else {
            return null;
        }
    }

    private String getAssetIdFromPath(final String path) {
        final Matcher matcher = assetIdPattern.matcher(path);
        if (matcher.matches()) {
            return matcher.group(2);
        } else {
            return null;
        }
    }
}
