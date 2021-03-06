package net.gini.android.vision;

import android.app.Activity;
import android.support.annotation.NonNull;

/**
 * Created by Alpar Szotyori on 08.05.2019.
 *
 * Copyright (c) 2019 Gini GmbH.
 */
public abstract class GiniVisionBasePresenter<V extends GiniVisionBaseView> {

    private final Activity mActivity;
    private final V mView;

    protected GiniVisionBasePresenter(@NonNull final Activity activity, @NonNull final V view) {
        mActivity = activity;
        mView = view;
    }

    @NonNull
    public Activity getActivity() {
        return mActivity;
    }

    @NonNull
    protected V getView() {
        return mView;
    }

    public abstract void start();

    public abstract void stop();
}
