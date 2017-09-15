package net.gini.android.vision.document;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;

import net.gini.android.vision.Document;

import java.io.IOException;

/**
 * @exclude
 */
public final class DocumentFactory {

    /**
     * @param intent  an {@link Intent} containing an image or a pdf {@link Uri}
     * @param context Android context
     * @return new {@link Document} instance with the contents of the Intent's Uri
     * @throws IOException              if there is an issue with the input stream from the Uri
     * @throws IllegalArgumentException if the Intent's data is null or the mime type is unknown
     * @throws IllegalStateException    if the mime type couldn't be fetched or the Uri couldn't be
     *                                  read from
     */
    @NonNull
    public static Document documentFromIntent(@NonNull final Intent intent,
            @NonNull final Context context) throws IOException {
        final Uri data = intent.getData();
        if (data == null) {
            throw new IllegalArgumentException("Intent data must contain a Uri");
        }
        final String mimeType = context.getContentResolver().getType(data);
        if (mimeType == null) {
            throw new IllegalStateException("Could not get the mime type of the Intent's Uri");
        }
        if (mimeType.equals("application/pdf")) {
            return PdfDocument.fromIntent(intent, context);
        }
        if (mimeType.startsWith("image/")) {
            return ImageDocument.fromIntent(intent, context);
        }
        throw new IllegalArgumentException("Unknown Intent Uri mime type.");
    }
}
