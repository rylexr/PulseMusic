package com.hardcodecoder.pulsemusic.activities;

import android.os.Bundle;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatActivity;

import com.hardcodecoder.pulsemusic.themes.ThemeColors;
import com.hardcodecoder.pulsemusic.themes.ThemeManagerUtils;

public abstract class PMBActivity extends AppCompatActivity {

    @StyleRes
    private int mCurrentTheme;
    @ColorInt
    private int mCurrentAccent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ThemeManagerUtils.init(getApplicationContext(), false);
        setTheme(ThemeManagerUtils.getThemeToApply());
        // Initialize ThemeColors only after applying theme to the calling activity
        ThemeColors.initColors(this);
        mCurrentTheme = ThemeManagerUtils.getThemeToApply();
        mCurrentAccent = ThemeColors.getAccentColorForCurrentTheme();
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        if (mCurrentTheme != ThemeManagerUtils.getThemeToApply() ||
                mCurrentAccent != ThemeColors.getAccentColorForCurrentTheme()) {
            supportInvalidateOptionsMenu();
            recreate();
        }
        super.onStart();
    }
}