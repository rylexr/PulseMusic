package com.hardcodecoder.pulsemusic.themes;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hardcodecoder.pulsemusic.R;

import java.lang.reflect.Field;

public class TintHelper {

    public static void setAccentTintTo(ImageView imageView) {
        imageView.setImageTintList(ColorStateList.valueOf(ThemeColors.getAccentColorForCurrentTheme()));
    }

    public static Drawable setAccentTintTo(Drawable drawable, boolean mutate) {
        if (mutate) drawable.mutate();
        drawable.setTint(ThemeColors.getAccentColorForCurrentTheme());
        return drawable;
    }

    public static void setAccentTintTo(FloatingActionButton fab) {
        fab.setBackgroundTintList(ThemeColors.getAccentColorStateList());
    }

    public static void setAccentTintToMaterialButton(MaterialButton materialButton) {
        materialButton.setBackgroundTintList(ColorStateList.valueOf(ThemeColors.getAccentColorForCurrentTheme()));
    }

    public static void setAccentTintToMaterialOutlineButton(MaterialButton materialButton) {
        ColorStateList stateList = ColorStateList.valueOf(ThemeColors.getAccentColorForCurrentTheme());
        materialButton.setRippleColor(ColorStateList.valueOf(
                ColorUtil.changeAlphaComponentTo(ThemeColors.getCurrentAccentColor(), 0.26f)));
        materialButton.setTextColor(stateList);
        materialButton.setIconTint(stateList);
        materialButton.setStrokeColor(stateList);
    }

    public static void setAccentTintToCursor(EditText editText) {
        if (Build.VERSION.SDK_INT == 28) {
            // Cannot be applied using reflection
            // mDrawableForCursor in Editor class set's the drawable for cursor
            // but is blacklisted can cannot be accessed using reflection
            // back luck for api = 28 :(
            return;
        }

        int color = ThemeColors.getAccentColorForCurrentTheme();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Use TextView#setTextCursorDrawable to set custom tinted drawable
            Drawable drawable = ContextCompat.getDrawable(editText.getContext(), R.drawable.edit_text_cursor);
            if (null != drawable)
                drawable.setTint(color);
            editText.setTextCursorDrawable(drawable);
        } else {
            // Use reflection to set for devices api < 28
            try {
                // Get the cursor resource id
                /*Field field = TextView.class.getDeclaredField("mCursorDrawableRes");
                field.setAccessible(true);
                int drawableResId = field.getInt(editText);*/

                // Get the editor
                Field field = TextView.class.getDeclaredField("mEditor");
                field.setAccessible(true);
                Object editor = field.get(editText);

                // Get the drawable and set a color filter
                //Drawable drawable = ContextCompat.getDrawable(editText.getContext(), drawableResId);
                Drawable drawable = ContextCompat.getDrawable(editText.getContext(), R.drawable.edit_text_cursor);
                assert drawable != null;
                //drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
                drawable.setTint(color);

                // Set the drawables
                /*if (Build.VERSION.SDK_INT == 28) {//set differently in Android P (API 28)
                    assert editor != null;
                    field = editor.getClass().getDeclaredField("mDrawableForCursor");
                    field.setAccessible(true);
                    field.set(editor, drawable);
                } else {*/
                Drawable[] drawables = {drawable, drawable};
                assert editor != null;
                field = editor.getClass().getDeclaredField("mCursorDrawable");
                field.setAccessible(true);
                field.set(editor, drawables);
                //}

                // set the "selection handle" color too
                // colorHandles(editText); // Result is not satisfactory
            } catch (Exception exception) {
                Log.w("TintHelper", "Failed to change edit text cursor drawable tint");
            }
        }
    }

    /*
     * Set the color of the handles when you select text in a
     * {@link android.widget.EditText} or other view that extends {@link TextView}.
     *
     * @param view  The {@link TextView} or a {@link android.view.View} that extends {@link TextView}.
     * @see <a href="https://gist.github.com/jaredrummler/2317620559d10ac39b8218a1152ec9d4">External reference</a>

    public static void colorHandles(TextView view) {
        int color = ThemeColors.getCurrentAccentColor();
        try {
            Field editorField = TextView.class.getDeclaredField("mEditor");
            if (!editorField.isAccessible()) {
                editorField.setAccessible(true);
            }

            Object editor = editorField.get(view);
            assert editor != null;
            Class<?> editorClass = editor.getClass();

            String[] handleNames = {"mSelectHandleLeft", "mSelectHandleRight", "mSelectHandleCenter"};
            String[] resNames = {"mTextSelectHandleLeftRes", "mTextSelectHandleRightRes", "mTextSelectHandleRes"};

            for (int i = 0; i < handleNames.length; i++) {
                Field handleField = editorClass.getDeclaredField(handleNames[i]);
                if (!handleField.isAccessible())
                    handleField.setAccessible(true);

                Drawable handleDrawable = (Drawable) handleField.get(editor);

                if (handleDrawable == null) {
                    Field resField = TextView.class.getDeclaredField(resNames[i]);
                    if (!resField.isAccessible()) {
                        resField.setAccessible(true);
                    }
                    int resId = resField.getInt(view);
                    handleDrawable = ResourcesCompat.getDrawable(view.getResources(), resId, view.getContext().getTheme());
                }

                if (handleDrawable != null) {
                    //Drawable drawable = handleDrawable.mutate();
                    //drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
                    handleDrawable.setTint(color);
                    handleField.set(editor, handleDrawable);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
}