package net.gini.android.vision.internal.document;

import android.support.annotation.NonNull;

import net.gini.android.vision.Document;
import net.gini.android.vision.document.ImageDocument;
import net.gini.android.vision.document.PdfDocument;

/**
 * @exclude
 */

/**
 * @exclude
 */
public final class DocumentRendererFactory {

    public static DocumentRenderer fromDocument(@NonNull final Document document) {
        switch (document.getType()) {
            case IMAGE:
                return new ImageDocumentRenderer((ImageDocument) document);
            case PDF:
                return new PdfDocumentRenderer((PdfDocument) document);
            default:
                throw new IllegalArgumentException("Unknown document type");
        }
    }

    private DocumentRendererFactory() {
    }
}
