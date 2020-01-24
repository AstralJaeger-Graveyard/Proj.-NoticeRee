package org.astraljaeger.noticeree.DataTools;

import lombok.Getter;
import lombok.Setter;
import org.dizitart.no2.IndexType;
import org.dizitart.no2.objects.Id;
import org.dizitart.no2.objects.Index;
import org.dizitart.no2.objects.Indices;

import java.util.List;

@Indices({
        @Index(value = "id", type = IndexType.Unique)
})
public class Chatter {

    @Id
    @Getter
    @Setter
    private int id;

    @Getter
    @Setter
    private String username;

    @Getter
    @Setter
    private String welcomeMessage;

    @Getter
    @Setter
    private List<String> sounds;

    public Chatter(){

    }

    public Chatter(int id, String username){
        this.id = id;
        this.username = username;
        this.welcomeMessage = "";
    }

    public String toString(){
        return String.format("[Chatter %d %s]", id, username);
    }
}
