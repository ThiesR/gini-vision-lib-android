package net.gini.android.vision.camera;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.gini.android.vision.DocumentImportEnabledFileTypes;
import net.gini.android.vision.GiniVisionFeatureConfiguration;
import net.gini.android.vision.internal.permission.PermissionRequestListener;
import net.gini.android.vision.internal.permission.RuntimePermissions;

/**
 * <h3>Component API</h3>
 *
 * <p>
 *     {@code CameraFragmentStandard} is the main entry point to the Gini Vision Library when using the Component API without the Android Support Library.
 * </p>
 * <p>
 *     It shows a camera preview with tap-to-focus functionality and a trigger button. The camera preview also shows document corner guides to which the user should align the document.
 * </p>
 * <p>
 *     If instantiated with {@link CameraFragmentStandard#createInstance(DocumentImportEnabledFileTypes)} then a button for importing documents is shown next to the trigger button. A hint popup is displayed the first time the Gini Vision Library is used to inform the user about document importing.
 * </p>
 * <p>
 *     For importing documents {@code READ_EXTERNAL_STORAGE} permission is required and if the permission is not granted the Gini Vision Library will prompt the user to grant the permission. See @{code Customizing the Camera Screen} on how to override the message and button titles for the rationale and on permission denial alerts.
 * </p>
 * <p>
 *     Include the {@code CameraFragmentStandard} into your layout either directly with {@code <fragment>} in your Activity's layout or using the {@link android.app.FragmentManager} and one of the {@code createInstance()} methods.
 * </p>
 * <p>
 *     Your Activity must implement the {@link CameraFragmentListener} interface to receive events from the Camera Fragment. Failing to do so will throw an exception.
 * </p>
 * <p>
 *     Your Activity is automatically set as the listener in {@link CameraFragmentStandard#onAttach(Context)}.
 * </p>
 *
 * <h3>Customizing the Camera Screen</h3>
 *
 * <p>
 *     See the {@link CameraActivity} for details.
 * </p>
 */
public class CameraFragmentStandard extends Fragment implements CameraFragmentInterface, CameraFragmentImplCallback {

    public static CameraFragmentStandard createInstance() {
        return new CameraFragmentStandard();
    }

    /**
     * <p>
     *     Factory method for creating a new instance of the Fragment with document import enabled for the specified file types.
     * </p>
     * @param giniVisionFeatureConfiguration feature configuration
     * @return a new instance of the Fragment
     */
    public static CameraFragmentStandard createInstance(
            @NonNull final GiniVisionFeatureConfiguration giniVisionFeatureConfiguration) {
        CameraFragmentStandard fragment = new CameraFragmentStandard();
        fragment.setArguments(
                CameraFragmentHelper.createArguments(giniVisionFeatureConfiguration));
        return fragment;
    }

    private CameraFragmentImpl mFragmentImpl;
    private final RuntimePermissions mRuntimePermissions = new RuntimePermissions();

    /**
     * @exclude
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mFragmentImpl = CameraFragmentHelper.createFragmentImpl(this, getArguments());
        CameraFragmentHelper.setListener(mFragmentImpl, context);
    }

    /**
     * @exclude
     */
    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return;
        }
        mFragmentImpl = CameraFragmentHelper.createFragmentImpl(this, getArguments());
        CameraFragmentHelper.setListener(mFragmentImpl, activity);
    }

    /**
     * @exclude
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFragmentImpl.onCreate(savedInstanceState);
    }

    /**
     * @exclude
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return mFragmentImpl.onCreateView(inflater, container, savedInstanceState);
    }

    /**
     * @exclude
     */
    @Override
    public void onStart() {
        super.onStart();
        mFragmentImpl.onStart();
    }

    /**
     * @exclude
     */
    @Override
    public void onStop() {
        super.onStop();
        mFragmentImpl.onStop();
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        boolean handled = mFragmentImpl.onActivityResult(requestCode, resultCode, data);
        if (!handled) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode,
            @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        boolean handled = mRuntimePermissions.onRequestPermissionsResult(requestCode, permissions,
                grantResults);
        if (!handled) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Deprecated
    @Override
    public void showDocumentCornerGuides() {
        if (mFragmentImpl == null) {
            return;
        }
        mFragmentImpl.showDocumentCornerGuides();
    }

    @Deprecated
    @Override
    public void hideDocumentCornerGuides() {
        if (mFragmentImpl == null) {
            return;
        }
        mFragmentImpl.hideDocumentCornerGuides();
    }

    @Deprecated
    @Override
    public void showCameraTriggerButton() {
        if (mFragmentImpl == null) {
            return;
        }
        mFragmentImpl.showCameraTriggerButton();
    }

    @Deprecated
    @Override
    public void hideCameraTriggerButton() {
        if (mFragmentImpl == null) {
            return;
        }
        mFragmentImpl.hideCameraTriggerButton();
    }

    @Override
    public void showInterface() {
        if (mFragmentImpl == null) {
            return;
        }
        mFragmentImpl.showInterface();
    }

    @Override
    public void hideInterface() {
        if (mFragmentImpl == null) {
            return;
        }
        mFragmentImpl.hideInterface();
    }

    @Override
    public void requestPermission(@NonNull final String permission,
            @NonNull final PermissionRequestListener listener) {
        mRuntimePermissions.requestPermission(this, permission, listener);
    }

    @Override
    public void showAlertDialog(@StringRes final int message,
            @StringRes final int positiveButtonTitle,
            @NonNull final DialogInterface.OnClickListener positiveButtonClickListener,
            @StringRes final int negativeButtonTitle) {
        final Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        showAlertDialog(activity.getString(message), positiveButtonTitle,
                positiveButtonClickListener, negativeButtonTitle);
    }

    @Override
    public void showAlertDialog(@NonNull final String message,
            @StringRes final int positiveButtonTitle,
            @NonNull final DialogInterface.OnClickListener positiveButtonClickListener,
            @StringRes final int negativeButtonTitle) {
        final Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        final AlertDialog alertDialog = new AlertDialog.Builder(activity)
                .setMessage(message)
                .setPositiveButton(positiveButtonTitle, positiveButtonClickListener)
                .setNegativeButton(negativeButtonTitle, null)
                .create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }
}
