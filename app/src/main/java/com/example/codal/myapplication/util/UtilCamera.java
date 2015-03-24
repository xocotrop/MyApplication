package com.example.codal.myapplication.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.List;

/**
 * Created by Codal on 16/03/15.
 */
public class UtilCamera {

    public static final int CAMERA_BACK = 0;
    public static final int CAMERA_FRONT = 1;
    public static int posicaoAtualCamera = CAMERA_BACK;

    public static Camera initCamera(Camera camera, SurfaceHolder surfaceHolder){
        if(camera != null){
            try {

                camera.setPreviewDisplay(surfaceHolder);


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return camera;
    }


    public static Camera switchCam(Camera camera, Activity activity){
        if(camera != null){
            camera = destroy(camera);
            camera = getCameraInstance(camera, posicaoAtualCamera == CAMERA_BACK ? CAMERA_FRONT : CAMERA_BACK, activity);

        }
        return camera;
    }

    public static Camera destroy(Camera camera){
        if(camera != null){

            try {
//                camera.unlock();
//                camera.stopPreview();
//                camera.setPreviewDisplay(null);

                camera.release();
                camera = null;
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return camera;
    }

    public static int quantidadeCameras(){
        return Camera.getNumberOfCameras();
    }

    public static int getBackCamera(){
        int qtd =  Camera.getNumberOfCameras();
        for(int i = 0; i < qtd; i++){
            Camera.CameraInfo c = new Camera.CameraInfo();
            Camera.getCameraInfo(i, c);
            if(c.facing == Camera.CameraInfo.CAMERA_FACING_BACK){
                return i;
            }
        }
        return -1;
    }

    public static int getFrontCamera(){
        int qtd =  Camera.getNumberOfCameras();
        for(int i = 0; i < qtd; i++){
            Camera.CameraInfo c = new Camera.CameraInfo();
            Camera.getCameraInfo(i, c);
            if(c.facing == Camera.CameraInfo.CAMERA_FACING_FRONT){
                return i;
            }
        }
        return -1;
    }

    public static Camera getCameraInstance(Camera camera, int qualCamera, Activity activity){
        if(camera == null){
            int idCamera = -1;
            if(qualCamera == CAMERA_BACK){

                idCamera = getBackCamera();
            } else {
                idCamera = getFrontCamera();
            }
            posicaoAtualCamera = qualCamera;
            Log.d("Camera >>", idCamera + "");
            if(idCamera > -1) {
                camera = Camera.open(idCamera);
                int orientacao = getCameraDisplayOrientation(activity, idCamera);
                camera.setDisplayOrientation(orientacao);


            }
        }
        return camera;
    }

    public static Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.05;
        double targetRatio = (double) w/h;

        if (sizes==null) return null;

        Camera.Size optimalSize = null;

        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Find size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    public static int getCameraDisplayOrientation(Activity activity,
                                                  int qualCamera) {

        int cameraId = -1;
        if(qualCamera == CAMERA_BACK){
            cameraId = getBackCamera();
        } else {
            cameraId = getFrontCamera();
        }

        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        //camera.setDisplayOrientation(result);
        return result;
    }

    public static boolean isDeviceSupportCamera(Context context) {
        if (context.getApplicationContext().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

}
