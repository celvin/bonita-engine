/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package org.bonitasoft.engine.profile.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.xml.ElementBinding;

/**
 * @author Matthieu Chaffotte
 */
public class RoleNamesBinding extends ElementBinding {

    private final List<String> roles;

    public RoleNamesBinding() {
        roles = new ArrayList<String>();
    }

    @Override
    public void setAttributes(final Map<String, String> attributes) {
    }

    @Override
    public void setChildElement(final String name, final String value, final Map<String, String> attributes) {
        if ("role".equals(name)) {
            roles.add(value);
        }
    }

    @Override
    public void setChildObject(final String name, final Object value) {
    }

    @Override
    public Object getObject() {
        return roles;
    }

    @Override
    public String getElementTag() {
        return "roles";
    }

}
