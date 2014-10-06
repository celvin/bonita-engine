/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
 **/
package org.bonitasoft.engine.persistence;

import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.hibernate.service.jta.platform.internal.AbstractJtaPlatform;

/**
 * @author Charles Souillard
 */
public class JNDIAtomikosJtaPlatform extends AbstractJtaPlatform {

    private static final long serialVersionUID = 4893085097625997082L;

    public static final String USER_TRANSACTION = "java:comp/UserTransaction";
    public static final String TRANSACTION_SERVICE = "java:comp/env/TransactionManager";

    private final String userTransactionJndiName;

    private final String transactionServiceJndiName;

    public JNDIAtomikosJtaPlatform() {
        transactionServiceJndiName = System.getProperty("sysprop.bonita.transaction.manager", TRANSACTION_SERVICE);
        userTransactionJndiName = System.getProperty("sysprop.bonita.userTransaction", USER_TRANSACTION);
    }

    @Override
    protected TransactionManager locateTransactionManager() {
        // Force the lookup to JNDI to find the TransactionManager : since we share it between
        // Hibernate and Quartz, I prefer to force the JNDI lookup in order to be sure that
        // they are using the same instance.
        return (TransactionManager) jndiService().locate(transactionServiceJndiName);
    }

    @Override
    protected UserTransaction locateUserTransaction() {
        return (UserTransaction) jndiService().locate(userTransactionJndiName);
    }

}
