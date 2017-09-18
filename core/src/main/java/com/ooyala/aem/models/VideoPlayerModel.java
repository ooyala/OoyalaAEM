package com.ooyala.aem.models;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;

import javax.inject.Inject;
import javax.inject.Named;

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
 * Sling Model which represents Ooyala Video Player setup node.
 */
@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class VideoPlayerModel {

    private static final String COMPONENT_NOT_READY_MESSAGE = "Drag video here to replace.";
    private static final String COMPONENT_READY_MESSAGE = "Drag video here to replace. Switch to preview mode to see the video.";

    private String videoId;
    private String playerId;
    private String width;
    private String height;
    private String autoPlay;

    @Inject
    public VideoPlayerModel(@Named("videoId") String videoId,
                            @Named("playerId") String playerId,
                            @Named("width") String width,
                            @Named("height") String height,
                            @Named("autoPlay") String autoPlay) {
        this.videoId = videoId;
        this.playerId = playerId;
        this.width = width;
        this.height = height;
        this.autoPlay = autoPlay;
    }

    public String getVideoId() {
        return videoId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getWidth() {
        return width;
    }

    public String getHeight() {
        return height;
    }

    public String getAutoPlay() {
        return autoPlay;
    }

    /**
     * Generate video container id.
     *
     * @return id of video container or empty string in case of videoId is not defined.
     */
    public String getVideoContainerId() {
        return StringUtils.isNotEmpty(videoId)
                ? ("container-" + videoId)
                : StringUtils.EMPTY;
    }

    /**
     * Generate height style rule for video container based on dialog properties.
     * E.g. height: 150px;
     *
     * @return height style rule or empty string in case of height is not defined.
     */
    public String getHeightStyleRule() {
        return StringUtils.isNotEmpty(height)
                ? createStyleRule("height", height)
                : StringUtils.EMPTY;
    }

    /**
     * Generate width style rule for video container based on dialog properties.
     * E.g. width: 270px;
     *
     * @return width style rule or empty string in case of width is not defined.
     */
    public String getWidthStyleRule() {
        return StringUtils.isNotEmpty(width)
                ? createStyleRule("width", width)
                : StringUtils.EMPTY;
    }

    /**
     * Generates message to drag'n'drop container.
     *
     * @return String representation of drag'n'drop container message.
     */
    public String getEmptyMessage() {
        return StringUtils.isNotEmpty(videoId) ? COMPONENT_READY_MESSAGE : COMPONENT_NOT_READY_MESSAGE;
    }

    /**
     * Return component ready state depending on videoId and playerId values.
     *
     * @return true case of videoId and playerId are not empty and false otherwise.
     */
    public boolean isComponentReady() {
        return StringUtils.isNotEmpty(videoId) && StringUtils.isNotEmpty(playerId);
    }

    /**
     * Generate style rule.
     *
     * @param rule  css rule. E.g. "height".
     * @param value of the rule. E.g. "400".
     * @return generated css style rule. E.g. "height: 400px";
     */
    private String createStyleRule(final String rule, final String value) {
        return String.format("%s: %spx;", rule, value);
    }
}
