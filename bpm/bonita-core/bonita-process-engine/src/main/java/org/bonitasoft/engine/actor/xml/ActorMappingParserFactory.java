/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/

package org.bonitasoft.engine.actor.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.api.impl.transaction.actor.ImportActorMapping;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.xml.ElementBinding;
import org.bonitasoft.engine.xml.Parser;
import org.bonitasoft.engine.xml.ParserFactory;
import org.bonitasoft.engine.xml.SInvalidSchemaException;

/**
 * @author Baptiste Mesta
 */
public class ActorMappingParserFactory {

    private ParserFactory parserFactory;

    public ActorMappingParserFactory(ParserFactory parserFactory) {
        this.parserFactory = parserFactory;
    }

    public Parser create(){
        final List<Class<? extends ElementBinding>> bindings = new ArrayList<Class<? extends ElementBinding>>();
        bindings.add(ActorMappingBinding.class);
        bindings.add(ActorBinding.class);
        bindings.add(UserNamesBinding.class);
        bindings.add(GroupPathsBinding.class);
        bindings.add(RoleNamesBinding.class);
        bindings.add(ActorMembershipBinding.class);
        final Parser parser = parserFactory.createParser(bindings);
        final InputStream resource = ImportActorMapping.class.getClassLoader().getResourceAsStream("actorMapping.xsd");
        try {
            parser.setSchema(resource);
            return parser;
        } catch (final SInvalidSchemaException ise) {
            throw new BonitaRuntimeException(ise);
        } finally {
            try {
                resource.close();
            } catch (final IOException ioe) {
                throw new BonitaRuntimeException(ioe);
            }
        }
    }

}
