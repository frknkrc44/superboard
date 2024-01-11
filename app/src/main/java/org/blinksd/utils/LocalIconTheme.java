package org.blinksd.utils;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;

import org.blinksd.board.SuperBoardApplication;
import org.blinksd.board.services.parcelables.IconThemeParcel;

@SuppressWarnings("deprecation")
public class LocalIconTheme {
    public static final int SYM_TYPE_SHIFT = 0, SYM_TYPE_EMOJI = 1,
            SYM_TYPE_SPACE = 2, SYM_TYPE_ENTER = 3,
            SYM_TYPE_DELETE = 4;
    public final Drawable shiftIcon;
    public final Drawable emojiIcon;
    public final Drawable spaceIcon;
    public final Drawable returnIcon;
    public final Drawable deleteIcon;

    public LocalIconTheme(Drawable shiftIcon, Drawable emojiIcon, Drawable spaceIcon,
                          Drawable returnIcon, Drawable deleteIcon) {
        this.shiftIcon = shiftIcon;
        this.emojiIcon = emojiIcon;
        this.spaceIcon = spaceIcon;
        this.returnIcon = returnIcon;
        this.deleteIcon = deleteIcon;
    }

    public LocalIconTheme(Bitmap shiftIcon, Bitmap emojiIcon, Bitmap spaceIcon,
                          Bitmap returnIcon, Bitmap deleteIcon) {
        this(
                new BitmapDrawable(shiftIcon),
                new BitmapDrawable(emojiIcon),
                new BitmapDrawable(spaceIcon),
                new BitmapDrawable(returnIcon),
                new BitmapDrawable(deleteIcon)
        );
    }

    public LocalIconTheme(IconThemeParcel iconThemeParcel) {
        this(
                iconThemeParcel.mShiftImage.mBitmap,
                iconThemeParcel.mEmojiImage.mBitmap,
                iconThemeParcel.mSpaceImage.mBitmap,
                iconThemeParcel.mReturnImage.mBitmap,
                iconThemeParcel.mDeleteImage.mBitmap
        );
    }

    public LocalIconTheme(int[] map) {
        this(
                getDrawable(map[0]),
                getDrawable(map[1]),
                getDrawable(map[2]),
                getDrawable(map[3]),
                getDrawable(map[4])
        );
    }

    private static Drawable getDrawable(int res) {
        switch (res) {
            case SpaceBarThemeUtils.SPACEBAR_DEFAULT:
            case SpaceBarThemeUtils.SPACEBAR_TEXT:
                return null;
            case SpaceBarThemeUtils.SPACEBAR_HIDE:
                return new ColorDrawable();
            default:
                break;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return SuperBoardApplication.getApplication().getDrawable(res);
        }

        return SuperBoardApplication.getApplication().getResources().getDrawable(res);
    }

    public Drawable getIconByType(int type) {
        switch (type) {
            case SYM_TYPE_SHIFT:
                return shiftIcon;
            case SYM_TYPE_EMOJI:
                return emojiIcon;
            case SYM_TYPE_SPACE:
                return spaceIcon;
            case SYM_TYPE_ENTER:
                return returnIcon;
            case SYM_TYPE_DELETE:
                return deleteIcon;
        }

        return new ColorDrawable();
    }
}
