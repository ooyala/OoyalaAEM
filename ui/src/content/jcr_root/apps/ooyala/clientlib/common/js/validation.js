(function($) {
    window.Ooyala.Client.Validation = {
        setRequire: function($elem, message) {
            if ($elem.get(0).invalid) {
                return;
            }
            const icon = new window.Coral.Icon().set({
                icon: "alert",
            });
            $elem.parent().append($(icon));
            $(icon).addClass("coral-Form-fielderror");

            const tooltip = new window.Coral.Tooltip().set({
                content: {
                    innerHTML: message || "This field is required"
                },
                variant: 'error',
                target: icon,
                placement: 'left',
                interaction: 'on'
            });
            $elem.parent().append($(tooltip));

            $elem.get(0).invalid = true;
        },
        removeRequire: function($elem) {
            $elem.get(0).invalid = false;
            const $parent = $($elem).parent();
            $parent.find("coral-icon").remove();
            $parent.find("coral-tooltip").remove();
        }
    }
}).call(this, jQuery);