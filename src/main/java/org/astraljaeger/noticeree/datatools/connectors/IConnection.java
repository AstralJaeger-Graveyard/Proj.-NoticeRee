/*
 * Copyright(c) AstralJaeger 2020.
 */

package org.astraljaeger.noticeree.datatools.connectors;

import java.util.Optional;
import org.astraljaeger.noticeree.datatools.data.Chatter;

public interface IConnection {

    /**
     * adds a new chatter to the local database
     * @param chatter the chatter to add
     */
    public void addChatter(Chatter chatter);

    /**
     * Updates a chatter with the selected username to the passed object
     * @param username the chatter to change
     * @param chatter the new data to replace the old data with
     */
    public void updateChatter(String username, Chatter chatter);

    /**
     * Removes a chatter
     * @param chatter the chatter to remove
     */
    public void removeChatter(Chatter chatter);

    /**
     * Returns an array of chatters, returns an empty array if none are found
     * @param start the first index
     * @param limit the amount of chatters to return
     * @return the found chatters, empty array (length 0) if none found.
     */
    public Chatter[] getChatter(int start, int limit);

    /**
     * Finds chatter with specified username
     * @param username the username to be found
     * @return the found chatter, empty if none found
     */
    public Optional<Chatter> getChatter(String username);

    /**
     * Database information
     * @return a string to show in the about panel
     */
    public default String getInfo(){
        return "<Empty connector>";
    }
}
