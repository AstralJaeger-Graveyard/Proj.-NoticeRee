/*
 * Copyright (c) 2020.
 */

package org.astraljaeger.noticeree;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;

public class Configuration {

    public static String SEPARATOR = FileSystems.getDefault().getSeparator();
    public static String CONFIG_LOCATION =  SEPARATOR + "config" + SEPARATOR;
}
