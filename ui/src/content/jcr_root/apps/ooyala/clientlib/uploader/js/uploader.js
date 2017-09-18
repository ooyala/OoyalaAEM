/**
* Base class for Uploaders
* */
(function($) {
    window.Ooyala.Client.Uploader = function() {
        window.Ooyala.Client.EventDispatcher.call(this);
        this.chunk_size = 4194304;
        this.file = null;
        this.uploadingURLs = [];
        this.browseButton = null;
    };

    $.extend(window.Ooyala.Client.Uploader.prototype, new window.Ooyala.Client.EventDispatcher(), {
        setUploadingURLs: function(urls) {
            this.uploadingURLs = urls;
        },

        upload: function() {
            throw new Error("This method should be implemented by a child object");
        },

        progress: function() {
            return 0;
        },

        assignBrowse: function(browseButton) {
            this.browseButton = browseButton;
        }
    });
}).call(this, jQuery);
