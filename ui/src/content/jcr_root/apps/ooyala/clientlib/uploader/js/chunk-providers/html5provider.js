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
        const sel = document.createElement("input");
        sel.type = "file";

        sel.style.position = "absolute";
        sel.style.top = sel.style.left = sel.style.width = sel.style.height = 0;
        sel.style.overflow = "hidden";

        this.browseButton.closest("form").appendChild(sel);

        sel.addEventListener("change", function() {
            that.file = sel.files[0];
            that.dispatchEvent("fileSelected");
        }, false);

        sel.closest("form").addEventListener("reset", function() {
            that.file = null;
            that.dispatchEvent("fileSelected");
        }, false)
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
