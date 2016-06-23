package net.gini.android.vision.reviewdocument;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import net.gini.android.vision.GiniVisionError;
import net.gini.android.vision.R;
import net.gini.android.vision.scanner.Document;

/**
 * <p>
 *     When using the Screen API {@code ReviewDocumentActivity} displays the photographed document and allows the user to review it by checking the sharpness, quality and orientation of the image. The user can correct the orientation by rotating the image.
 * </p>
 * <p>
 *     You must extend the {@code ReviewDocumentActivity} in your application and provide it to the {@link net.gini.android.vision.scanner.ScannerActivity} by using the {@link net.gini.android.vision.scanner.ScannerActivity#setReviewDocumentActivityExtra(Intent, Context, Class)} helper method.
 * </p>
 * <p>
 *     <b>Note:</b> {@code ReviewDocumentActivity} extends {@link AppCompatActivity} and requires an AppCompat Theme.
 * </p>
 * <p>
 *     The {@code ReviewDocumentActivity} is started by the {@link net.gini.android.vision.scanner.ScannerActivity} after the user took an image of a document.
 * </p>
 * <p>
 *     In your {@code ReviewDocumentActivity} subclass you have to implement the following methods:
 *     <ul>
 *         <li>{@link ReviewDocumentActivity#onShouldAnalyzeDocument(Document)} - you should start analysing the original document by sending it to the Gini API. We assume that in most cases the photo is good enough and this way we are able to provide analysis results quicker.<br/><b>Note:</b> Call {@link ReviewDocumentActivity#onDocumentAnalyzed()} when the analysis is done and the Activity wasn't stopped.</li>
 *         <li>{@link ReviewDocumentActivity#onAddDataToResult(Intent)} - you can add the results of the analysis to the Intent as extras and retrieve them when the {@link net.gini.android.vision.scanner.ScannerActivity} returned.<br/>This is called only, if you called {@link ReviewDocumentActivity#onDocumentAnalyzed()} and the image wasn't changed before the user tapped on the Next button.<br/>When this is called, your {@link net.gini.android.vision.analyse.AnalyseDocumentActivity} subclass is not launched, instead control is returned to your Activity which started the {@link net.gini.android.vision.scanner.ScannerActivity} and you can extract the results of the analysis.</li>
 *     </ul>
 * </p>
 */
public abstract class ReviewDocumentActivity extends AppCompatActivity implements ReviewDocumentFragmentListener, ReviewDocumentFragmentInterface {

    /**
     * @exclude
     */
    public static final String EXTRA_IN_DOCUMENT = "GV_EXTRA_IN_DOCUMENT";
    /**
     * @exclude
     */
    public static final String EXTRA_OUT_DOCUMENT = "GV_EXTRA_OUT_DOCUMENT";
    /**
     * @exclude
     */
    public static final String EXTRA_OUT_ERROR = "GV_EXTRA_OUT_ERROR";

    /**
     * @exclude
     */
    public static final int RESULT_PHOTO_WAS_REVIEWED = RESULT_FIRST_USER + 1;
    /**
     * @exclude
     */
    public static final int RESULT_PHOTO_WAS_REVIEWED_AND_ANALYZED = RESULT_FIRST_USER + 2;
    /**
     * @exclude
     */
    public static final int RESULT_ERROR = RESULT_FIRST_USER + 3;

    private ReviewDocumentFragmentCompat mFragment;
    private Document mDocument;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gv_activity_review_document);
        if (savedInstanceState == null) {
            readExtras();
            createFragment();
            showFragment();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearMemory();
    }

    private void clearMemory() {
        mDocument = null;
    }

    private void readExtras() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mDocument = extras.getParcelable(EXTRA_IN_DOCUMENT);
        }
        checkRequiredExtras();
    }

    private void checkRequiredExtras() {
        if (mDocument == null) {
            throw new IllegalStateException("ReviewDocumentActivity requires a Document. Set it as an extra using the EXTRA_IN_DOCUMENT key.");
        }
    }

    private void createFragment() {
        mFragment = ReviewDocumentFragmentCompat.createInstance(mDocument);
    }

    private void showFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.gv_fragment_review_document, mFragment)
                .commit();
    }

    // callback for subclasses for uploading the photo before it was reviewed, if the photo is not changed
    // no new upload is required
    @Override
    public abstract void onShouldAnalyzeDocument(Document document);

    @Override
    public void onProceedToAnalyzeScreen(Document document) {
        Intent result = new Intent();
        result.putExtra(EXTRA_OUT_DOCUMENT, document);
        onAddDataToResult(result);
        setResult(RESULT_PHOTO_WAS_REVIEWED, result);
        finish();
    }

    @Override
    public void onDocumentReviewedAndAnalyzed(Document document) {
        Intent result = new Intent();
        result.putExtra(EXTRA_OUT_DOCUMENT, document);
        onAddDataToResult(result);
        setResult(RESULT_PHOTO_WAS_REVIEWED_AND_ANALYZED, result);
        finish();
    }

    // Photo was already analyzed, add the extractions to the result
    public abstract void onAddDataToResult(Intent result);

    // TODO: call this, if the photo was analyzed before the review was completed, it prevents the analyze activity to
    // be started, if the photo was already analyzed and the user didn't change it
    @Override
    public void onDocumentAnalyzed() {
        mFragment.onDocumentAnalyzed();
    }

    @Override
    public void onError(GiniVisionError error) {
        Intent result = new Intent();
        result.putExtra(EXTRA_OUT_ERROR, error);
        setResult(RESULT_ERROR, result);
        finish();
    }
}
