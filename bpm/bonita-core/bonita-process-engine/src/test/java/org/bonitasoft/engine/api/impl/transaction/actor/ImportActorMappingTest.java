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

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.actor.xml.Actor;
import org.bonitasoft.engine.actor.xml.ActorMapping;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.junit.Test;

/**
 * Created by mazourd on 03/07/15.
 */
public class ImportActorMappingTest {

    /*
     * Test the correct reading of a pre-existing (ie. before rework of xml gestion) style actor-mapping xml document.
     */
    @Test
    public void testGetActorMappingFromXML() throws Exception {
        InputStream xmlStream = ImportActorMappingTest.class.getResourceAsStream("/complexActorMapping.xml");
        String xmlContent = IOUtils.toString(xmlStream);

        ImportActorMapping importActorMapping = new ImportActorMapping(null, null, -1, xmlContent);
        ActorMapping actorMappingFromXML = importActorMapping.getActorMappingFromXML();

        Actor actorTest = new Actor("Employee actor");
        actorTest.addUser("john");
        actorTest.addRole("dev");
        actorTest.addGroup("/RD");
        actorTest.addMembership("/RD", "dev");
        assertThat(actorMappingFromXML.getActors()).contains(actorTest);
        System.out.print(actorMappingFromXML.getActors().toString());
    }

    /*
     * Tests if the macro reading the actor-mapping xml(s) correctly detects a garbage input.
     */
    @Test
    public void testLeveeExceptionSurXMLNonValide() throws Exception {
        InputStream xmlStream = ImportActorMappingTest.class.getResourceAsStream("/testfaux.xml");
        String xmlContent = IOUtils.toString(xmlStream);

        ImportActorMapping importActorMapping = new ImportActorMapping(null, null, -1, xmlContent);
        try {
            ActorMapping actorMappingFromXML = importActorMapping.getActorMappingFromXML();
            throw new Exception("Devrait échouer à la lecture");
        } catch (SBonitaReadException S) {
            System.out.print("exception correctly raised");
        }
    }
}
