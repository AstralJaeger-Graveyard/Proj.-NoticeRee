/*
 * Copyright(c) AstralJaeger 2020.
 */

package org.astraljaeger.noticeree.datatools.connectors;

import java.util.Optional;
import org.astraljaeger.noticeree.datatools.data.Chatter;

public class NitriteConnection implements IConnection{

    public NitriteConnection(){

    }

    public void addChatter(Chatter chatter){

    }

    public void updateChatter(String username, Chatter chatter){

    }

    public void removeChatter(Chatter chatter){

    }

    @Override
    public Chatter[] getChatter(int start, int limit) {
        return new Chatter[0];
    }

    @Override
    public Optional<Chatter> getChatter(String username) {
        return Optional.empty();
    }

    @Override
    public String getInfo() {
        return "<Empty>";
    }
}
