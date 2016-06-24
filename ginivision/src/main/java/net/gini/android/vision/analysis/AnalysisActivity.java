package net.gini.android.vision.analysis;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import net.gini.android.vision.GiniVisionError;
import net.gini.android.vision.R;
import net.gini.android.vision.scanner.Document;

/**
 * <p>
 *     When using the Screen API {@code AnalysisActivity} displays the captured document and an activity indicator while the document is being analyzed by the Gini API.
 * </p>
 * <p>
 *     You must extend the {@code AnalysisActivity} in your application and provide it to the {@link net.gini.android.vision.scanner.ScannerActivity} by using the {@link net.gini.android.vision.scanner.ScannerActivity#setAnalysisActivityExtra(Intent, Context, Class)} helper method.
 * </p>
 * <p>
 *     <b>Note:</b> {@code AnalysisActivity} extends {@link AppCompatActivity} and requires an AppCompat Theme.
 * </p>
 * <p>
 *     The {@code AnalysisActivity} is started by the {@link net.gini.android.vision.scanner.ScannerActivity} after the user reviewed the document and either didn't change the document and it wasn't analyzed before tapping on the Next button or the user modified the document.
 * </p>
 * <p>
 *     In your {@code AnalysisActivity} subclass you have to implement the following methods:
 *     <ul>
 *         <li>
 *          {@link AnalysisActivity#onAnalyzeDocument(Document)} - start analysing the document by sending it to the Gini API.<br/><b>Note:</b> Call {@link AnalysisActivity#onDocumentAnalyzed()} when the analysis is done and the Activity wasn't stopped.
 *         </li>
 *         <li>{@link AnalysisActivity#onAddDataToResult(Intent)} - you should add the results of the analysis to the Intent as extras and retrieve them when the {@link net.gini.android.vision.scanner.ScannerActivity} returned.<br/>This is called only, if you called {@link AnalysisActivity#onDocumentAnalyzed()} before.<br/>When this is called control is returned to your Activity which started the {@link net.gini.android.vision.scanner.ScannerActivity} and you can extract the results of the analysis.</li>
 *     </ul>
 * </p>
 *
 * <h3>Customising the Analysis Screen</h3>
 *
 * <p>
 *   Customising the look of the Analysis Screen is done via app resources overriding.
 * </p>
 * <p>
 *     The following items are customizable:
 *     <ul>
 *         <li>
 *             <b>Activity indicator color:</b> with the color resource name {@code gv_analyze_document_activity_indicator}
 *         </li>
 *     </ul>
 * </p>
 */
public abstract class AnalysisActivity extends AppCompatActivity implements AnalysisFragmentListener, AnalysisFragmentInterface {

    /**
     * @exclude
     */
    public static final String EXTRA_IN_DOCUMENT = "GV_EXTRA_IN_DOCUMENT";
    /**
     * @exclude
     */
    public static final String EXTRA_OUT_ERROR = "GV_EXTRA_OUT_ERROR";

    /**
     * @exclude
     */
    public static final int RESULT_ERROR = RESULT_FIRST_USER + 1;

    private AnalysisFragmentCompat mFragment;
    private Document mDocument;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gv_activity_analysis);
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
            throw new IllegalStateException("AnalysisActivity requires a Document. Set it as an extra using the EXTRA_IN_DOCUMENT key.");
        }
    }

    private void createFragment() {
        mFragment = AnalysisFragmentCompat.createInstance(mDocument);
    }

    private void showFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.gv_fragment_analyze_document, mFragment)
                .commit();
    }

    @Override
    public abstract void onAnalyzeDocument(Document document);

    @Override
    public void onError(GiniVisionError error) {
        Intent result = new Intent();
        result.putExtra(EXTRA_OUT_ERROR, error);
        setResult(RESULT_ERROR, result);
        finish();
    }

    @Override
    public void startScanAnimation() {
        mFragment.startScanAnimation();
    }

    @Override
    public void stopScanAnimation() {
        mFragment.stopScanAnimation();
    }

    @Override
    public void onDocumentAnalyzed() {
        mFragment.onDocumentAnalyzed();
        Intent result = new Intent();
        onAddDataToResult(result);
        setResult(RESULT_OK, result);
        finish();
    }

    /**
     * <p>
     *     Callback for adding your own data to the Activity's result.
     * </p>
     * <p>
     *     Called when the document was analyzed.
     * </p>
     * <p>
     *     You should add the results of the analysis as extras and retrieve them when the {@link net.gini.android.vision.scanner.ScannerActivity} returned.
     * </p>
     * <p>
     *     <b>Note:</b> you must call {@link AnalysisActivity#onDocumentAnalyzed()} after you received the analysis results from the Gini API, otherwise this method won't be invoked.
     * </p>
     * @param result the {@link Intent} which will be returned as the result data.
     */
    public abstract void onAddDataToResult(Intent result);
}
