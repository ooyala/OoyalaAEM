/**
 * Copyright (c) 2017, Ooyala, Inc.
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * •    Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * •    Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
(function (document, $) {
    "use strict";

    $(document).ready(function () {
        setTimeout(function() {
            const MIN_SEARCH_TEXT_LENGTH = 2;

            const $fulltext = $('.coral-Search--cqSearchPanel input[name$=fulltext]'),
                $suggestPredicateSearchWrapper = $('.suggestPredicateSearchWrapper'),
                $autocompleteFieldItemsWrapper = $suggestPredicateSearchWrapper.find(".autocompleteFieldItemsWrapper"),
                $filterFieldWrapper = $suggestPredicateSearchWrapper.find('.filterFieldWrapper'),
                $autocompleteFieldWrapper = $suggestPredicateSearchWrapper.find('.autocompleteFieldWrapper'),
                $autocompleteFieldInput = $autocompleteFieldWrapper.find('.coral-Textfield'),
                $assetTypeSelector = $('coral-select[name=assetfilter_type_selector]'),
                $searchByAccordion = $('coral-accordion-item:has(input[value="searchBy"])'),
                ns = ".fulltext",
                $clearAllButton = $(".taglist-clearall button");
            let searchBy, request;

            const coralSuggestions = $autocompleteFieldItemsWrapper.get(0);

            coralSuggestions.delay = 0;
            window.Coral.commons.ready(coralSuggestions, function() {
                coralSuggestions.on('coral-autocomplete:showsuggestions', function(event) {
                    if (request) {
                        request.abort();
                    }
                    event.preventDefault();
                          
                    if ($autocompleteFieldInput.val().length < MIN_SEARCH_TEXT_LENGTH) {
                        return;
                    }
                    request = $.ajax({
                        type: 'GET',
                        dataType: 'json',
                        url: '/bin/wcm/ooyala/suggestions',
                        data: {
                            type: getSuggestionTypeByLabel(searchBy),
                            query: $autocompleteFieldInput.val(),
                            '_charset_': 'utf-8'
                        },
                        success: function(data) {
                            const newSuggestions = [];
                            $.each(data.suggestions, function(index, item) {
                                if (!item.value) {
                                    return
                                }
                                newSuggestions.push({
                                    value: item.value,
                                    content: item.value
                                })
                            });
                            coralSuggestions.addSuggestions(newSuggestions, true);
                        }
                    });
                });
                coralSuggestions.on('coral-autocomplete:hidesuggestions', function() {
                    if (request) {
                        request.abort();
                    }
                });
            });

            $clearAllButton.on("click", function() {
                $autocompleteFieldInput.val("");
            })

            $(document).off('resetSearchFilters' + ns).on('resetSearchFilters' + ns, function () {
                $fulltext.val("");
            });

            /**
             * Switching the filter panel between default and suggest ones depends on search by criteria.
             */
            $searchByAccordion.on('change', function(event) {
                searchBy = $(event.target).val();

                if ('searchByTitle' === searchBy || 'searchByDescription' === searchBy || 'searchByPlaylist' === searchBy) {
                    $filterFieldWrapper.show();
                    $autocompleteFieldWrapper.addClass('hidden');

                    $fulltext.val($autocompleteFieldInput.val());
                } else {
                    $filterFieldWrapper.hide();
                    $autocompleteFieldWrapper.removeClass('hidden');

                    $autocompleteFieldInput.val($fulltext.val());
                }
            });

            $fulltext.on('change', function() {
                $autocompleteFieldInput.val($(this).val() || '');
            });

            /**
             * Restore default filter panel.
             */
            $assetTypeSelector.on('change', function() {
                $filterFieldWrapper.show();
                $autocompleteFieldWrapper.addClass('hidden');
                $fulltext.val('');
                $autocompleteFieldInput.val('');
            });

            /**
             * Submit form when pressing enter in fulltext input field
             */
            function enterHandler(e, $tagList, $target) {
                const $elem = $(this),
                    ui = $(window).adaptTo('foundation-ui'),
                    minLength = $elem.data('minlength');
                let isValid = true;

                if (minLength && $elem.val().length < minLength) {
                    if($target.attr("name") === $elem.attr("name")) {
                        e.preventDefault();
                        e.stopPropagation();

                        ui.alert(window.Granite.I18n.get("Error"), window.Granite.I18n.get("Please enter at least {0} characters.", minLength), "error");
                        isValid = false;
                    } else {
                        $elem.val("");
                    }
                }
                const maxLength = $elem.data('maxlength');
                if (maxLength && $elem.val().length > maxLength && $target === $elem) {
                    if($target.attr("name") === $elem.attr("name")) {
                        e.preventDefault();
                        e.stopPropagation();

                        ui.alert(window.Granite.I18n.get("Error"), window.Granite.I18n.get("Please enter at most {0} characters.", maxLength), "error");
                        isValid = false;
                    } else {
                        $elem.val("");
                    }
                }
                if (isValid && $tagList !== null) {
                    const tag = new window.Coral.Tag();
                    tag.label.innerHTML = $elem.val();
                    tag.name = $target.attr("name");
                    tag.value = $elem.val();
                    $tagList.items.add(tag);
                    $tagList.trigger("change");
                }
            }
            $fulltext.keyup(function (e) {
                const keycode = window.Granite.Util.getKeyCode(e),
                    $target = $(e.target);

                if (keycode === 13 && (!$target.val() || $target.val().length >= MIN_SEARCH_TEXT_LENGTH)) {
                    const $tagList = document.querySelector(".granite-omnisearch-typeahead-tags");
                    $fulltext.each(function(event) {
                        enterHandler(event, $tagList, $target)
                    });
                }
            });

            MutationObserver = window.MutationObserver || window.WebKitMutationObserver;
            const trackChange = function(element) {
                const observer = new MutationObserver(function(mutations) {
                    if (mutations[0].attributeName == "value") {
                        $fulltext.val($(element).val());
                        $fulltext.trigger('change');
                    }
                });

                observer.observe(element, {
                    attributes: true
                });
            }

            trackChange($autocompleteFieldWrapper.find('input[type="hidden"]')[0]);

            $autocompleteFieldInput.keyup(function(e) {
                const keycode = window.Granite.Util.getKeyCode(e),
                    $target = $(e.target);

                if (keycode === 13 && (!$target.val() || $target.val().length >= MIN_SEARCH_TEXT_LENGTH)) {
                    const $tagList = document.querySelector(".granite-omnisearch-typeahead-tags");
                    $autocompleteFieldInput.each(function(event) {
                        enterHandler(event, $tagList, $target) ;
                    });

                    // Trigger loadAssets event to update Ooyala assets list via controller.
                    $('div.assetfinder-content-container').trigger('loadAssets');
                }
            });

            /**
             * Reset search field
             */
            $fulltext.closest(".coral-Form-fieldwrapper").find("button").click(function () {
                $(this).closest(".coral-Form-fieldwrapper").find("input").val("");
                $(this).closest("form").trigger('submit', [true]);
            });

            function getSuggestionTypeByLabel(searchBy) {
                if ('searchByLabel' === searchBy) {
                    return 'label';
                }

                if ('searchByMeta' === searchBy) {
                    return 'meta';
                }
            }
        }, 0);
    });
})(document, window.Granite.$);