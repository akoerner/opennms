/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.tags;

import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.model.OnmsFilterFavorite;
import org.opennms.web.filter.Filter;
import org.opennms.web.filter.NormalizedAcknowledgeType;
import org.opennms.web.filter.NormalizedQueryParameters;
import org.opennms.web.filter.QueryParameters;
import org.opennms.web.tags.filters.FilterCallback;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Renders the filter area for a given set of filters. It also considers if a favorite is there.
 */
public class FiltersTag extends TagSupport {

    private static final String TEMPLATE = "{LEADING}{FILTERS}";

    private static final String FILTER_TEMPLATE = "<span class=\"filter\">{FILTER_DESCRIPTION} {REMOVE_FILTER_LINK}</span> ";

    private static final String REMOVE_FILTER_TEMPLATE = "<a href=\"{REMOVE_LINK}\" title=\"{REMOVE_LINK_TITLE}\"><i class=\"fa fa-minus-square-o\"></i></a>";

    private String showRemoveLink;

    private String showAcknowledgeFilter;

    private String acknowledgeFilterPrefix;

    private String acknowledgeFilterSuffix;

    private OnmsFilterFavorite favorite;

    private QueryParameters parameters;

    private FilterCallback filterCallback;

    private String context;

    public void setContext(String context) {
        if (context != null && context.startsWith("/")) context = context.substring(1, context.length());
        this.context = context;
    }

    public void setAcknowledgeFilterPrefix(String acknowledgeFilterPrefix) {
        this.acknowledgeFilterPrefix = acknowledgeFilterPrefix;
    }

    public void setAcknowledgeFilterSuffix(String acknowledgeFilterSuffix) {
        this.acknowledgeFilterSuffix = acknowledgeFilterSuffix;
    }

    public void setCallback(FilterCallback filterCallback) {
        this.filterCallback = filterCallback;
    }

    public void setShowAcknowledgeFilter(String showAcknowledgeFilter) {
        this.showAcknowledgeFilter = showAcknowledgeFilter;
    }

    public void setShowRemoveLink(String showRemoveLink) {
        this.showRemoveLink = showRemoveLink;
    }

    public void setParameters(QueryParameters parameters) {
        this.parameters = parameters;
    }

    public void setFavorite(OnmsFilterFavorite favorite) {
        this.favorite = favorite;
    }

    private List<Filter> getFilters() {
        if (parameters == null || parameters.getFilters() == null) return new ArrayList<Filter>();
        return parameters.getFilters();

    }

    @Override
    public int doStartTag() throws JspException {
        final String leadingString = getLeading();
        final StringBuffer filterBuffer = new StringBuffer();

        for (Filter eachFilter : getFilters()) {
            NormalizedQueryParameters params = new NormalizedQueryParameters(parameters);
            params.getFilters().remove(eachFilter);
            filterBuffer.append(FILTER_TEMPLATE
                    .replaceAll("\\{FILTER_DESCRIPTION\\}", WebSecurityUtils.sanitizeString(eachFilter.getTextDescription()))
                    .replaceAll("\\{REMOVE_FILTER_LINK\\}", getRemoveLink("Remove filter", params)));
        }

        out(TEMPLATE
                .replaceAll("\\{LEADING\\}", leadingString)
                .replaceAll("\\{FILTERS\\}", filterBuffer.toString()));
        return EVAL_BODY_INCLUDE;
    }

    private void out(String content) throws JspException {
        try {
            pageContext.getOut().write(content);
        } catch (IOException e) {
            throw new JspException(e);
        }
    }

    private boolean isShowRemoveLink() {
        return Boolean.valueOf(showRemoveLink);
    }

    private boolean isShowAcknowledgeFilter() {
        return Boolean.valueOf(showAcknowledgeFilter);
    }

    private boolean isAcknowledgeType() {
        if (parameters != null) {
            return parameters.getAckType().equals(NormalizedAcknowledgeType.ACKNOWLEDGED);
        }
        return false;
    }

    private boolean isUnacknowledgeType() {
        if (parameters != null) {
            return parameters.getAckType().equals(NormalizedAcknowledgeType.UNACKNOWLEDGED);
        }
        return false;
    }

    private String getRemoveLink(final String removeLinkTitle, final QueryParameters params) {
        if (isShowRemoveLink()) {
            return REMOVE_FILTER_TEMPLATE
                    .replaceAll("\\{REMOVE_LINK\\}", filterCallback.createLink(getUrlBase(), params, favorite))
                    .replaceAll("\\{REMOVE_LINK_TITLE\\}", removeLinkTitle);
        }
        return "";
    }

    private String getAcknowledgeFilterPrefix() {
        return acknowledgeFilterPrefix != null ? acknowledgeFilterPrefix : "";
    }

    private String getAcknowledgeFilterSuffix() {
        return acknowledgeFilterSuffix != null ? acknowledgeFilterSuffix : "";
    }

    private String getLeading() {
        StringBuffer leadingString = new StringBuffer();
        if (favorite == null) {
            leadingString.append("Search constraints: ");
        }
        if (isShowAcknowledgeFilter()) {
            NormalizedQueryParameters params = new NormalizedQueryParameters(parameters);
            if (isAcknowledgeType()) {
                params.setAckType(NormalizedAcknowledgeType.UNACKNOWLEDGED);
                leadingString.append(FILTER_TEMPLATE
                        .replaceAll("\\{FILTER_DESCRIPTION\\}", getAcknowledgeFilterPrefix() + " acknowledged")
                        .replaceAll("\\{REMOVE_FILTER_LINK\\}", getRemoveLink("Show outstanding " + getAcknowledgeFilterSuffix(), params)));
            } else if (isUnacknowledgeType()) {
                params.setAckType(NormalizedAcknowledgeType.ACKNOWLEDGED);
                leadingString.append(FILTER_TEMPLATE
                        .replaceAll("\\{FILTER_DESCRIPTION\\}", getAcknowledgeFilterPrefix() + " outstanding")
                        .replaceAll("\\{REMOVE_FILTER_LINK\\}", getRemoveLink("Show acknowledged " + getAcknowledgeFilterSuffix(), params)));
            }
        }
        return leadingString.toString();
    }

    private String getUrlBase() {
        String urlBase = ((HttpServletRequest)pageContext.getRequest()).getContextPath();
        if (!urlBase.endsWith("/")) urlBase += "/";
        return urlBase + context;
    }
}
