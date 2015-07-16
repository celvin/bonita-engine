/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package org.bonitasoft.engine.actor.xml;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.bonitasoft.engine.bpm.bar.xml.XMLProcessDefinition.BEntry;

/**
 * @author Matthieu Chaffotte
 * @author Baptiste Mesta
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Actor {

    @XmlAttribute
    private String name;
    @XmlAttribute
    private String description;
    @XmlElementWrapper(name = "users", required = false)
    @XmlElement(name = "user")
    private Set<String> users;
    @XmlElementWrapper(name = "groups", required = false)
    @XmlElement(name = "group")
    private Set<String> groups;
    @XmlElementWrapper(name = "roles", required = false)
    @XmlElement(name = "role")
    private Set<String> roles;
    @XmlElementWrapper(name = "memberships", required = false)
    @XmlJavaTypeAdapter(BEntryAdapter.class)
    @XmlElement(name = "membership")
    private Set<BEntry<String, String>> memberships = null;

    public Actor(final String name) {
        this.name = name;
        users = new HashSet<String>();
        groups = new HashSet<String>();
        roles = new HashSet<String>();
        memberships = new HashSet<BEntry<String, String>>();
    }

    public Actor() {

    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void addGroup(final String group) {
        groups.add(group);
    }

    public void addGroups(final List<String> groups) {
        this.groups = new HashSet<String>(groups);
    }

    public Set<String> getGroups() {
        return groups;
    }

    public void addRole(final String role) {
        roles.add(role);
    }

    public void addRoles(final List<String> roles) {
        this.roles = new HashSet<String>(roles);
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void addUser(final String user) {
        users.add(user);
    }

    public void addUsers(final List<String> userNames) {
        users = new HashSet<String>(userNames);
    }

    public Set<String> getUsers() {
        return users;
    }

    public void addMembership(final String groupName, final String roleName) {
        memberships.add(new BEntry<String, String>(groupName, roleName));
    }

    public Set<BEntry<String, String>> getMemberships() {
        return memberships;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Actor actor = (Actor) o;
        return Objects.equals(name, actor.name) &&
                Objects.equals(description, actor.description) &&
                Objects.equals(users, actor.users) &&
                Objects.equals(groups, actor.groups) &&
                Objects.equals(roles, actor.roles) &&
                Objects.equals(memberships, actor.memberships);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, users, groups, roles, memberships);
    }

    @Override
    public String toString() {
        return "Actor{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", users=" + users +
                ", groups=" + groups +
                ", roles=" + roles +
                ", memberships=" + memberships +
                '}';
    }
}
