$(document).ready(function() {
    "use strict";

    function setPlayerParam(value) {
        if (value) {
           return value + "px";
        }
    }

    function callback($ooyalaVideoContainer, isPlaylist) {
        const videoId = $ooyalaVideoContainer.attr('data-videoId'),
              videoContainerId = $ooyalaVideoContainer.attr('id'),
              videoContainerWidth = $ooyalaVideoContainer.attr('data-width'),
              videoContainerHeight = $ooyalaVideoContainer.attr('data-height'),
              autoPlay = $ooyalaVideoContainer.attr('data-autoPlay'),
              playerSettings = {};

        playerSettings.height = setPlayerParam(videoContainerHeight);
        playerSettings.width = setPlayerParam(videoContainerWidth);
        playerSettings.autoplay = autoPlay === 'true'

        if (videoId) {
            window.OO.ready(function() {
                if (isPlaylist) {
                    $.extend(playerSettings, {
                        playlistsPlugin: {
                            data: [videoId]
                        },
                        useFirstVideoFromPlaylist: true
                    });
                }
                window.OO.Player.create(videoContainerId, isPlaylist ? "" : videoId, playerSettings);
            });
        }
    }

    function loadExternalScript(url, callback) {
        $.ajax({
            url: url,
            dataType: 'script',
            success: callback,
            async: true
        });
    }

    const $ooyalaVideoContainer = $('.ooyalaVideoDiv');
    const playerId = $ooyalaVideoContainer.attr('data-playerId');

    if ($ooyalaVideoContainer.length && playerId) {
        const playerScriptUrl = 'http://player.ooyala.com/core/' + playerId;
        const playlistScriptUrl = 'http://player.ooyala.com/static/v4/production/other-plugin/playlists.js';

        $.ajax({
            url: "/bin/wcm/ooyala/playlist",
            data: {
                playlistId: $ooyalaVideoContainer.attr('data-videoId')
            },
            dataType: "json",
            success: function() {
                loadExternalScript(playerScriptUrl, function() {
                    loadExternalScript(playlistScriptUrl, function() {
                            callback($ooyalaVideoContainer, true);
                    });
                });
            },
            error: function() {
                loadExternalScript(playerScriptUrl, function() {
                    callback($ooyalaVideoContainer);
                });
            }
        });
    }
});