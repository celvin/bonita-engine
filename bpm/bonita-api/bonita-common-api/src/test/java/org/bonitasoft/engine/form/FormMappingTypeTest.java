/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package org.bonitasoft.engine.form;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.bonitasoft.engine.bpm.bar.form.model.FormMappingDefinition;
import org.bonitasoft.engine.bpm.bar.form.model.FormMappingModel;
import org.junit.Test;

/**
 * author Emmanuel Duchastenier
 */
public class FormMappingTypeTest {

    @Test
    public void getTypeFromIdShouldReturnProperEnumValue() throws JAXBException {
        FormMappingModel formMappingModel = new FormMappingModel();
        formMappingModel.addFormMapping(new FormMappingDefinition("lala", null, null));
        /*
         * Employee_actor.addActor(new Actor("lala"));
         * Employee_actor.addActor(new Actor("tralala"));
         * Employee_actor.getActors().get(0).addUser("william.jobs");
         * Employee_actor.getActors().get(0).addGroup("RD");
         * Employee_actor.getActors().get(0).addRole("dev");
         * Employee_actor.getActors().get(1).addUser("lala.ru");
         */
        StringWriter result = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(FormMappingModel.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(formMappingModel, result);
        String result2 = result.toString();
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        Object formMappingResult = unmarshaller.unmarshal(new StringReader(result2));
        assertThat(formMappingModel).isEqualTo(formMappingResult);
        /*
         * assertThat(FormMappingType.getTypeFromId(1)).isEqualTo(FormMappingType.PROCESS_START);
         * assertThat(FormMappingType.getTypeFromId(2)).isEqualTo(FormMappingType.PROCESS_OVERVIEW);
         * assertThat(FormMappingType.getTypeFromId(3)).isEqualTo(FormMappingType.TASK);
         */
    }
}
