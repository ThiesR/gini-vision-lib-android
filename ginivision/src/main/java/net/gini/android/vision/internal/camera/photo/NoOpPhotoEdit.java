package net.gini.android.vision.internal.camera.photo;

import android.support.annotation.NonNull;

/**
 * @exclude
 */
class NoOpPhotoEdit extends PhotoEdit {

    NoOpPhotoEdit(@NonNull final Photo photo) {
        super(photo);
    }

    @NonNull
    @Override
    public PhotoEdit rotateTo(final int degrees) {
        getPhoto().setRotationForDisplay(degrees);
        return this;
    }

    @NonNull
    @Override
    public PhotoEdit compressBy(final int quality) {
        return this;
    }

    @NonNull
    @Override
    public PhotoEdit compressByDefault() {
        return this;
    }
}
