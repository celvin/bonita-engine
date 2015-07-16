/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package org.bonitasoft.engine.api.impl.transaction.actor;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.bonitasoft.engine.actor.xml.Actor;
import org.bonitasoft.engine.actor.xml.ActorMapping;
import org.junit.Test;

/**
 * Created by mazourd on 13/07/15.
 */
public class ExportActorMappingTest {

    /*
     * Test the creation of a simple actor mapping xml file, and if it can be read correctly
     * Prints the resulting xml for debugging purposes
     */
    @Test
    public void testCreateActorMappingFromClass() throws Exception {
        ActorMapping employeeActor = new ActorMapping();
        employeeActor.addActor(new Actor("lala"));
        StringWriter result = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(ActorMapping.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(employeeActor, result);
        String result2 = result.toString();
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        Object actors = unmarshaller.unmarshal(new StringReader(result2));
        assertThat(employeeActor).isEqualTo(actors);
        System.out.print(result2);
    }

    /*
     * Same as above but with a more complex structure
     */
    @Test
    public void testActorMembership() throws Exception {
        ActorMapping employeeActor = new ActorMapping();
        employeeActor.addActor(new Actor("EmployeeMembership1"));
        employeeActor.addActor(new Actor("EmployeeMembership2"));
        employeeActor.getActors().get(0).addUser("william.jobs");
        employeeActor.getActors().get(0).addGroup("RD");
        employeeActor.getActors().get(0).addRole("dev");
        employeeActor.getActors().get(1).addUser("lala.ru");
        employeeActor.getActors().get(1).addMembership("group1", "role1");
        StringWriter result = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(ActorMapping.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(employeeActor, result);
        String result2 = result.toString();
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        Object actors = unmarshaller.unmarshal(new StringReader(result2));
        assertThat(employeeActor).isEqualToComparingFieldByField((ActorMapping) actors);
        System.out.println(actors.toString());
        System.out.print(result2);
    }
}
