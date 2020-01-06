/*
 * Copyright(c) AstralJaeger 2020.
 */

package org.astraljaeger.noticeree.DataTools;

import lombok.Getter;
import lombok.Setter;

public class ConfigItem {


    @Getter
    @Setter
    private String token;

    public ConfigItem(){

    }

    public ConfigItem(String token){
        this.token = token;
    }
}
