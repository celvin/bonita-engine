/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package org.bonitasoft.engine.actor.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Matthieu Chaffotte
 */
@XmlRootElement(name = "actorMappings", namespace = "http://www.bonitasoft.org/ns/actormapping/6.0")
@XmlAccessorType(XmlAccessType.FIELD)
public class ActorMapping {

    //@XmlElementWrapper(name = "actor-mappings",required = true)
    @XmlElement(name = "actorMapping", required = false)
    private List<Actor> actors;

    public ActorMapping() {
        actors = new ArrayList<Actor>(10);
    }

    public List<Actor> getActors() {
        return actors;
    }

    public void setActors(List<Actor> actors) {
        this.actors = actors;
    }

    public void addActor(final Actor actor) {
        actors.add(actor);
    }

    @Override
    public String toString() {
        return "ActorMapping{" +
                "actors=" + actors +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ActorMapping that = (ActorMapping) o;
        return Objects.equals(actors, that.actors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(actors);
    }
}
