<%--

Copyright (c) 2017, Ooyala, Inc.
All rights reserved.
Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
•    Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
•    Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

--%><%
%><%@include file="/libs/granite/ui/global.jsp" %><%
%><%@page session="false"
          import="org.apache.commons.lang.StringUtils,
                  com.adobe.granite.ui.components.AttrBuilder,
                  com.adobe.granite.ui.components.Config,
                  com.adobe.granite.ui.components.Field,
                  com.adobe.granite.ui.components.Tag" %><%

    Config cfg = cmp.getConfig();
    ValueMap vm = (ValueMap) request.getAttribute(Field.class.getName());

    Tag tag = cmp.consumeTag();
    AttrBuilder attrs = tag.getAttrs();

    attrs.add("id", cfg.get("id", String.class));
    attrs.addClass(cfg.get("class", String.class));
    attrs.addRel(cfg.get("rel", String.class));
    attrs.add("title", i18n.getVar(cfg.get("title", String.class)));
    
    attrs.add("type", "text");
    attrs.add("name", cfg.get("name", String.class));
    attrs.add("placeholder", i18n.getVar(cfg.get("emptyText", String.class)));
    attrs.addDisabled(cfg.get("disabled", false));
    attrs.add("value", vm.get("value", String.class));

    if (cfg.get("required", false)) {
        attrs.add("aria-required", true);
    }

    String validation = StringUtils.join(cfg.get("validation", new String[0]), " ");
    attrs.add("data-validation", validation);

    attrs.addClass("coral-Textfield");
    attrs.addClass("coral-DecoratedTextfield-input");

    attrs.addOthers(cfg.getProperties(), "id", "class", "rel", "title", "type", "name", "value", "emptyText", "disabled", "required", "validation", "fieldLabel", "fieldDescription", "renderReadOnly", "ignoreData", "icon");

    AttrBuilder wrapperAttrs = new AttrBuilder(request, xssAPI);
    wrapperAttrs.addClass("coral-DecoratedTextfield");
    wrapperAttrs.addClass(cfg.get("wrapperConfig/class", String.class));

    String icon = cfg.get("icon", String.class);

    Resource button = resource.getChild("button");

    AttrBuilder suggestAttrs = new AttrBuilder(request, xssAPI);
    suggestAttrs.add("placeholder", i18n.getVar(cfg.get("emptyText", String.class)));

%><ui:includeClientLib categories="ooyala.suggestpredicate" />
<div class="suggestPredicateSearchWrapper">
    <div class="coral-Form-fieldwrapper coral-Search--cqSearchPanel filterFieldWrapper">
        <span <%= wrapperAttrs.build() %> ><%
        if (icon != null) {
            %><i class="coral-DecoratedTextfield-icon coral-Icon coral-Icon--sizeXS <%= cmp.getIconClass(icon) %>"></i><%
        }
        %><input <%= attrs.build() %> /><%
        if (button != null) {
            AttrBuilder buttonAttrs = new AttrBuilder(request, cmp.getXss());
            buttonAttrs.addClass("coral-DecoratedTextfield-button");

            cmp.include(button, new Tag(buttonAttrs));
        }
        %></span>
    </div>
    <div class="coral-Form-fieldwrapper autocompleteFieldWrapper hidden">
        <coral-autocomplete data-editor-searchfilter-search="" class="autocompleteFieldItemsWrapper" <%= suggestAttrs.build() %> match="startswith" labelledby="label-vertical-0">
        </coral-autocomplete>
    </div>
</div>