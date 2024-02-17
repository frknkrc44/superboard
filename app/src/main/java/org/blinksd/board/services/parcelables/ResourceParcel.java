package org.blinksd.board.services.parcelables;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

public final class ResourceParcel implements Parcelable {
    public static final Parcelable.Creator<ResourceParcel> CREATOR
            = new Parcelable.Creator<ResourceParcel>() {
        public ResourceParcel createFromParcel(Parcel in) {
            return new ResourceParcel(in);
        }

        public ResourceParcel[] newArray(int size) {
            return new ResourceParcel[size];
        }
    };
    public final String mResName;
    public final Bitmap mBitmap;

    ResourceParcel(Parcel in) {
        mResName = in.readString();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mBitmap = in.readParcelable(getClass().getClassLoader(), Bitmap.class);
        } else {
            mBitmap = in.readParcelable(getClass().getClassLoader());
        }
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mResName);
        dest.writeParcelable(mBitmap, flags);
    }
}
