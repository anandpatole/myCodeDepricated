package com.cheep.strategicpartner;

/**
 * Created by giteeka on 31/7/17.
 * Media model for Type image and video
 *
 */

class MediaModel {

    enum MediaType {
        IMAGE,
        VIDEO
    }


    public MediaModel(String path, MediaType type) {
        this.path = path;
        this.type = type;
    }

    String path = "";
    MediaType type;
}
