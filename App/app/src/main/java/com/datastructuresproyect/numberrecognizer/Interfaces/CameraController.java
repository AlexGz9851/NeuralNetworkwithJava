// Luis Iván Morett Arévalo		   A01634417
// Jesús Alejandro González Sánchez A00820225
// CameraController
// Profesor: Gerardo Salinas

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
