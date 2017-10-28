package com.example.kimjungwon.livebusker.CustomClass;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.Log;
import android.util.SparseArray;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.kimjungwon.livebusker.R;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;

import java.util.List;
import java.util.Objects;


/**
 * Created by kimjungwon on 2017-10-14.
 */

public class BunnyMask extends RelativeLayout {

    private static final String TAG = BunnyMask.class.getSimpleName();
    ImageView EarsView, NoseView;

    Rect EarRect, NoseRect, FaceRect;

    private int VWidth, VHeight;

    public Bitmap faceBitmap;

    private Boolean detected = false;

    protected final Object syncBitmap = new Object();

    public BunnyMask(Context context, Bitmap FaceBitmap) {
        super(context);
        this.faceBitmap = FaceBitmap;
    }

    public Bitmap resizedBitmap(Bitmap bm, int newWidth, int newHeight){
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }

    public void DetectPositions(Bitmap facebp){
        FaceDetector faceDetector =
                new FaceDetector.Builder(this.getContext())
                        .setProminentFaceOnly(true)
                        .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                        .setTrackingEnabled(true)
                        .setMode(FaceDetector.FAST_MODE)
                        .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                        .build();

        float resizeRatioWidth = 200 / ((float)facebp.getWidth());
        float resizeRatioHeight = 200 / ((float)facebp.getHeight());

        Bitmap resizeFace = resizedBitmap(facebp, 200, 200);

        Frame frame = new Frame.Builder().setBitmap(resizeFace).build();
        SparseArray<Face> faces = faceDetector.detect(frame);

//        for(int i = 0 ; i < faces.size() ; i++){
        //얼굴 인식을 했을 경우
        if(faces.size() != 0){
            detected = true;
            Face thisFace = faces.valueAt(0);
            float x1 = thisFace.getPosition().x;
            float y1 = thisFace.getPosition().y;
            float x2 = x1 + thisFace.getWidth();
            float y2 = y1 + thisFace.getHeight();

            FaceRect = new Rect();
            FaceRect.left = (int) x1;
            FaceRect.top = (int) y1;
            FaceRect.right = (int) x2;
            FaceRect.bottom = (int) y2;

            Log.d(TAG, "<face detect> \nx1: " + x1 + " y1: " + y1 + " x2: " + x2 + " y2: " + y2);


            List<Landmark> landmarks = thisFace.getLandmarks();
            for(int j = 0 ; j < landmarks.size() ; j ++){
                Landmark landmark = landmarks.get(j);
                PointF pos = landmark.getPosition();
                int posX = (int) pos.x;
                int posY = (int) pos.y;

                if(landmark.getType() == Landmark.NOSE_BASE){
//                    폭이 얼굴의 1/5이고 높이가 얼굴의 1/3인 이미지 만들기

                    int Left = (int) (posX - thisFace.getWidth() / (5 * 2) );
                    int Top = (int) (posY - thisFace.getHeight() / (3 * 2) );
                    int Right = (int) (Left + thisFace.getWidth() / 5);
                    int Bottom = (int) (Top + thisFace.getHeight() / 3);

                    NoseRect = new Rect((int) (Left / (resizeRatioHeight * 1.5)),
                            (int) (Top / (resizeRatioHeight * 1.5)),
                            (int) (Right / (resizeRatioWidth * 1.5)),
                            (int) (Bottom / (resizeRatioHeight * 1.5)));

                }
                Log.d(TAG,"landmark (" + j + ") \nx: " + pos.x + " y: " + pos.y);
            }
        }else{
            detected = false;
        }
    }

    public void init(int w, int h) {
        VWidth = w;
        VHeight = h;

        DetectPositions(this.faceBitmap);


//        인식했을 경우
        if(detected){
            //        코
            NoseView = new ImageView(this.getContext());

            this.setBackgroundColor(Color.argb(0, 0, 0, 0));
            int NoseWidth = (int) ((VWidth - FaceRect.left - FaceRect.right) / 5);
            int NoseHeight = (int) ((VHeight - FaceRect.top - FaceRect.bottom) / 3);

            RelativeLayout.LayoutParams Noselp = new RelativeLayout.LayoutParams(NoseWidth, NoseHeight);
            Noselp.setMargins(NoseRect.left, NoseRect.top, NoseRect.right, NoseRect.bottom);
            NoseView.setImageResource(R.drawable.bunny_nose);
            this.addView(NoseView, Noselp);
        }


//        귀
//        EarsView = new ImageView(this.getContext());
//
//        this.setBackgroundColor(Color.argb(0, 0, 0, 0));
//        int EarsWidth = (int) ((VWidth - FaceRect.left - FaceRect.right) /)
        update();
    }

    public void update(){
        this.measure(VWidth,VHeight);
        this.layout(0,0,VWidth,VHeight);
    }


    public void updateMask(Bitmap faceBitmap) {
        synchronized (faceBitmap){
            if(faceBitmap != null)
                DetectPositions(faceBitmap);
        }
    }

    public void destroy() {
        this.removeAllViews();
    }
}
