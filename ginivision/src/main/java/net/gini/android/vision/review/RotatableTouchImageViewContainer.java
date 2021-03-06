package net.gini.android.vision.review;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.ortiz.touch.TouchImageView;

/**
 * Created by Alpar Szotyori on 19.02.2018.
 *
 * Copyright (c) 2018 Gini GmbH.
 */

/**
 * @exclude
 */
public class RotatableTouchImageViewContainer extends RotatableImageViewContainer {

    private TouchImageView mTouchImageView;

    public RotatableTouchImageViewContainer(final Context context) {
        super(context);
    }

    public RotatableTouchImageViewContainer(final Context context,
            @Nullable final AttributeSet attrs) {
        super(context, attrs);
    }

    public RotatableTouchImageViewContainer(final Context context,
            @Nullable final AttributeSet attrs,
            final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public RotatableTouchImageViewContainer(final Context context,
            @Nullable final AttributeSet attrs,
            final int defStyleAttr, final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @NonNull
    @Override
    protected ImageView createImageView(final Context context) {
        mTouchImageView = new TouchImageView(context);
        return mTouchImageView;
    }

    public TouchImageView getTouchImageView() {
        return mTouchImageView;
    }
}
