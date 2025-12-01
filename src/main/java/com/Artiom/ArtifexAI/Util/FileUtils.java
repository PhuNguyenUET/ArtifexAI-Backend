package com.Artiom.ArtifexAI.Util;

import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@UtilityClass
public class FileUtils {
    public static String readFromFile(String filePath) {
        Path path = Paths.get(filePath);
        String content = "";

        try {
            content = Files.readString(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return content;
    }
}
