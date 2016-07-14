package net.gini.android.vision.camera;

import static net.gini.android.vision.camera.Util.cameraExceptionToGiniVisionError;
import static net.gini.android.vision.util.AndroidHelper.isMarshmallowOrLater;
import static net.gini.android.vision.util.ContextHelper.getClientApplicationId;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import net.gini.android.vision.Document;
import net.gini.android.vision.GiniVisionError;
import net.gini.android.vision.R;
import net.gini.android.vision.camera.api.CameraController;
import net.gini.android.vision.camera.api.CameraInterface;
import net.gini.android.vision.camera.photo.Photo;
import net.gini.android.vision.ui.FragmentImplCallback;
import net.gini.android.vision.ui.ViewStubSafeInflater;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class CameraFragmentImpl implements CameraFragmentInterface {

    private static final CameraFragmentListener NO_OP_LISTENER = new CameraFragmentListener() {
        @Override
        public void onDocumentAvailable(@NonNull Document document) {
        }

        @Override
        public void onError(@NonNull GiniVisionError error) {
        }
    };

    private final FragmentImplCallback mFragment;
    private CameraFragmentListener mListener = NO_OP_LISTENER;

    private CameraController mCameraController;
    private CameraListener mCameraListener;

    private ImageView mCameraPreview;
    private ImageView mImageCorners;
    private ImageButton mButtonCameraTrigger;
    private LinearLayout mLayoutNoPermission;

    private ViewStubSafeInflater mViewStubInflater;

    CameraFragmentImpl(@NonNull FragmentImplCallback fragment) {
        mFragment = fragment;
    }

    public void setListener(CameraFragmentListener listener) {
        if (listener == null) {
            mListener = NO_OP_LISTENER;
        } else {
            mListener = listener;
        }
        if (mCameraListener != null) {
            mCameraListener.setFragmentListener(mListener);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.gv_fragment_camera, container, false);
        bindViews(view);
        setInputHandlers();
        return view;
    }

    public void onStart() {
        mCameraController = new CameraController();
        createCameraListener();
        mCameraController.setListener(mCameraListener);
        mCameraController.open();
    }

    private void createCameraListener() {
        mCameraListener = new CameraListener(mListener, new CameraListener.Callback() {

            @Override
            public void onHideNoPermissionView() {
                hideNoPermissionView();
                // TODO: this hack is only for the stub library
                mCameraPreview.setImageResource(R.drawable.gv_test_document);
            }

            @Override
            public void onShowNoPermissionView() {
                showNoPermissionView();
            }

            @Override
            public void onCameraError() {
                // TODO: this hack is only for the stub library
                mCameraPreview.setImageDrawable(null);
            }
        });
    }

    public void onStop() {

    }

    private void bindViews(View view) {
        mCameraPreview = (ImageView) view.findViewById(R.id.gv_camera_preview);
        mImageCorners = (ImageView) view.findViewById(R.id.gv_image_corners);
        mButtonCameraTrigger = (ImageButton) view.findViewById(R.id.gv_button_camera_trigger);
        ViewStub stubNoPermission = (ViewStub) view.findViewById(R.id.gv_stub_camera_no_permission);
        mViewStubInflater = new ViewStubSafeInflater(stubNoPermission);
    }

    private void setInputHandlers() {
        mButtonCameraTrigger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: this is only for the stub library
                byte[] testDocument = loadTestDocument();
                if (testDocument != null) {
                    mListener.onDocumentAvailable(Document.fromPhoto(Photo.fromJpeg(testDocument, 0)));
                } else {
                    mListener.onError(new GiniVisionError(GiniVisionError.ErrorCode.CAMERA_UNKNOWN, "Could not load the test document jpeg."));
                }
            }
        });
    }

    @Override
    public void showDocumentCornerGuides() {
        if (isNoPermissionViewVisible()) {
            return;
        }
        showDocumentCornerGuidesAnimated();
    }

    private void showDocumentCornerGuidesAnimated() {
        mImageCorners.animate().alpha(1.0f);
    }

    @Override
    public void hideDocumentCornerGuides() {
        if (isNoPermissionViewVisible()) {
            return;
        }
        hideDocumentCornerGuidesAnimated();
    }

    private void hideDocumentCornerGuidesAnimated() {
        mImageCorners.animate().alpha(0.0f);
    }

    @Override
    public void showCameraTriggerButton() {
        if (isNoPermissionViewVisible()) {
            return;
        }
        showCameraTriggerButtonAnimated();
    }

    private void showCameraTriggerButtonAnimated() {
        mButtonCameraTrigger.animate().alpha(1.0f);
        mButtonCameraTrigger.setEnabled(true);
    }

    @Override
    public void hideCameraTriggerButton() {
        if (isNoPermissionViewVisible()) {
            return;
        }
        hideCameraTriggerButtonAnimated();
    }

    private void hideCameraTriggerButtonAnimated() {
        mButtonCameraTrigger.animate().alpha(0.0f);
        mButtonCameraTrigger.setEnabled(false);
    }

    private void showNoPermissionView() {
        hideCameraPreviewAnimated();
        hideCameraTriggerButtonAnimated();
        hideDocumentCornerGuidesAnimated();
        inflateNoPermissionStub();
        setUpNoPermissionButton();
        if (mLayoutNoPermission != null) {
            mLayoutNoPermission.setVisibility(View.VISIBLE);
        }
    }

    private boolean isNoPermissionViewVisible() {
        return mLayoutNoPermission != null &&
                mLayoutNoPermission.getVisibility() == View.VISIBLE;
    }

    private void inflateNoPermissionStub() {
        if (mLayoutNoPermission == null) {
            mLayoutNoPermission = (LinearLayout) mViewStubInflater.inflate();
        }
    }

    public void hideNoPermissionView() {
        showCameraPreviewAnimated();
        showCameraTriggerButtonAnimated();
        showDocumentCornerGuidesAnimated();
        if (mLayoutNoPermission != null) {
            mLayoutNoPermission.setVisibility(View.GONE);
        }
    }

    private void setUpNoPermissionButton() {
        if (isMarshmallowOrLater()) {
            handleNoPermissionButtonClick();
        } else {
            hideNoPermissionButton();
        }
    }

    private void hideCameraPreviewAnimated() {
        mCameraPreview.animate().alpha(0.0f);
        mCameraPreview.setEnabled(false);
    }

    private void showCameraPreviewAnimated() {
        mCameraPreview.animate().alpha(1.0f);
        mCameraPreview.setEnabled(true);
    }

    private void handleNoPermissionButtonClick() {
        View view = mFragment.getView();
        if (view == null) {
            return;
        }
        Button button = (Button) view.findViewById(R.id.gv_button_camera_no_permission);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startApplicationDetailsSettings();
            }
        });
    }

    private void hideNoPermissionButton() {
        View view = mFragment.getView();
        if (view == null) {
            return;
        }
        Button button = (Button) view.findViewById(R.id.gv_button_camera_no_permission);
        button.setVisibility(View.GONE);
    }

    private void startApplicationDetailsSettings() {
        Activity activity = mFragment.getActivity();
        if (activity == null) {
            return;
        }
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getClientApplicationId(activity), null);
        intent.setData(uri);
        mFragment.startActivity(intent);
    }

    // TODO: used only for the stub library
    private byte[] loadTestDocument() {
        if (mFragment.getActivity() == null) {
            return null;
        }
        InputStream inputStream = null;
        ByteArrayOutputStream outputStream = null;
        try {
            inputStream = mFragment.getActivity().getAssets().open("gv_test_document.jpg");
            outputStream = new ByteArrayOutputStream();
            copyStream(inputStream, outputStream);
        } catch (IOException e) {
            // Ignore
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
        return outputStream.toByteArray();
    }

    // TODO: used only for the stub library
    private OutputStream copyStream(InputStream is, OutputStream os) throws IOException {
        int bufferLength = 8192;
        byte[] buffer = new byte[bufferLength];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            os.write(buffer, 0, bytesRead);
        }
        return os;
    }

    @VisibleForTesting
    static class CameraListener implements CameraInterface.Listener {

        public interface Callback {
            void onHideNoPermissionView();

            void onShowNoPermissionView();

            void onCameraError();
        }

        private CameraFragmentListener mFragmentListener;
        private final Callback mCallback;

        CameraListener(@NonNull CameraFragmentListener fragmentListener, @NonNull Callback callback) {
            mFragmentListener = fragmentListener;
            mCallback = callback;
        }

        public void setFragmentListener(@NonNull CameraFragmentListener fragmentListener) {
            mFragmentListener = fragmentListener;
        }

        @Override
        public void onCameraOpened() {
            mCallback.onHideNoPermissionView();
        }

        @Override
        public void onCameraClosed() {

        }

        @Override
        public void onCameraFocusFinished(boolean success) {

        }

        @Override
        public void onPhotoTaken(@NonNull Photo photo) {

        }

        @Override
        public void onCameraError(@NonNull RuntimeException e) {
            handleCameraException(e);
            mCallback.onCameraError();
        }

        private void handleCameraException(RuntimeException e) {
            GiniVisionError error = cameraExceptionToGiniVisionError(e);
            if (error.getErrorCode() == GiniVisionError.ErrorCode.CAMERA_NO_ACCESS) {
                mCallback.onShowNoPermissionView();
            } else {
                mFragmentListener.onError(cameraExceptionToGiniVisionError(e));
            }
        }
    }
}