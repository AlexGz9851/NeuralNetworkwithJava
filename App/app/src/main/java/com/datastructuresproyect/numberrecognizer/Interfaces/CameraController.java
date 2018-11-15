package com.datastructuresproyect.numberrecognizer.Interfaces;

import java.io.Serializable;

public interface CameraController extends Serializable {
    void lock();
    void unlock();
    void createPreviewSession();
    void setUpCamera();
    void openCamera();
    void closeCamera();
    void openBackgroundThread();
    void closeBackgroundThread();
}
