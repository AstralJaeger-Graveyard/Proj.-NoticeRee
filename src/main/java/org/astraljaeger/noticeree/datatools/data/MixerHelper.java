package org.astraljaeger.noticeree.datatools.data;

import lombok.Getter;
import lombok.Setter;

import javax.sound.sampled.Mixer;

public class MixerHelper {

    @Getter
    @Setter
    private Mixer mixer;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String description;

    public MixerHelper(String name, String description, Mixer mixer){
        this.name = name;
        this.description = description;
        this.mixer = mixer;
    }

    @Override
    public String toString(){
        return String.format("%s", name);
    }
}