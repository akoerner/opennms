/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013 The OpenNMS Group, Inc.
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

package org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Logger;

import org.opennms.features.vaadin.nodemaps.internal.gwt.client.JSNodeMarker;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.NodeMarker;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.OpenNMSEventManager;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.ApplicationInitializedEvent;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.ApplicationInitializedEventHandler;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.ComponentInitializedEvent;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.FilterUpdatedEvent;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.FilterUpdatedEventHandler;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.FilteredMarkersUpdatedEvent;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.MarkersModelUpdatedEvent;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.MarkersModelUpdatedEventHandler;

public class MarkerContainer implements MarkerProvider, FilterUpdatedEventHandler, MarkersModelUpdatedEventHandler, ApplicationInitializedEventHandler {
    List<JSNodeMarker> m_markers         = new ArrayList<JSNodeMarker>();
    List<JSNodeMarker> m_filteredMarkers = new ArrayList<JSNodeMarker>();

    private Logger logger = Logger.getLogger(getClass().getName());
    private OpenNMSEventManager m_eventManager;
    private MarkerFilter m_filter;

    public MarkerContainer(final MarkerFilter filter, final OpenNMSEventManager eventManager) {
        m_filter = filter;
        m_eventManager = eventManager;
    }

    public void onLoad() {
        m_eventManager.addHandler(FilterUpdatedEvent.TYPE, this);
        m_eventManager.addHandler(MarkersModelUpdatedEvent.TYPE, this);
        m_eventManager.addHandler(ApplicationInitializedEvent.TYPE, this);

        //refresh();
        m_eventManager.fireEvent(new ComponentInitializedEvent(MarkerContainer.class.getName()));
    }

    public void onUnload() {
        m_eventManager.removeHandler(FilterUpdatedEvent.TYPE, this);
        m_eventManager.removeHandler(MarkersModelUpdatedEvent.TYPE, this);
        m_eventManager.removeHandler(ApplicationInitializedEvent.TYPE, this);
    }

    public int size() {
        return getMarkers().size();
    }

    public ListIterator<JSNodeMarker> listIterator() {
        return getMarkers().listIterator();
    }

    public List<JSNodeMarker> getDisabledMarkers() {
        final ArrayList<JSNodeMarker> markers = new ArrayList<JSNodeMarker>();
        final List<JSNodeMarker> existingMarkers = getMarkers();
        if (existingMarkers != null) {
            for (final NodeMarker marker : existingMarkers) {
                if (marker instanceof JSNodeMarker) {
                    final JSNodeMarker m = (JSNodeMarker)marker;
                    if (!m_filteredMarkers.contains(m)) {
                        markers.add(m);
                    }
                }
            }
        }
        return Collections.unmodifiableList(markers);
    }

    public List<JSNodeMarker> getAllMarkers() {
        return Collections.unmodifiableList(m_markers);
    }

    @Override
    public List<JSNodeMarker> getMarkers() {
        return Collections.unmodifiableList(m_filteredMarkers);
    }

    public void setMarkers(final List<JSNodeMarker> markers) {
        if (markers == null) {
            logger.info("MarkerContainer.setMarkers(): clearing master marker list in the marker container.");
            if (m_markers != null) m_markers.clear();
        } else {
            logger.info("MarkerContainer.setMarkers(): saving " + markers.size() + " markers to the master marker list in the marker container.");
            m_markers = markers;
        }
        m_eventManager.fireEvent(new MarkersModelUpdatedEvent());
        refresh();
    }

    public void refresh() {
        logger.info("MarkerContainer.refresh()");

        final List<JSNodeMarker> markers = new ArrayList<JSNodeMarker>();
        final List<JSNodeMarker> existingMarkers = getAllMarkers();
        if (existingMarkers != null) {
            for (final JSNodeMarker marker : existingMarkers) {
                if (m_filter.matches(marker)) {
                    markers.add(marker);
                }
            }
        }
        m_filteredMarkers = markers;
        logger.info("MarkerContainer.refresh(): out of " + getMarkers().size() + " markers, " + markers.size() + " matched the search filter.");
        m_eventManager.fireEvent(new FilteredMarkersUpdatedEvent());
    }

    @Override
    public void onFilterUpdatedEvent(final FilterUpdatedEvent event) {
        logger.info("MarkerContainer.onFilterUpdatedEvent()");
        refresh();
    }

    @Override
    public void onMarkersModelUpdated(final MarkersModelUpdatedEvent event) {
        logger.info("MarkerContainer.onMarkersModelUpdated()");
        refresh();
    }

    @Override
    public void onApplicationInitialized(final ApplicationInitializedEvent event) {
        logger.info("MarkerContainer.onApplicationInitialized()");
        refresh();
    }
}
