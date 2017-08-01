package com.cheep.strategicpartner;

/**
 * Created by giteeka on 31/7/17.
 */

public class MediaModel {

    enum MediaType {
        IMAGE,
        VIDEO
    }

    public MediaModel() {

    }

    public MediaModel(String path, MediaType type) {
        this.path = path;
        this.type = type;
    }

    String path = "";
    MediaType type;
}
