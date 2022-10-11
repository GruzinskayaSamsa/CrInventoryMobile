package com.example.storageappmobile;

import android.graphics.Bitmap;

public class ImageEventBus {

    private Bitmap newImage, oldImage;

    public void setNewImage(Bitmap image) {
        oldImage = newImage;
        newImage = image;
    }

    public Bitmap getImage() {
        return newImage;
    }

}
