// Luis Iván Morett Arévalo		   A01634417
// Jesús Alejandro González Sánchez A00820225
// CameraActivity
// Profesor: Gerardo Salinas

package com.datastructuresproyect.numberrecognizer;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;

import com.datastructuresproyect.numberrecognizer.Utils.BitmapManager;
import com.datastructuresproyect.numberrecognizer.Utils.Camera;

public class CameraActivity extends AppCompatActivity {
    private static final String TAG = "CameraActivity";

    private int dpBoxSize;


    private Camera camera;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);


        camera = new Camera(this, (TextureView) findViewById(R.id.camera_preview));

        dpBoxSize =  (int)getResources().getDimension(R.dimen.box_size) - 200;


        ((Button)findViewById(R.id.capture_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera.lock();
                try{
                    int move = dpBoxSize/2;
                    BitmapManager bmpManager = new BitmapManager(camera.getBitmap());
                    int width = bmpManager.getWidth();
                    int height = bmpManager.getHeight();
                    bmpManager.setBitmap(bmpManager.crop(
                            width/2-move,
                            height/2-move,
                            dpBoxSize,
                            dpBoxSize
                    ));
                    bmpManager.setBitmap(bmpManager.scale(28,28));
                    bmpManager.setBitmap(bmpManager.toGrayScale());

                    FragmentManager fm = getSupportFragmentManager();

                    ImageFragment imageFragment = ImageFragment.newInstance(bmpManager, camera);

                    imageFragment.show(fm, "fragment");


                }catch(Exception e){
                    e.printStackTrace();
                    camera.unlock();
                }

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        camera.openBackgroundThread();
        if (camera.viewIsAvailable()) {
            camera.setUpCamera();
            camera.openCamera();
        } else {
            camera.setSurfaceTextureListener();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        camera.closeCamera();
        camera.closeBackgroundThread();
    }

    @Override
    protected void onPause() {
        super.onPause();
        camera.closeCamera();
        camera.closeBackgroundThread();
    }

}
