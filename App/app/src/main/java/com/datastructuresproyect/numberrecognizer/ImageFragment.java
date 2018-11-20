package com.datastructuresproyect.numberrecognizer;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.datastructuresproyect.numberrecognizer.Interfaces.CameraController;
import com.datastructuresproyect.numberrecognizer.Utils.BitmapManager;
import com.datastructuresproyect.numberrecognizer.Utils.Communication;


public class ImageFragment extends DialogFragment {

    private static final String TAG = "DialogFragment";
    private static final String ARG1 = "IMAGE";
    private static final String ARG2 = "CAMERA_CONTROLLER";
    private BitmapManager bmpManager;
    private CameraController cameraController;
    private float contrast = 1;
    private int brightness = 0;

    public ImageFragment() {
        // Required empty public constructor
    }

    public static ImageFragment newInstance(BitmapManager bmpManager, CameraController cameraController) {
        ImageFragment fragment = new ImageFragment();
        Bundle args = new Bundle();
        args.putByteArray(ARG1, bmpManager.getBytes());
        args.putSerializable(ARG2, cameraController);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            bmpManager = new BitmapManager(getArguments().getByteArray(ARG1));
            cameraController = (CameraController) getArguments().getSerializable(ARG2);
        }

        changeImage(view, bmpManager.getBitmap());

        view.findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageFragment.this.dismiss();
                cameraController.unlock();
            }
        });

        view.findViewById(R.id.btnOk).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Communication communication = Communication.connect("192.168.1.56");
                communication.sendIntArray(bmpManager.getPixelArray(getBitmap(view)));
                int answer = communication.receiveAnswer();
                Toast.makeText(getContext(), "Your number is: "+ answer, Toast.LENGTH_LONG).show();
                communication.close();
            }
        });

        ((SeekBar)view.findViewById(R.id.sb_contrast)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progress-=255;
                contrast =progress<=0?((float)progress+255)/255:(float)progress*(9.0f/255)+1;
                changeImage(view, bmpManager.changeContrastBrightness(contrast,brightness));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        ((SeekBar)view.findViewById(R.id.sb_light)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                brightness=progress-255;
                changeImage(view, bmpManager.changeContrastBrightness(contrast,brightness));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_image, container, false);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        cameraController.unlock();
    }

    public void changeImage(View view, Bitmap bmp){
        ((ImageView)view.findViewById(R.id.image)).setImageBitmap(bmp);
    }

    public Bitmap getBitmap(View view){
        return ((BitmapDrawable)((ImageView)view.findViewById(R.id.image)).getDrawable()).getBitmap();
    }

}
