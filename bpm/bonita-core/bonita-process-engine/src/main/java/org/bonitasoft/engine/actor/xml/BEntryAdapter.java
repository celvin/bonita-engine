/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package org.bonitasoft.engine.actor.xml;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.bonitasoft.engine.bpm.bar.xml.XMLProcessDefinition;

/**
 * Created by mazourd on 15/07/15.
 */
public class BEntryAdapter extends XmlAdapter<BEntryAdapter.Entry, XMLProcessDefinition.BEntry<String, String>> {

    @Override
    public XMLProcessDefinition.BEntry<String, String> unmarshal(Entry v) throws Exception {
        XMLProcessDefinition.BEntry<String, String> mapEntry = new XMLProcessDefinition.BEntry<>(v.group, v.role);
        return mapEntry;
    }

    @Override
    public Entry marshal(XMLProcessDefinition.BEntry<String, String> mapEntry) throws Exception {
        Entry entree = new Entry();
        entree.group = mapEntry.getKey();
        entree.role = mapEntry.getValue();
        return entree;
    }

    public static class Entry {

        public String group;

        public String role;

    }

}
