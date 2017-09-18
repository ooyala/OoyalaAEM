(function($) {
    /**
     * Provision the browseElement with the file selector
     * @private
     * @note:Technique to add the invisible file selector taken from Resummable.js (https://github.com/23/resumable.js)
     * */
    function initHTMLFileSelector() {
        //This function is suppposed to be called using the context of the owner Object,
        //which in this case is the Ooyala.Client.HTMLUplaoder.
        const that = this;
        const $form = $(this.browseButton).closest("form")
        const $sel = $("<input />");
        $sel.attr("type", "file");

        $sel.css({
            position: "absolute",
            top: "0",
            left: "0",
            width: "0",
            height: "0",
            overflow: "hidden"
        });

        $form.append($sel);

        $sel.on("change", function() {
            that.file = $sel.get(0).files[0];
            that.dispatchEvent("fileSelected");
        });

        $form.on("reset", function() {
            that.file = null;
            that.dispatchEvent("fileSelected");
        });
    }

    window.Ooyala.Client.HTML5ChunkProvider = function(file, browseButton) {
        window.Ooyala.Client.EventDispatcher.call(this);
        this.file = file;
        this.data = "";
        this.browseButton = browseButton;
        //Call private method to initialize the file selector button.
        initHTMLFileSelector.call(this);
    };

    $.extend(window.Ooyala.Client.HTML5ChunkProvider.prototype, new window.Ooyala.Client.EventDispatcher(), {
        getChunk: function(startByte, endByte) {
            if (!this.file) {
                throw new Error("No file has been selected yet.");
            }

            this.data = this.file.slice(startByte, endByte);
            this.dispatchEvent("complete");
        }
    });
}).call(this, jQuery);
