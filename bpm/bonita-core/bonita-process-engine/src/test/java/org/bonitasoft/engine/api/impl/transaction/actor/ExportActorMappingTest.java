/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 ******************************************************************************/

package org.bonitasoft.engine.api.impl.transaction.actor;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.bonitasoft.engine.bpm.bar.ActorMappingConverter;
import org.bonitasoft.engine.bpm.bar.actorMapping.Actor;
import org.bonitasoft.engine.bpm.bar.actorMapping.ActorMapping;
import org.junit.Test;

/**
 * Created by mazourd on 13/07/15.
 */
public class ExportActorMappingTest {

    /*
     * Test the deserialization of an empty actor mapping
     */
    @Test
    public void testActorMappingNull() throws Exception {
        ActorMapping employeeActor = new ActorMapping();
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
     * Test the deserialization of an empty actor mapping with the converter
     */
    @Test
    public void testActorMappingNullWithJavaIO() throws Exception {
        ActorMapping employeeActor = new ActorMapping();
        byte[] result;
        ActorMappingConverter actorMappingConverter = new ActorMappingConverter();
        result = actorMappingConverter.serializeToXML(employeeActor);
        ActorMapping result2 = actorMappingConverter.deserializeFromXML(result);
        assertThat(employeeActor).isEqualTo(result2);
        System.out.print(result);
        System.out.print(result2);
    }

    /*
     * Test the deserialization of actor mapping with an empty actor with the converter
     */
    @Test
    public void testActorMappingWithOneEmptyActorJavaIO() throws Exception {
        ActorMapping employeeActor = new ActorMapping();
        Actor actor = new Actor("lulu");
        employeeActor.addActor(actor);
        byte[] result;
        ActorMappingConverter actorMappingConverter = new ActorMappingConverter();
        result = actorMappingConverter.serializeToXML(employeeActor);
        ActorMapping result2 = actorMappingConverter.deserializeFromXML(result);
        assertThat(employeeActor).isEqualTo(result2);
        System.out.print(result);
        System.out.print(result2);
    }

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
        employeeActor.getActors().get(0).setDescription("Just here for the tests");
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
