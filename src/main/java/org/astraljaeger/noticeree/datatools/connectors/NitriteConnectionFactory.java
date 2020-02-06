/*
 * Copyright(c) AstralJaeger 2020.
 */

package org.astraljaeger.noticeree.datatools.connectors;

public class NitriteConnectionFactory implements IConnectionFactory{

    @Override
    public IConnection getConnection() {
        return new NitriteConnection();
    }
}
