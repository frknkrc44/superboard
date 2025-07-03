package org.blinksd.board.views.emoji;

import static org.blinksd.utils.DensityUtils.maxP;
import static org.blinksd.utils.ResourcesUtils.getTransSelectableItemBg;
import static org.blinksd.utils.SuperDBHelper.getIntOrDefault;
import static org.blinksd.utils.SystemUtils.getMultipliedTextSize;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.blinksd.utils.SettingMap;

import thirdparty.two_way_grid_view.TwoWayGridView;

// noinspection ViewConstructor
public class EmojiCategoryViewV2 extends TwoWayGridView {
    private final EmojiList emojiList;
    private final OnEmojiClickListener listener;

    // TODO: Save the selected skin tone
    public EmojiCategoryViewV2(Context context, EmojiList emojiList, OnEmojiClickListener listener) {
        super(context);
        this.emojiList = emojiList;
        this.listener = listener;
        setNumColumns(8);
        setNumRows(4);

        // TODO: Prepare the skin tone selector (similar to BoardPopup)
        /*
        setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(TwoWayAdapterView<?> parent, View view, int position, long id) {
                final var emoji = (Emoji) parent.getAdapter().getItem(position);
                return !emoji.skinTones.isEmpty();
            }
        });
         */

        setAdapter(new EmojiListAdapter());
    }

    public interface OnEmojiClickListener {
        void onEmojiClick(String emoji);
    }

    private class EmojiListAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return emojiList.size();
        }

        @Override
        public Emoji getItem(int position) {
            return emojiList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final var keyTextColor = getIntOrDefault(SettingMap.SET_KEY_TEXTCLR);
            final var emoji = getItem(position).emoji;
            final var itemView = new TextView(parent.getContext()) {
                @Override
                protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                    final var max = Math.max(widthMeasureSpec, heightMeasureSpec);
                    super.onMeasure(max, max);
                }
            };
            itemView.setText(emoji);
            itemView.setGravity(Gravity.CENTER);
            itemView.setBackground(getTransSelectableItemBg(getContext(), keyTextColor));
            itemView.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    getMultipliedTextSize(
                            maxP(getIntOrDefault(SettingMap.SET_KEYBOARD_HEIGHT) / (getNumRows() * 3f))));
            itemView.setTextColor(keyTextColor);
            itemView.setOnClickListener(v -> listener.onEmojiClick(emoji));
            return itemView;
        }
    }
}
