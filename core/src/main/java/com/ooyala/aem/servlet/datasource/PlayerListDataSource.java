package com.ooyala.aem.servlet.datasource;

import com.adobe.granite.ui.components.ds.DataSource;
import com.adobe.granite.ui.components.ds.SimpleDataSource;
import com.adobe.granite.ui.components.ds.ValueMapResource;
import com.day.cq.commons.jcr.JcrConstants;
import com.ooyala.aem.service.OoyalaService;
import org.apache.commons.collections.iterators.TransformIterator;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
 * Data source for available players which is used in dialog of Ooyala OoyalaVideo Player component.
 */
@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.resourceTypes=ooyala/components/playerListDataSource"
        }
)
public class PlayerListDataSource extends SlingSafeMethodsServlet {

    private static final Logger logger = LoggerFactory.getLogger(PlayerListDataSource.class);

    @Reference
    private transient OoyalaService ooyalaService;

    @Override
    protected void doGet(@Nonnull SlingHttpServletRequest request, @Nonnull SlingHttpServletResponse response) throws ServletException, IOException {
        Map<String, String> playersMap = new HashMap<>();
        String allPlayersString = ooyalaService.getAllPlayers();

        if (StringUtils.isNotEmpty(allPlayersString)) {
            try {
                JSONObject allPlayersJSON = new JSONObject(allPlayersString);
                JSONArray items = allPlayersJSON.getJSONArray("items");

                for (int i = 0; i < items.length(); i++) {
                    JSONObject currItem = items.getJSONObject(i);
                    playersMap.put(currItem.getString("id"), currItem.getString("name"));
                }

                DataSource dataSource = new SimpleDataSource(new TransformIterator(playersMap.keySet().iterator(), o -> {
                    String player = (String) o;
                    ValueMap valueMap = new ValueMapDecorator(new HashMap<>());

                    valueMap.put("text", playersMap.get(player));
                    valueMap.put("value", player);

                    return new ValueMapResource(request.getResourceResolver(), new ResourceMetadata(), JcrConstants.NT_UNSTRUCTURED, valueMap);
                }));

                request.setAttribute(DataSource.class.getName(), dataSource);
            } catch (JSONException e) {
                logger.error("Unable to create JSON object by OoyalaService response", e);
            }
        } else {
            logger.error("Unable to get predefined player list from OoyalaService");
        }
    }
}
