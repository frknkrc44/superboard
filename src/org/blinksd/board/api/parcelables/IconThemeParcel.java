package org.blinksd.board.api.parcelables;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

public class IconThemeParcel implements Parcelable {
    public final String mThemeName;
    public final ResourceParcel mShiftImage;
    public final ResourceParcel mEmojiImage;
    public final ResourceParcel mSpaceImage;
    public final ResourceParcel mReturnImage;
    public final ResourceParcel mDeleteImage;

    public static final Parcelable.Creator<IconThemeParcel> CREATOR
            = new Parcelable.Creator<IconThemeParcel>() {
        public IconThemeParcel createFromParcel(Parcel in) {
            return new IconThemeParcel(in);
        }

        public IconThemeParcel[] newArray(int size) {
            return new IconThemeParcel[size];
        }
    };

    private IconThemeParcel(Parcel in) {
        mThemeName = in.readString();
        mShiftImage = readRes(in);
        mEmojiImage = readRes(in);
        mSpaceImage = readRes(in);
        mReturnImage = readRes(in);
        mDeleteImage = readRes(in);
    }

    private ResourceParcel readRes(Parcel in) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return in.readParcelable(getClass().getClassLoader(), ResourceParcel.class);
        }
        return in.readParcelable(getClass().getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mThemeName);
        dest.writeParcelable(mShiftImage, flags);
        dest.writeParcelable(mEmojiImage, flags);
        dest.writeParcelable(mSpaceImage, flags);
        dest.writeParcelable(mReturnImage, flags);
        dest.writeParcelable(mDeleteImage, flags);
    }
}
