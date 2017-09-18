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

--%>

<%@page session="false" contentType="text/html; charset=utf-8"%><%
%><%@page import="com.adobe.granite.ui.components.ComponentHelper,
                  com.adobe.granite.ui.components.Config,
                  com.day.cq.i18n.I18n,
                  com.adobe.granite.ui.components.AttrBuilder"%><%

%><%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.0"%><%
%><%@taglib prefix="cq" uri="http://www.day.com/taglibs/cq/1.0"%><%
%><%@taglib prefix="ui" uri="http://www.adobe.com/taglibs/granite/ui/1.0"%><%
%><cq:defineObjects /><%
    ComponentHelper cmp = new ComponentHelper(pageContext);
    Config cfg = new Config(resource);
    AttrBuilder inputAttrs = new AttrBuilder(request, xssAPI);
    AttrBuilder suggestAttrs = new AttrBuilder(request, xssAPI);

    cmp.populateCommonAttrs(inputAttrs);
    cmp.populateCommonAttrs(suggestAttrs);

    inputAttrs.add("type", "text");

    inputAttrs.addClass("coral-Form-field coral-Textfield coral-DecoratedTextfield-input");

    I18n i18n = new I18n(slingRequest);
    String placeholder = i18n.get(cfg.get("emptyText", "Enter Keyword(s)"));
    inputAttrs.add("placeholder", placeholder);
    suggestAttrs.add("placeholder", placeholder);

    String defaultValue = "";
    if (slingRequest.getRequestParameter(cfg.get ("value")) != null) {
        defaultValue = slingRequest.getRequestParameter(cfg.get ("value")).getString("UTF-8");
    }
    inputAttrs.add("value", defaultValue);
    suggestAttrs.add("value", defaultValue);

    inputAttrs.addOthers(cfg.getProperties(), new String[] {"name", "value", "hideCloseBtn", "emptyText", "listOrder"});
    suggestAttrs.addOthers(cfg.getProperties(), new String[] {"name", "value", "hideCloseBtn", "emptyText", "listOrder"});

    int predicateIndex = cfg.get("listOrder", 2);
    String predicateName = predicateIndex + "_fulltext";
    String suggestPredicateName = predicateIndex + "_suggest";
    inputAttrs.add("name", predicateName);
    suggestAttrs.add("name", suggestPredicateName);

    String propertyName = cfg.get("name", ".");

    AttrBuilder closeBtnAttrs = new AttrBuilder(request, xssAPI);
    closeBtnAttrs.addClass("coral-DecoratedTextfield-button");
    closeBtnAttrs.add("is", "coral-button");
    closeBtnAttrs.add("variant", "minimal");
    closeBtnAttrs.add("icon", "close");
    closeBtnAttrs.add("iconsize", "XS");
    closeBtnAttrs.add("autocomplete", "off");
    closeBtnAttrs.add("title", i18n.get("clear") );

    if(cfg.get("hideCloseBtn", false)){
        closeBtnAttrs.add("hidden","");
    }
%><ui:includeClientLib categories="ooyala.suggestpredicate" />
<div class="suggestPredicateSearchWrapper">
    <div class="coral-Form-fieldwrapper coral-Search--cqSearchPanel filterFieldWrapper">
        <span class="coral-Form-field coral-DecoratedTextfield">
            <coral-icon class="coral-DecoratedTextfield-icon" icon="search" size="XS"></coral-icon>
            <input <%= inputAttrs.build() %>>
            <button <%= closeBtnAttrs.build() %>></button>
        </span>
        <input type="hidden" name="<%= xssAPI.encodeForHTMLAttr(predicateName) %>.relPath" value="<%= xssAPI.encodeForHTMLAttr(propertyName) %>"/>
    </div>
    <div class="coral-Form-fieldwrapper autocompleteFieldWrapper hidden">
        <coral-autocomplete data-editor-searchfilter-search="" class="coral-Form-field autocompleteFieldItemsWrapper" <%= suggestAttrs.build() %> match="startswith" labelledby="label-vertical-0"></coral-autocomplete>
    </div>
</div>