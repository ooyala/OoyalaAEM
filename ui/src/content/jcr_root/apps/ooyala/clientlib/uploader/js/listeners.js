$(document).ready(function() {
    'use strict';
    const SELECT_ASSET_TITLE_DEFAULT = "No file chosen";

    window.uploader = new window.Ooyala.Client.AssetCreator('/bin/ooyala/upload', 'selectAsset');

    const $uploaderItemsContainer = $('.uploaderItemsContainer'),
        $selectAsset = $uploaderItemsContainer.find('#selectAsset'),
        $uploadButton = $('#uploadButton'),
        $closeButton = $('#closeButton'),
        $assetName = $uploaderItemsContainer.find('#assetName'),
        $assetDescription = $uploaderItemsContainer.find('#assetDescription'),

        progress = new window.Coral.Progress(),
        modal = new window.Coral.Dialog().set({
            id: 'modal',
            footer: {
                innerHTML: "<button is='coral-button' variant='primary' coral-close>Ok</button>"
            }
        }),
        assetTooltip = new window.Coral.Tooltip().set({
            content: {
                innerHTML: SELECT_ASSET_TITLE_DEFAULT
            },
            target: $selectAsset.get(0),
            placement: 'bottom',
            interaction: 'on'
        }),            
        alert = new window.Coral.Alert().set({
            variant: "error",
            header: {
                innerHTML: "error:"
            },
            id: "alert"
        });

    $.ajax({
        url: "/bin/wcm/ooyala/validation/credentials",
        success: function(data) {
            const results = JSON.parse(data).results;
            if (!results.isValid) {
                $uploadButton.attr("disabled", true);
                $selectAsset.attr("disabled", true);
                $assetName.attr("disabled", true);
                $assetDescription.attr("disabled", true);
                alert.content.innerHTML = results.message;
                $(alert).insertBefore($(".uploaderItemsContainer"));
            }
        }
    });
        
    $(assetTooltip).insertBefore($selectAsset);

    const $form = $selectAsset.closest("form");
    const $sel = $form.find("input[type='file']");
    const accept = [".wmv", ".avi", ".mov", ".moov", ".mpg", ".mpeg", ".m2t", ".m2v", ".vob", ".flv", ".mp4", ".mpg4", ".mkv", ".asf", ".m4v", ".m2p", ".3gp", ".3g2", ".f4v", ".mp3", ".m4a", ".wma", ".aac", ".mxf", ".wav", ".gxf", ".fcp", ".dv", ".aif", ".pek", ".cfa"];
    $sel.attr("accept", accept.join(","));
    let uploadPhase;

    $selectAsset.on("click", function() {
        if (uploadPhase) {
            return;
        }
        const listener = function(event) {
            event.preventDefault();
            assetTooltip.off("coral-overlay:beforeopen", listener)
        }
        if (assetTooltip.open) {
            assetTooltip.hide();
        } else {
            assetTooltip.on("coral-overlay:beforeopen", listener);
        }
        $sel.trigger("click");
    });

    $sel.on("change", function(event) {
        const value = event.target.value.split("\\").reverse()[0];
        $selectAsset.blur();
        assetTooltip.content.innerHTML = value || SELECT_ASSET_TITLE_DEFAULT;
        assetTooltip.variant = "info";
        $selectAsset.get(0).variant = "secondary";
        $selectAsset.find("coral-icon").get(0).icon = "fileAdd";
        $assetName.val(value.split(".").slice(0,-1).join("."));
        $assetName.trigger("change");
    });

    $form.on("reset", function() {
        assetTooltip.content.innerHTML = SELECT_ASSET_TITLE_DEFAULT;
    })

    progress.size = "S";
    progress.indeterminate = true;
    progress.id = "uploadProgress";

    modal.backdrop = "static";
    modal.closable = "on";
    $("body").append($(modal));

    $uploadButton.wrap($("<div id='uploadButtonWrapper'/>"));
    const $uploadButtonWrapper = $uploadButton.closest("#uploadButtonWrapper");

    $(modal).find("[coral-close]").on("click", function() {
        $uploadButtonWrapper.removeClass("complete").removeClass("error");
        progress.indeterminate = true;
        if (modal.variant == "success") {
            $uploadButton.closest("form").get(0).reset();
        }
    });

    
    $(progress).insertBefore($uploadButton);
    window.uploader.on('progress', function() {
        if (progress.get("indeterminate") && !progress.get("value")) {
            progress.indeterminate = false;
        }
        progress.value = window.uploader.progress() * 100;
    });

    window.uploader.on('complete', function() {
        uploadPhase = false;
        $uploadButtonWrapper.removeClass("upload").addClass("complete");
        modal.variant = "success";
        modal.header.innerHTML = "Done";
        modal.content.innerHTML = "File uploaded successfully";
        modal.show();
    });

    window.uploader.on('error', function() {
        uploadPhase = false;
        $uploadButtonWrapper.removeClass("upload").addClass("error");
        modal.variant = "error";
        modal.header.innerHTML = "Error";
        modal.content.innerHTML = "Upload failed. Please try again";
        modal.show();
    });

    window.uploader.on('assetCreationComplete', window.uploader.upload);

    function validateFormat($input) {
        const file = $input.get(0).files[0];
        if (!file) {
            return true;
        }
        const ext = file.name.split(".").reverse()[0].toLowerCase(),
            type = file.type,
            group = type.replace(/(.*?)\/.*/, "$1/*");
        return accept.indexOf("." + ext) !== -1 || accept.indexOf(type) !== -1 || accept.indexOf(group) !== -1;
    }


    function validate() {
        let result;
        $.ajax({
            url: "/bin/wcm/ooyala/validation/video",
            data: {
                name: $assetName.val()
            },
            dataType: "json",
            async: false,
            success: function(data) {
                result = data.results.isValid ? false : data.results;
            }
        });
        return result;
    }
    
    const validation = window.Ooyala.Client.Validation;

    $uploadButton.on('click', function () {
        if (uploadPhase) {
            return;
        }
        let invalid = false;
        if (!$assetName.val()) {
            validation.setRequire($assetName);
            invalid = true;
        } else {
            const data = validate();
            if (data) {
                validation.setRequire($assetName, data.message);
                invalid = true;
            }
        }
        if (!$sel.val()) {
            $selectAsset.find("coral-icon").get(0).icon = "alert";
            $selectAsset.get(0).variant = "warning";
            assetTooltip.variant = "error";
            assetTooltip.content.innerHTML = "Please choose a file to upload";
            invalid = true;
        }
        if (!validateFormat($sel)) {
            $selectAsset.find("coral-icon").get(0).icon = "alert";
            $selectAsset.get(0).variant = "warning";
            assetTooltip.variant = "error";
            assetTooltip.content.innerHTML = "Unsupported file format.";
            invalid = true;
        }
        if (invalid) {
            return;
        }
        
        uploadPhase = true;
        $uploadButtonWrapper.addClass("upload");
        window.uploader.prepareUpload($assetName.val(), $assetDescription.val());
    });
    $.validator.register({
        selector: '#assetName',
        clear: validation.removeRequire
    });

    $closeButton.on("click", function() {
        window.location.href = "/aem/start";
    })
});