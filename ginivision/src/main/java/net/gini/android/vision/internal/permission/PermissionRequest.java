package net.gini.android.vision.internal.permission;

import android.support.annotation.NonNull;

/**
 * @exclude
 */
interface PermissionRequest<T> {

    void requestPermission(@NonNull final T context);

    void requestPermissionWithoutRationale(@NonNull final T context);

    void onRequestPermissionsResult(@NonNull final String[] permissions,
            @NonNull final int[] grantResults);
}
