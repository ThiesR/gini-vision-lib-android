package net.gini.android.vision.internal.ui;

import android.content.DialogInterface;
import android.support.annotation.NonNull;

/**
 * Created by Alpar Szotyori on 05.06.2018.
 *
 * Copyright (c) 2018 Gini GmbH.
 */

/**
 * @exclude
 */
public interface AlertDialogFragmentListener {

    void onPositiveButtonClicked(@NonNull final DialogInterface dialog, final int dialogId);

    void onNegativeButtonClicked(@NonNull final DialogInterface dialog, final int dialogId);

}
