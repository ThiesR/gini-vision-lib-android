package net.gini.android.vision.component.camera;

import static android.app.Activity.RESULT_OK;

import static net.gini.android.vision.example.ExampleUtil.getExtractionsBundle;
import static net.gini.android.vision.example.ExampleUtil.getLegacyExtractionsBundle;
import static net.gini.android.vision.example.ExampleUtil.isIntentActionViewOrSend;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import net.gini.android.models.SpecificExtraction;
import net.gini.android.vision.AsyncCallback;
import net.gini.android.vision.Document;
import net.gini.android.vision.GiniVision;
import net.gini.android.vision.GiniVisionCoordinator;
import net.gini.android.vision.GiniVisionError;
import net.gini.android.vision.ImportedFileValidationException;
import net.gini.android.vision.camera.CameraFragmentInterface;
import net.gini.android.vision.camera.CameraFragmentListener;
import net.gini.android.vision.component.ExtractionsActivity;
import net.gini.android.vision.component.R;
import net.gini.android.vision.component.review.multipage.MultiPageReviewExampleActivity;
import net.gini.android.vision.document.GiniVisionMultiPageDocument;
import net.gini.android.vision.document.QRCodeDocument;
import net.gini.android.vision.example.BaseExampleApp;
import net.gini.android.vision.example.DocumentAnalyzer;
import net.gini.android.vision.example.SingleDocumentAnalyzer;
import net.gini.android.vision.help.HelpActivity;
import net.gini.android.vision.network.model.GiniVisionSpecificExtraction;
import net.gini.android.vision.onboarding.OnboardingFragmentListener;
import net.gini.android.vision.util.CancellationToken;
import net.gini.android.vision.util.IntentHelper;
import net.gini.android.vision.util.UriHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.android.LogcatAppender;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;

/**
 * Contains the logic for the Camera Screen.
 * <p>
 * Code that differs between the standard and the compatibility library is abstracted away and is
 * implemented in the {@code standard} and {@code compat} packages.
 */
public abstract class BaseCameraScreenHandler implements CameraFragmentListener,
        OnboardingFragmentListener {

    // Set to true to allow execution of the custom code check
    private static final boolean DO_CUSTOM_DOCUMENT_CHECK = false;
    private static final Logger LOG = LoggerFactory.getLogger(BaseCameraScreenHandler.class);
    private static final int REVIEW_REQUEST = 1;
    private static final int MULTI_PAGE_REVIEW_REQUEST = 2;
    private static final int ANALYSIS_REQUEST = 3;
    private final Activity mActivity;
    private CameraFragmentInterface mCameraFragmentInterface;
    private GiniVisionCoordinator mGiniVisionCoordinator;
    private Menu mMenu;
    private SingleDocumentAnalyzer mSingleDocumentAnalyzer;
    private CancellationToken mFileImportCancellationToken;

    protected BaseCameraScreenHandler(final Activity activity) {
        mActivity = activity;
    }

    @Override
    public void onCloseOnboarding() {
        removeOnboarding();
    }

    private void removeOnboarding() {
        LOG.debug("Remove the Onboarding Screen");
        if (mMenu != null) {
            mMenu.setGroupVisible(R.id.group, true);
        }
        mCameraFragmentInterface.showInterface();
        setTitlesForCamera();
        removeOnboardingFragment();
    }

    protected abstract void removeOnboardingFragment();

    protected abstract void setTitlesForCamera();

    @Override
    public void onDocumentAvailable(@NonNull final Document document) {
        LOG.debug("Document available {}", document);
        // Cancel analysis to make sure, that the document analysis will start in
        // onShouldAnalyzeDocument()
        getSingleDocumentAnalyzer().cancelAnalysis();
        if (document.isReviewable()) {
            launchReviewScreen(document);
        } else {
            launchAnalysisScreen(document);
        }
    }

    private SingleDocumentAnalyzer getSingleDocumentAnalyzer() {
        if (mSingleDocumentAnalyzer == null) {
            mSingleDocumentAnalyzer =
                    ((BaseExampleApp) mActivity.getApplication()).getSingleDocumentAnalyzer();
        }
        return mSingleDocumentAnalyzer;
    }

    @Override
    public void onQRCodeAvailable(@NonNull final QRCodeDocument qrCodeDocument) {
        mCameraFragmentInterface.showActivityIndicatorAndDisableInteraction();
        getSingleDocumentAnalyzer().cancelAnalysis();
        getSingleDocumentAnalyzer().analyzeDocument(qrCodeDocument,
                new DocumentAnalyzer.Listener() {
                    @Override
                    public void onException(final Exception exception) {
                        mCameraFragmentInterface.hideActivityIndicatorAndEnableInteraction();
                        mCameraFragmentInterface.showError(
                                mActivity.getString(R.string.qrcode_error), 4000);
                    }

                    @Override
                    public void onExtractionsReceived(
                            final Map<String, SpecificExtraction> extractions) {
                        mCameraFragmentInterface.hideActivityIndicatorAndEnableInteraction();
                        final Intent intent = new Intent(mActivity, ExtractionsActivity.class);
                        intent.putExtra(ExtractionsActivity.EXTRA_IN_EXTRACTIONS,
                                getLegacyExtractionsBundle(extractions));
                        mActivity.startActivity(intent);
                        mActivity.setResult(Activity.RESULT_OK);
                        mActivity.finish();
                    }
                });
    }

    @Override
    public void onCheckImportedDocument(@NonNull final Document document,
            @NonNull final DocumentCheckResultCallback callback) {
        // We can apply custom checks here to an imported document and notify the Gini Vision
        // Library about the result

        // As an example we allow only documents smaller than 5MB
        if (DO_CUSTOM_DOCUMENT_CHECK) {
            // Use the Intent with which the document was imported to access its contents
            // (document.getData() may be null)
            final Intent intent = document.getIntent();
            if (intent == null) {
                callback.documentRejected(mActivity.getString(R.string.gv_document_import_error));
                return;
            }
            final Uri uri = IntentHelper.getUri(intent);
            if (uri == null) {
                callback.documentRejected(mActivity.getString(R.string.gv_document_import_error));
                return;
            }
            // IMPORTANT: always call one of the callback methods
            if (hasLessThan5MB(callback, uri)) {
                callback.documentAccepted();
            } else {
                callback.documentRejected(mActivity.getString(R.string.document_size_too_large));
            }
        } else {
            // IMPORTANT: always call one of the callback methods
            callback.documentAccepted();
        }
    }

    private boolean hasLessThan5MB(@NonNull final DocumentCheckResultCallback callback,
            final Uri uri) {
        final int fileSize = UriHelper.getFileSizeFromUri(uri, mActivity);
        return fileSize <= 5 * 1024 * 1024;
    }

    @Override
    public void onError(@NonNull final GiniVisionError error) {
        LOG.error("Gini Vision Lib error: {} - {}", error.getErrorCode(), error.getMessage());
        Toast.makeText(mActivity, mActivity.getString(R.string.gini_vision_error,
                error.getErrorCode(), error.getMessage()), Toast.LENGTH_LONG).show();
    }

    private void launchReviewScreen(final Document document) {
        final Intent intent = getReviewActivityIntent(document);
        mActivity.startActivityForResult(intent, REVIEW_REQUEST);
    }

    protected abstract Intent getReviewActivityIntent(final Document document);

    private void launchAnalysisScreen(final Document document) {
        final Intent intent = getAnalysisActivityIntent(document);
        mActivity.startActivityForResult(intent, ANALYSIS_REQUEST);
    }

    protected abstract Intent getAnalysisActivityIntent(final Document document);

    protected Activity getActivity() {
        return mActivity;
    }

    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REVIEW_REQUEST:
            case MULTI_PAGE_REVIEW_REQUEST:
            case ANALYSIS_REQUEST:
                if (resultCode == RESULT_OK) {
                    mActivity.finish();
                }
                break;
        }
    }

    public boolean onBackPressed() {
        if (isOnboardingVisible()) {
            removeOnboarding();
            return true;
        }
        if (mFileImportCancellationToken != null) {
            mFileImportCancellationToken.cancel();
            mFileImportCancellationToken = null;
        }
        return false;
    }

    protected abstract boolean isOnboardingVisible();

    public void onCreate(final Bundle savedInstanceState) {
        setUpActionBar();
        setTitlesForCamera();

        configureLogging();
        setupGiniVisionCoordinator(mActivity);

        // Deprecated: configuration applied in MainActivity#initGiniVision()
        // Configure the features you would like to use
//        mGiniVisionFeatureConfiguration =
//                GiniVisionFeatureConfiguration.buildNewConfiguration()
//                        .setDocumentImportEnabledFileTypes(
//                                DocumentImportEnabledFileTypes.PDF_AND_IMAGES)
//                        .setFileImportEnabled(true)
//                        .setQRCodeScanningEnabled(true)
//                        .build();

        if (savedInstanceState == null) {
            final Intent intent = mActivity.getIntent();
            if (isIntentActionViewOrSend(intent)) {
                startGiniVisionLibraryForImportedFile(intent);
            } else {
                showCamera();
            }
        } else {
            mCameraFragmentInterface = retrieveCameraFragment();
        }
    }

    protected abstract void setUpActionBar();

    private void showCamera() {
        LOG.debug("Show the Camera Screen");
        mCameraFragmentInterface = createCameraFragment();
        showCameraFragment();
        // Delay notifying the coordinator to allow the camera fragment view to be created
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                mGiniVisionCoordinator.onCameraStarted();
            }
        });
    }

    protected abstract CameraFragmentInterface createCameraFragment();

    protected abstract void showCameraFragment();

    protected abstract CameraFragmentInterface retrieveCameraFragment();

    private void configureLogging() {
        final LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        lc.reset();

        final PatternLayoutEncoder layoutEncoder = new PatternLayoutEncoder();
        layoutEncoder.setContext(lc);
        layoutEncoder.setPattern("%-5level %file:%line [%thread] - %msg%n");
        layoutEncoder.start();

        final LogcatAppender logcatAppender = new LogcatAppender();
        logcatAppender.setContext(lc);
        logcatAppender.setEncoder(layoutEncoder);
        logcatAppender.start();

        final ch.qos.logback.classic.Logger root =
                (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.addAppender(logcatAppender);
    }

    private void setupGiniVisionCoordinator(final Activity activity) {
        mGiniVisionCoordinator = GiniVisionCoordinator.createInstance(activity);
        mGiniVisionCoordinator.setListener(new GiniVisionCoordinator.Listener() {
            @Override
            public void onShowOnboarding() {
                showOnboarding();
            }
        });
    }

    private void showOnboarding() {
        LOG.debug("Show the Onboarding Screen");
        if (mMenu != null) {
            mMenu.setGroupVisible(R.id.group, false);
        }
        mCameraFragmentInterface.hideInterface();
        setTitlesForOnboarding();
        showOnboardingFragment();
    }

    protected abstract void showOnboardingFragment();

    protected abstract void setTitlesForOnboarding();

    private void startGiniVisionLibraryForImportedFile(@NonNull final Intent importedFileIntent) {
        getSingleDocumentAnalyzer().cancelAnalysis();
        if (GiniVision.hasInstance() && GiniVision.getInstance().isMultiPageEnabled()) {
            mFileImportCancellationToken = GiniVision.getInstance().createDocumentForImportedFiles(
                    importedFileIntent, mActivity,
                    new AsyncCallback<Document, ImportedFileValidationException>() {
                        @Override
                        public void onSuccess(@NonNull final Document result) {
                            if (result.isReviewable()) {
                                launchMultiPageReviewScreen();
                            } else {
                                launchAnalysisScreen(result);
                            }
                            mActivity.finish();
                        }

                        @Override
                        public void onError(
                                @NonNull final ImportedFileValidationException exception) {
                            handleFileImportError(exception);
                        }

                        @Override
                        public void onCancelled() {

                        }
                    });
        } else {
            try {
                final Document document = GiniVision.createDocumentForImportedFile(
                        importedFileIntent,
                        mActivity);
                if (document.isReviewable()) {
                    launchReviewScreen(document);
                } else {
                    launchAnalysisScreen(document);
                }
                mActivity.finish();

            } catch (final ImportedFileValidationException e) {
                e.printStackTrace();
                handleFileImportError(e);
            }
        }
    }

    private void handleFileImportError(final ImportedFileValidationException exception) {
        String message = exception.getMessage();
        if (exception.getValidationError() != null) {
            message = mActivity.getString(exception.getValidationError().getTextResource());
        }
        new AlertDialog.Builder(mActivity)
                .setMessage(message)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialogInterface, final int i) {
                        mActivity.finish();
                    }
                })
                .show();
    }

    private void launchMultiPageReviewScreen() {
        final Intent intent = MultiPageReviewExampleActivity.newInstance(mActivity);
        mActivity.startActivityForResult(intent, MULTI_PAGE_REVIEW_REQUEST);
    }

    public boolean onCreateOptionsMenu(final Menu menu) {
        mMenu = menu;
        mActivity.getMenuInflater().inflate(R.menu.menu_camera, menu);
        return true;
    }

    public void onNewIntent(final Intent intent) {
        if (isIntentActionViewOrSend(intent)) {
            startGiniVisionLibraryForImportedFile(intent);
        }
    }

    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.tips:
                showOnboarding();
                return true;
            case R.id.help:
                showHelp();
                return true;
        }
        return false;
    }

    private void showHelp() {
        final Intent intent = new Intent(mActivity, HelpActivity.class);
        mActivity.startActivity(intent);
    }

    @Override
    public void onExtractionsAvailable(
            @NonNull final Map<String, GiniVisionSpecificExtraction> extractions) {
        final Intent intent = new Intent(mActivity, ExtractionsActivity.class);
        intent.putExtra(ExtractionsActivity.EXTRA_IN_EXTRACTIONS,
                getExtractionsBundle(extractions));
        mActivity.startActivity(intent);
        mActivity.setResult(Activity.RESULT_OK);
        mActivity.finish();
    }

    @Override
    public void onProceedToMultiPageReviewScreen(
            @NonNull final GiniVisionMultiPageDocument multiPageDocument) {
        // Only compat version available (which uses the support library)
        launchMultiPageReviewScreen();
    }
}
