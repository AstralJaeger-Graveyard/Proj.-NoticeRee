/*
 * Copyright(c) AstralJaeger 2020.
 */

package org.astraljaeger.noticeree.datatools.connectors;

import static org.dizitart.no2.filters.Filters.eq;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.astraljaeger.noticeree.Configuration;
import org.astraljaeger.noticeree.datatools.data.Chatter;
import org.astraljaeger.noticeree.datatools.data.Sound;
import org.dizitart.no2.Cursor;
import org.dizitart.no2.Document;
import org.dizitart.no2.FindOptions;
import org.dizitart.no2.Index;
import org.dizitart.no2.IndexOptions;
import org.dizitart.no2.IndexType;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteCollection;

public class NitriteConnection implements IConnection{

    private static final Logger logger = LogManager.getLogger(NitriteConnection.class.getSimpleName());
    private static final String FILE_NAME = "data.db"; // Name of database file

    private static final String USERNAME = "username";
    private static final String WELCOME_MESSAGE = "welcomeMessage";
    private static final String LAST_USED = "lastUsed";

    private static final String PRIORITY = "priority";
    private static final String LABEL = "label";
    private static final String NSFW = "nsfw";
    private static final String FILE = "file";
    private static final String ORIGINAL_FILE = "originalFile";

    private Nitrite nitrite;
    private NitriteCollection chatterCollection;
    private NitriteCollection soundCollection;

    public NitriteConnection(){
        logger.debug("Initializing nitrite connector");
        nitrite = Nitrite.builder()
            .filePath(Configuration.getAppDataDirectory() + FILE_NAME)
            .openOrCreate();

        logger.debug("Retrieving collections and indexing them");
        chatterCollection = nitrite.getCollection(Chatter.class.getName());
        if(notIndexed(chatterCollection))
            chatterCollection.createIndex(USERNAME, IndexOptions.indexOptions(IndexType.Unique));
        soundCollection = nitrite.getCollection(Sound.class.getName());
        if(notIndexed(soundCollection))
            soundCollection.createIndex(USERNAME, IndexOptions.indexOptions(IndexType.NonUnique));
    }

    public void addChatter(Chatter chatter){
        logger.trace("Adding new chatter {}", chatter.getUsername());
        chatterCollection.insert(chatter2Doc(chatter));
        for(Sound sound : chatter.getSounds()){
            soundCollection.insert(sound2Doc(sound));
        }
    }

    public void updateChatter(String username, Chatter chatter){
        logger.trace("Updating chatter {} to {}", username, chatter.getUsername());
        chatterCollection.remove(eq(USERNAME, username));
        soundCollection.remove(eq(USERNAME, username));
        chatterCollection.insert(chatter2Doc(chatter));
        for(Sound sound: chatter.getSounds())
            soundCollection.insert(sound2Doc(sound));
    }

    public void removeChatter(Chatter chatter){
        logger.trace("Removing chatter {}", chatter.getUsername());
        chatterCollection.remove(eq(USERNAME, chatter.getUsername()));
        soundCollection.remove(eq(USERNAME, chatter.getUsername()));
    }

    @Override
    public Chatter[] getChatters(int start, int limit) {

        List<Chatter> chatterList = new ArrayList<>();
        Cursor results = chatterCollection.find(FindOptions.limit(start, limit));
        for(Document document : results){
            chatterList.add(reconstructChatter(document));
        }

        return chatterList.toArray(new Chatter[0]);
    }

    @Override
    public Chatter[] getChatters() {

        Cursor results = chatterCollection.find();
        List<Chatter> chatters = new ArrayList<>(results.size() + 2);
        for(Document document : results)
            chatters.add(reconstructChatter(document));

        return chatters.toArray(new Chatter[0]);
    }

    @Override
    public Optional<Chatter> getChatter(String username) {
        Optional<Chatter> chatterOptional = Optional.empty();
        Cursor result = chatterCollection.find(eq(USERNAME, username));
        int count = 0;
        for(Document document : result){
            if(count > 0)
                throw new PersistenceException("Database can only have 1 user with name " + username);

            chatterOptional = Optional.of(reconstructChatter(document));
            count++;
        }
        return chatterOptional;
    }

    @Override
    public String getInfo() {
        return "Nitrite - NOsql Object database";
    }

    // region Utility

    private boolean notIndexed(NitriteCollection collection){
        Collection<Index> indices = collection.listIndices();
        for(Index index : indices){
            if(index.getField().equals(USERNAME)){
                return false;
            }
        }
        return true;
    }

    private Document chatter2Doc(Chatter chatter){
        return Document.createDocument(USERNAME, chatter.getUsername())
            .put(WELCOME_MESSAGE, chatter.getWelcomeMessage())
            .put(LAST_USED, chatter.getLastUsed());
    }

    private Document sound2Doc(Sound sound){
        return Document.createDocument(USERNAME, sound.getUsername())
            .put(PRIORITY, sound.getPriority())
            .put(LABEL, sound.getLabel())
            .put(NSFW, sound.isNsfw())
            .put(FILE, sound.getFile().toURI().toString())
            .put(ORIGINAL_FILE, sound.getOriginalFile().toString());
    }

    private Chatter doc2Chatter(Document document){
        return new Chatter(
            document.get(USERNAME, String.class),
            document.get(WELCOME_MESSAGE, String.class),
            Collections.emptyList(),
            document.get(LAST_USED, LocalDateTime.class)
        );
    }

    private Sound doc2Sound(Document document){
        return new Sound(
            document.get(USERNAME, String.class),
            document.get(PRIORITY, Integer.class),
            document.get(LABEL, String.class),
            document.get(NSFW, Boolean.class),
            new File(document.get(ORIGINAL_FILE, String.class))
        );
    }

    private Chatter reconstructChatter(Document document){
        Chatter chatter = doc2Chatter(document);
        List<Sound> sounds = new ArrayList<>();
        Cursor soundResults = soundCollection.find(eq(USERNAME, chatter.getUsername()));
        for (Document soundDoc : soundResults)
            sounds.add(doc2Sound(soundDoc));
        chatter.getSounds().addAll(sounds.toArray(new Sound[0]));
        return chatter;
    }

    // endregion
}
