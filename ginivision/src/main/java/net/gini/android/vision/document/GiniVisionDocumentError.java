package net.gini.android.vision.document;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 * Created by Alpar Szotyori on 15.03.2018.
 *
 * Copyright (c) 2018 Gini GmbH.
 */

/**
 * @exclude
 */
public class GiniVisionDocumentError implements Parcelable {

    public static final Creator<GiniVisionDocumentError> CREATOR =
            new Creator<GiniVisionDocumentError>() {
                @Override
                public GiniVisionDocumentError createFromParcel(final Parcel in) {
                    return new GiniVisionDocumentError(in);
                }

                @Override
                public GiniVisionDocumentError[] newArray(final int size) {
                    return new GiniVisionDocumentError[size];
                }
            };
    private final String mMessage;
    private final ErrorCode mErrorCode;


    public GiniVisionDocumentError(@NonNull final String message,
            @NonNull final ErrorCode errorCode) {
        mMessage = message;
        mErrorCode = errorCode;
    }

    GiniVisionDocumentError(final Parcel in) {
        mMessage = in.readString();
        mErrorCode = (ErrorCode) in.readSerializable();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull final Parcel dest, final int flags) {
        dest.writeString(mMessage);
        dest.writeSerializable(mErrorCode);
    }

    @NonNull
    public String getMessage() {
        return mMessage;
    }

    @NonNull
    public ErrorCode getErrorCode() {
        return mErrorCode;
    }

    /**
     * @exclude
     */
    public enum ErrorCode {
        UPLOAD_FAILED,
        FILE_VALIDATION_FAILED
    }
}
