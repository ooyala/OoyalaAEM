(function($) {
    window.Ooyala.Client.Events = {
        PROGRESS: "progress",
        COMPLETE: "complete",
        ERROR: "error",
        ASSET_CREATION_COMPLETE: "assetCreationComplete"
    };

    /**
     * Upload assets to window.Ooyala using the chunked upload API.
     *
     * @param endpoint
     *   The absolute URL to the endpoint the API we use to prepare an upload
     *   and notify when an upload is complete.
     * @param browseButton
     *   The ID or DOM object of the button used to trigger browsing for an
     *   asset to upload.
     * @param dropArea
     *   The DOM object of an area to use for Drag and Drop uploads of assets.
     * @param options
     *   An optional object of options to use when instantiating the uploader
     *   with the following properties:
     *   - useAspera: Set to TRUE to use the Aspera Uploader. Currently
     *     unimplemented.
     *   - postProcessingStatus: Set the post processing options for the asset.
     *     Currently unimplemented.
     */
    window.Ooyala.Client.AssetCreator = function(endpoint, browseButton, dropArea, options) {
        window.Ooyala.Client.EventDispatcher.call(this);
        if (typeof jQuery == "undefined") {
            throw new Error("This uploader needs jQuery 1.5+ to be loaded.");
        }
        if (typeof JSON == "undefined") {
            throw new Error("This uploader depend's on Douglas Crockford's JSON parser (https://github.com/douglascrockford/JSON-js) " +
                            "or one of the following browsers: Internet Explorer 8+, Firefox 3.1+, Safari 4+, Chrome 3+, and Opera 10.5+.");
        }
        if (!endpoint) {
            throw new Error("Please provide an endpoint URL.");
        }
        if (!browseButton && !dropArea) {
            throw new Error("You need to provide either a button element to fire the file browsing action or a file drop area element.");
        }

        if (typeof browseButton === "string") {
            browseButton = document.getElementById(browseButton);
        }

        this.browseButton = browseButton;
        this.dropArea = dropArea;
        this.endpoint = endpoint;
        this.options = options || {};
        this.eventNames = window.Ooyala.Client.Events;

        if (this.options.useAspera) {
            //TODO: Implement Aspera Uploader and plug it in here.
        }
        else {//Default back to the HTML Uploader
            this.uploader = new window.Ooyala.Client.HTMLUploader(browseButton, options);
        }

        const that = this;

        this.uploader.on(this.eventNames.PROGRESS, function() {that.progressHandler();});
        this.uploader.on(this.eventNames.COMPLETE, function() {that.uploadCompleteHandler();});
        this.uploader.on(this.eventNames.ERROR, function() {that._errorHandler();});

        if (this.browseButton) {
            this.uploader.assignBrowse(this.browseButton);
        }

        this.assetToUpload = null;
        this.embedCode = "";
        this.__asyncOperationsControl = [];
    };

    $.extend(window.Ooyala.Client.AssetCreator.prototype, new window.Ooyala.Client.EventDispatcher, {
        /**
         * Prepare to upload an window.Ooyala asset.
         *
         * This method will validate the information provided by the user (such as
         * ensuring that they selected a file) and if everything passes reserve an
         * embed code with the window.Ooyala backlot. The event fired from sucessfully
         * creating the embed code will trigger the actual upload of the asset.
         *
         * @param name
         *   The name of the asset to create.
         * @param description
         *   The short text description of the asset.
         */
        prepareUpload: function(name, description) {
            //Dispatch error event if the user has not selected a file
            if (!this.uploader.file) {
                this._errorHandler("The user has not selected a file.");
                return;
            }

            const fileToUpload = this.uploader.file;

            this.assetToUpload = {
                name: name,
                description: description,
            };

            //Take into consideration the Post Processing Status option if present
            if (this.options.postProcessingStatus) {
                window.body.postProcessingStatus = this.options.postProcessingStatus;
            }

            // Send the asset creation call to the API Proxy and fire the
            // corresponding events.
            this.createAsset(this.assetToUpload.name, this.assetToUpload.description, fileToUpload.name, fileToUpload.size, this.uploader.chunkSize);
        },

        upload: function() {
            const that = this;

            //If we don't have an embed code, it means that the asset has not been created.
            //Fire an error event in that case.
            if (!this.embedCode) {
                this.dispatchEvent(this.eventNames.ERROR, ["An asset has not been created."]);
                return;
            }

            that.uploader.upload();
        },

        progress: function() {
            return this.uploader ? this.uploader.progress() : 0;
        },

        progressHandler: function() {
            this.dispatchEvent(this.eventNames.PROGRESS);
        },

        uploadCompleteHandler: function() {
            const that = this;

            //Update the uploading_status of the asset via the API Proxy
            this._makeAPICall("PUT", "assets/" + this.embedCode + "/upload_status", {"status": "uploaded"}, function() {
                that._completeHandler();
            });
        },

        _notifyAsyncOperation: function() {
            this.__asyncOperationsControl.push(1);
        },

        _asyncOperationCompleted: function() {
            this.__asyncOperationsControl.pop();
            this._completeHandler();
        },

        _completeHandler: function() {
            this._clearAssetData();
            const that = this;
            this.dispatchEvent(that.eventNames.COMPLETE, [that.embedCode]);
        },

        _errorHandler: function(error) {
            this._clearAssetData();
            this.dispatchEvent(this.eventNames.ERROR, [error]);
        },

        _clearAssetData: function() {
            //Wait until all asynchronous operations have been completed
            if (this.__asyncOperationsControl.length) {
                return;
            }

            this.assetToUpload = null;
            this.embedCode = null;
        },

        /**
         * Create an asset in preparation for uploading to window.Ooyala.
         *
         * @param name
         *   The name of the asset to create.
         * @param description
         *   The short text description of the asset.
         * @param file_name
         *   The file name of the asset being uploaded.
         * @param file_size
         *   The number of bytes of the asset to upload.
         * @param chunk_size
         *   The size of each chunk to upload in bytes.
         * @param asset_type
         *   Optional parameter of the asset type to create. Defaults to "video".
         *
         * @return
         *   An object containing the following properties:
         *   - embed_code: The embed code for the asset that was created.
         *   - uploading_urls: An array of URLs to upload each chunk to.
         */
        createAsset: function(name, description, file_name, file_size, chunk_size, asset_type) {
            const that = this;
            const body = {
                name: name,
                description: description,
                file_name: file_name,
                file_size: file_size,
                chunk_size: chunk_size,
                asset_type: (typeof asset_type != 'undefined') ? asset_type : 'video'
            };

            this._makeAPICall("POST", "assets", body, function(data) {
                that.embedCode = data.embed_code;
                that.uploader.setUploadingURLs(data.uploading_urls);
                that.dispatchEvent(that.eventNames.ASSET_CREATION_COMPLETE);
            });
        },

        /**
         * Private method to call our remote API endpoint. All other code should
         * call methods such as createAsset instead of _makeAPICall() directly.
         *
         * @param method
         *   The HTTP method as a string, such as "POST" or "PATCH".
         * @param path
         *   The relative path to the resource within the endpoint.
         * @param body
         *   The object to use as the request body. The object will be converted to
         *   JSON before being POSTed.
         * @param success
         *   A callback function to call in the case of a sucessfull API call.
         * @param failure
         *   Optional parameter of a callback function to call in the case of a
         *   failed API call. Defaults to _errorHandler().
         * @param context
         *   Optional parameter of a reference to the object to use as the context
         *   for the request. Defaults to "this".
         */
        _makeAPICall: function(method, path, body, success, failure, context) {
            context = context || this;

            //If no failure function has been provided, default to a function
            //that fires an error event in case of failure
            failure = failure || function(error) {this._errorHandler(error)};

            $.ajax({
                url: this.endpoint + '/' + path,
                type: "POST",
                contentType: 'application/json',
                dataType: "json",
                data: JSON.stringify(body)
            }).done(function(data) {
                success.call(context, data);
            }).fail(function(error) {
                failure.call(context, error);
            });
        }
    });
}).call(this, jQuery);
