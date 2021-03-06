package net.gini.android.vision.internal.util;

import static net.gini.android.vision.internal.util.ContextHelper.getClientApplicationId;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;

import java.util.ArrayList;

/**
 * Created by Alpar Szotyori on 04.02.2019.
 *
 * Copyright (c) 2019 Gini GmbH.
 */

/**
 * @exclude
 */
public final class ApplicationHelper {

    public static void startApplicationDetailsSettings(@NonNull final Activity activity) {
        final Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        final Uri uri = Uri.fromParts("package", getClientApplicationId(activity), null);
        intent.setData(uri);
        activity.startActivity(intent);
    }

    public static boolean isDefaultForMimeType(@NonNull final Application app,
            @NonNull final String mimeType) {
        final ArrayList<ComponentName> components = new ArrayList<>();
        final ArrayList<IntentFilter> filters = new ArrayList<>();
        final String packageName = ContextHelper.getClientApplicationId(app);
        app.getPackageManager().getPreferredActivities(filters, components, packageName);
        for (final IntentFilter filter : filters) {
            if (filter.hasDataType(mimeType)) {
                return true;
            }
        }
        return false;
    }

    private ApplicationHelper() {
    }

}
