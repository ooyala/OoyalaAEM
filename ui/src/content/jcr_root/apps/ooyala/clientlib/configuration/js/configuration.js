$(document).ready(function() {
    const $apiKey = $("[name='./apiKey']"),
        $apiSecret = $("[name='./apiSecret']"),
        $saveButton = $("#submitButton"),
        $closeButton = $('#closeButton'),
        $form = $saveButton.closest("form");

    const modal = new window.Coral.Dialog().set({
            id: 'modal',
            variant: "success",
            header: {
                innerHTML: "Done"
            },
            content: {
                innerHTML: "Configuration saved successfully"
            },
            footer: {
                innerHTML: "<button is='coral-button' variant='primary' coral-close>Ok</button>"
            }
        });

    $("body").append($(modal));
    $saveButton.attr("type","button");

    const validation = window.Ooyala.Client.Validation;

    $saveButton.on("click", function() {
        let isValid = true;
        if (!$apiKey.val()) {
            validation.setRequire($apiKey);
            isValid = false;
        }
        if (!$apiSecret.val()) {
            validation.setRequire($apiSecret);
            isValid = false;
        }
        if (isValid) {
            $.ajax({
                data: $form.serialize(),
                type: "POST",
                url: $form.attr('action'),
                success: function() {
                    modal.show()
                }
            });
        }
    });

    $.validator.register({
        selector: "[name='./apiKey']",
        clear: validation.removeRequire
    },{
        selector: "[name='./apiSecret']",
        clear: validation.removeRequire
    });

    $closeButton.on("click", function() {
        window.location.href = "/aem/start";
    })
});