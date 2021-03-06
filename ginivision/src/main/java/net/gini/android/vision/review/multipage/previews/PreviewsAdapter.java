package net.gini.android.vision.review.multipage.previews;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import net.gini.android.vision.document.GiniVisionDocumentError;
import net.gini.android.vision.document.ImageDocument;
import net.gini.android.vision.document.ImageMultiPageDocument;

/**
 * Created by Alpar Szotyori on 08.05.2018.
 *
 * Copyright (c) 2018 Gini GmbH.
 */

/**
 * @exclude
 */
public class PreviewsAdapter extends FragmentStatePagerAdapter {

    private final ImageMultiPageDocument mMultiPageDocument;
    private final PreviewsAdapterListener mListener;

    public PreviewsAdapter(@NonNull final FragmentManager fm,
            @NonNull final ImageMultiPageDocument multiPageDocument,
            @NonNull final PreviewsAdapterListener listener) {
        super(fm);
        mMultiPageDocument = multiPageDocument;
        mListener = listener;
    }

    @Override
    public int getCount() {
        return mMultiPageDocument.getDocuments().size();
    }

    @Override
    public int getItemPosition(final Object object) {
        // Required for reloading the visible fragment
        return POSITION_NONE;
    }

    @Override
    public Fragment getItem(final int position) {
        final ImageDocument document =
                mMultiPageDocument.getDocuments().get(position);
        final GiniVisionDocumentError documentError =
                mMultiPageDocument.getErrorForDocument(document);
        String errorMessage = null;
        PreviewFragment.ErrorButtonAction errorButtonAction = null;
        if (documentError != null) {
            errorMessage = documentError.getMessage();
            errorButtonAction = mListener.getErrorButtonAction(documentError);
        }
        return PreviewFragment.createInstance(document, errorMessage, errorButtonAction);
    }

    public void rotateImageInCurrentItemBy(@NonNull final ViewPager viewPager, final int degrees) {
        final PreviewFragment fragment = (PreviewFragment) instantiateItem(viewPager,
                viewPager.getCurrentItem());
        fragment.rotateImageViewBy(degrees, true);
    }
}
