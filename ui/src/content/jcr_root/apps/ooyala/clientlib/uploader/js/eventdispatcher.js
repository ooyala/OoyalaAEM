/**
* Base class to all Objects that will support the "on('event', callback)" pattern for event listening
* */
(function($) {
    window.Ooyala.Client.EventDispatcher = function() {
        this.eventHandlers = {};
    };

    $.extend(window.Ooyala.Client.EventDispatcher.prototype, {
        on: function(eventName, eventHandler, context) {
            if (!this.eventHandlers[eventName]) {
                this.eventHandlers[eventName] = [];
            }

            context = context || this;
            this.eventHandlers[eventName].push({handler: eventHandler, context: context});
        },

        detach: function(eventName, eventHandler) {
            const handlers = this.eventHandlers[eventName];

            if (!handlers) {
                return;
            }

            let indexToRemove = null;
            for (let i = 0; i < handlers.length; i++) {
                if (handlers[i].handler === eventHandler) {
                    indexToRemove = i;
                    break;
                }
            }

            //Get rid of the desired handler
            if (indexToRemove != null) {
                this.eventHandlers[eventName].splice(indexToRemove,1);
            }
        },

        dispatchEvent: function(eventName, args) {
            const handlers = this.eventHandlers[eventName];

            if (!handlers) {
                return;
            }

            for (let i = 0; i < handlers.length; i++) {
                const currentHandler = handlers[i];
                //Could happen when an event is trying to be dispatched and the handlers has been removed.
                if (!currentHandler) {
                    continue;
                }
                currentHandler.handler.apply(currentHandler.context, args);
            }
        }
    });
}).call(this, jQuery);
