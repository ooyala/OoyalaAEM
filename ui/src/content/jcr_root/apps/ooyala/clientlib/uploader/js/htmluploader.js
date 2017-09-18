(function($) {
    /**
     * Pieces of code taken from resumable.js at https://github.com/23/resumable.js
     * */

    /**
     * Detect if the browser has support for the File API calls we need.
     * @private
     * */
    function isFileAPISuppported() {
        return typeof File !== "undefined" && File.prototype.slice;
    }

    function UploadableChunk(url, chunkProvider, maxNumberOfRetries) {
        window.Ooyala.Client.EventDispatcher.call(this);
        this.url = url;
        this.chunkProvider = chunkProvider;
        this.maxNumberOfRetries = maxNumberOfRetries;
        this.retriesSoFar = 0;
    }

    $.extend(UploadableChunk.prototype, new window.Ooyala.Client.EventDispatcher(), {
        upload: function(error) {
            //Dumb retries. We don't care about the underlying error, we just try again until we hit the retries limit.
            if (this.retriesSoFar > this.maxNumberOfRetries) {
                this.dispatchEvent("error", [error]);
            }
            else {
                this.retriesSoFar++;
                this.transferBytesOverTheWire();
            }
        },

        /**
         * This function does the heavy lifting of sending the bytes over to the uploading endpoint.
         * @private
         * */
        transferBytesOverTheWire: function() {
            const matchResults = this.url.match(/.+\/(.+)-([^&]+)/);
            const startByte = parseInt(matchResults[1], 10);
            const endByte = parseInt(matchResults[2], 10) + 1;
            const that = this;

            function onComplete() {
                that.chunkProvider.detach("complete", onComplete);
                const bytes = that.chunkProvider.data;

                const xhr = new XMLHttpRequest();
                xhr.addEventListener("load", function() {that.dispatchEvent("complete");});
                xhr.addEventListener("error", function(e) {that.upload(e);}); //Retry

                xhr.open("POST", that.url);

                if (window.Blob && document.getElementById("flashChunkProvider") == null) {
                    const data = new FormData();
                    data.append("chunk", bytes);
                    xhr.send(data);
                }
                else {
                    const boundary = "--------------------------" + Math.random().toString().replace("0.","");
                    const body = "--" + boundary + '\r\nContent-Disposition: form-data; name="file"; filename="blob"\r\nContent-Type: application/octet-stream' + (document.getElementById('flashChunkProvider') ? '; Content-Transfer-Encoding: base64' : '' ) + '\r\n\r\n' + bytes + "\r\n--" + boundary + "--\r\n";
                    xhr.setRequestHeader("content-type", "multipart/form-data; charset=x-user-defined-binary; boundary=" + boundary);
                    xhr.send(body);
                }
            }
            this.chunkProvider.on("complete", onComplete);
            this.chunkProvider.getChunk(startByte, endByte);
        }
    });

    /**
     * HTMLUploader Object to interact with either the HTML5 File API if available or fallback 
     * to Flash file slicing and doing a chunked upload via HTTP.
     * */
    window.Ooyala.Client.HTMLUploader = function(browseButton, options) {
        window.Ooyala.Client.Uploader.call(this);
        this.chunksUploaded = 0;
        this.totalChunks = this.uploadingURLs.length;
        this.currentChunks = [];
        this.isError = false;
        this.browseButton = browseButton;
        this.chunkSize = 1*1024*1024;
        const that = this;

        const defaults = {
            maxChunkRetries: 3,
            maxNumberOfConcurrnetChunks: 1
        };

        this.options = $.extend(defaults, options);

        if (isFileAPISuppported()) {
            this.chunkProvider = new window.Ooyala.Client.HTML5ChunkProvider(this.file, this.browseButton);
        }
        else {
            throw new Error("Your browser does not support HTML5 file APIs.");
        } 

        this.chunkProvider.on("fileSelected", function() {
            that.file = that.chunkProvider.file;
            window.uploader.file = that.chunkProvider.file;
        });
    };

    $.extend(window.Ooyala.Client.HTMLUploader.prototype, new window.Ooyala.Client.Uploader, {
        /**
         * Start uploading the selected file.
         * */
        upload: function() {
            this.isError = false; // If previously error has been thrown
            this.totalChunks = this.uploadingURLs.length;
            for (let i = 0; i < this.options.maxNumberOfConcurrnetChunks; i++) {
                this.uploadNextChunk();
            }
        },

        /**
         * Upload the next chunk fetch from either the HTML5 or Flash chunk providers
         * */
        uploadNextChunk: function() {
            const that = this;
            const urlToUpload = this.uploadingURLs.pop();

            this.dispatchEvent("progress");

            //Stop if we are done uploading chunks and dispatch complete event.
            if (!urlToUpload) {
                this._reset();
                this.dispatchEvent("complete");
                return;
            }
            
            //Stop if there has been an error trying to upload a chunk.
            if (this.isError) {
                return;
            }

            const uploadableChunk = new UploadableChunk(urlToUpload, this.chunkProvider, that.options.maxChunkRetries);
            //Upload the next chunk if this one has completed uploading
            uploadableChunk.on("complete", function() {
                that.chunksUploaded++; 
                that.uploadNextChunk();
            });

            //If an error is thrown by one of the chunks, 
            //set the flag to stop the ingestion.
            uploadableChunk.on("error", function(e) {
                that.dispatchEvent("error", [e]);
                that.isError = true;
                that._reset();
            });
            uploadableChunk.upload();
        },

        progress: function() {
            return this.chunksUploaded == 0 ? 0 : (this.chunksUploaded / this.totalChunks);
        },
        _reset: function() {
            this.chunksUploaded = 0;
        }
    });
}).call(this, jQuery);
