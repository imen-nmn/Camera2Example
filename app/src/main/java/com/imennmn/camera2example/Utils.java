package com.imennmn.camera2example;

import android.content.Context;
import android.content.res.Resources;
import android.os.Environment;
import android.util.Log;
import android.util.Size;
import android.view.TextureView;
import android.widget.RelativeLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by imen_nmn on 16/11/17.
 */

public class Utils {

    public static Boolean existsSdcard() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public static File getCacheFile(String dirName, Context context) {
        File result;
        if (existsSdcard()) {
            File cacheDir = context.getExternalCacheDir();
            if (cacheDir == null) {
                result = new File(Environment.getExternalStorageDirectory(),
                        "Android/data/" + context.getPackageName() + "/cache/" + dirName);
            } else {
                result = new File(cacheDir, dirName);
            }
        } else {
            result = new File(context.getCacheDir(), dirName);
        }

        if (result.exists() || result.mkdirs()) {
            return result;
        } else {
            return null;
        }
    }

    public static Size nearToRatio(final double ratio, List<Size> lista){

         List<Size> temp = new ArrayList<>() ;
         temp.addAll(lista) ;
        Collections.sort(lista, new Comparator<Size>() {
            @Override
            public int compare(Size left, Size right) {
                double ratioLeft = (double)left.getWidth()/left.getHeight() ;
                double ratioRight = (double)right.getWidth()/right.getHeight() ;

                return (int)( ratioLeft - ratioRight) ;
            }
        });



        Size size = getSizes(temp," Preview creen ratio "+ratio, ratio) ;
        Log.e("UtilsTag", "nearest size is  "+size) ;

//        int currentRatio = lista.get(0).getWidth()/ lista.get(0).getHeight() ;
//
//        if(currentRatio== )

        return size ;

    }


    public static  Size getSizes(List<Size> lista , String  nme, double ratioPerrefred){
        Log.e("UtilsTag", "******************************** "+nme) ;
        double minDiff = Double.MAX_VALUE ;
        Size nearestSize = null;
        for (int i = 0 ; i< lista.size() ; i++){
            double ratio = (double)lista.get(i).getWidth()/lista.get(i).getHeight() ;
            double diffRatio = Math.abs(ratioPerrefred-ratio) ;
            Log.i("UtilsTag", " "+lista.get(i).getWidth()+" x "+lista.get(i).getHeight()+" ratio = "+diffRatio) ;
            if(diffRatio == ratioPerrefred){
                return lista.get(i) ;
            } else if(diffRatio < minDiff){
                minDiff = diffRatio ;
                nearestSize = lista.get(i) ;
            }
        }

        return nearestSize ;
    }

    public static boolean adjustVideoSurface(int currentWidth,
                                             int currentHeight,
                                             int width,
                                             int height,
                                             TextureView surfaceView) {


        float ratioW = (float) currentWidth / width;
        float ratioH = (float) currentHeight / height;
        RelativeLayout.LayoutParams layoutParams;

        int displayedHeight = (int) (ratioW * height);

        layoutParams = new RelativeLayout.LayoutParams(currentWidth, displayedHeight);


        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        surfaceView.setLayoutParams(layoutParams);
        surfaceView.requestLayout();


        return Math.round(ratioH - ratioW) == 0;
    }

    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

}
