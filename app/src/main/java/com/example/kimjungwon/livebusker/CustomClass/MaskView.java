package com.example.kimjungwon.livebusker.CustomClass;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.kimjungwon.livebusker.R;

import me.lake.librestreaming.sample.ui.AlwaysMarqueeTextView;

/**
 * Created by kimjungwon on 2017-10-14.
 */

public class MaskView extends FrameLayout {

    private int VWidth, VHeight;

    public MaskView(Context context) {
        super(context);
    }

    public void init(int w, int h) {

    }

    private void update() {
        this.measure(VWidth, VHeight);
        this.layout(0, 0, VWidth, VHeight);
    }

    public void destroy() {
        this.removeAllViews();
    }

}
