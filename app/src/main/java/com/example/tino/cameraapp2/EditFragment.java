package com.example.tino.cameraapp2;

import android.Manifest;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import ja.burhanrashid52.photoeditor.PhotoEditor;
import ja.burhanrashid52.photoeditor.PhotoEditorView;
import ja.burhanrashid52.photoeditor.PhotoFilter;

import static android.view.View.GONE;


public class EditFragment extends Fragment  {

    private final String TAG = "EditorMode";
    private final int SELECT_IMAGE_REQUEST = 0;
    private final int ADD_IMAGE_REQUEST = 1;

    private PhotoEditorView mPhotoEditorView;
    private PhotoEditor mPhotoEditor;
    private Typeface mTextRobotoTf;

    private Button mDrawButton;
    private Button mFilterButton;
    private Button mAddImageButton;
    private Button mSaveButton;
    private Button mTextButton;

    private ConstraintLayout mBrushToolBox;
    private ConstraintLayout mAddTextToolBox;
    private ConstraintLayout mFilterToolBox;

    private NumberPicker mBrushSizePicker;
    private NumberPicker mOpacityPicker;

    private Button mWhiteButton;
    private Button mBlackButton;
    private Button mBlueButton;
    private Button mSkyBlueButton;
    private Button mBrownButton;
    private Button mOrangeButton;
    private Button mRedButton;
    private Button mVioletButton;
    private Button mYellowButton;
    private Button mYellowGreenButton;
    private Button mGreenButton;
    private ImageButton mEraserButton;

    private EditText mEditInputText;
    private Button mAddTextButton;

    private ImageView mNoFilter;
    private ImageView mGreyFilter;
    private ImageView mSepiaFilter;
    private ImageView mContrastFilter;
    private ImageView mSaturateFilter;
    private ImageView mTemperatureFilter;
    private ImageView mPosterizeFilter;
    private ImageView mVignetterFilter;

    public static Bitmap mMainImage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        mPhotoEditorView = view.findViewById(R.id.photoEditorView);
        //Sends a request to get any image file  that will be edited.
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture)), SELECT_IMAGE_REQUEST);

        //Custom font to be used in the text addition
        mTextRobotoTf = ResourcesCompat.getFont(getContext(), R.font.roboto_medium);
        //Sets up the photoeditor
        mPhotoEditor = new PhotoEditor.Builder(getContext(), mPhotoEditorView)
                .setPinchTextScalable(true)
                .setDefaultTextTypeface(mTextRobotoTf)
                .build();

        //Finds the toolboxes that contain the picture modification tools, so that the boxes can be hidden
        mBrushToolBox = getView().findViewById(R.id.brushToolBox);
        mAddTextToolBox = getView().findViewById(R.id.textToolBox);
        mFilterToolBox = getView().findViewById(R.id.filterToolBox);

        mDrawButton = view.findViewById(R.id.drawButton);
        mDrawButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFilterToolBox.setVisibility(GONE);
                mAddTextToolBox.setVisibility(GONE);
                mPhotoEditor.setBrushDrawingMode(true);
                openBrushTools();
            }
        });

        mFilterButton = view.findViewById(R.id.filterButton);
        mFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAddTextToolBox.setVisibility(GONE);
                endDrawMode();
                mFilterToolBox.setVisibility(View.VISIBLE);

                setFilter(mNoFilter, R.id.noFilter, PhotoFilter.NONE);
                setFilter(mGreyFilter, R.id.greyFilter, PhotoFilter.GRAY_SCALE);
                setFilter(mSepiaFilter, R.id.sepiaFilter, PhotoFilter.SEPIA);
                setFilter(mContrastFilter, R.id.contrastFilter, PhotoFilter.CONTRAST);
                setFilter(mSaturateFilter, R.id.saturateFilter, PhotoFilter.SATURATE);
                setFilter(mTemperatureFilter, R.id.temperatureFilter, PhotoFilter.TEMPERATURE);
                setFilter(mPosterizeFilter, R.id.posterizeFilter, PhotoFilter.POSTERIZE);
                setFilter(mVignetterFilter, R.id.vignetteFilter, PhotoFilter.VIGNETTE);
            }
        });

        mAddImageButton = view.findViewById(R.id.pictureButton);
        mAddImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFilterToolBox.setVisibility(GONE);
                mAddTextToolBox.setVisibility(GONE);
                endDrawMode();
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture)), ADD_IMAGE_REQUEST);
            }
        });

        mSaveButton = view.findViewById(R.id.saveButton);
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPhotoEditorView.buildDrawingCache(true);
                mMainImage = mPhotoEditorView.getDrawingCache(true).copy(Bitmap.Config.RGB_565, false);
                mPhotoEditorView.destroyDrawingCache();
                showSaveDialog();
            }
        });


        mTextButton = view.findViewById(R.id.textButton);
        mTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Hides the other toolboxes, incase they were in the way
                endDrawMode();
                mFilterToolBox.setVisibility(GONE);
                mAddTextToolBox.setVisibility(View.VISIBLE);

                mEditInputText = getView().findViewById(R.id.editInputText);
                mAddTextButton = getView().findViewById(R.id.addTextButton);
                mAddTextButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String inputText = mEditInputText.getText().toString();
                        Log.e(TAG, inputText);
                        if (inputText != null && inputText.length() > 0) {
                            mPhotoEditor.addText(inputText, R.color.black);
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Used when fetching an additional picture to be added to the main image
        if (requestCode == ADD_IMAGE_REQUEST && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
                mPhotoEditor.addImage(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //Used to load the main image
        if (requestCode == SELECT_IMAGE_REQUEST && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
                mPhotoEditorView.getSource().setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if(requestCode == SELECT_IMAGE_REQUEST) {
            //Returns to main activity if no image is selected=
            getActivity().finish();
        }
    }

    private void openBrushTools() {
        mBrushToolBox.setVisibility(View.VISIBLE);

        mBrushSizePicker = getView().findViewById(R.id.brushSizePicker);
        mBrushSizePicker.setMinValue(1);
        mBrushSizePicker.setMaxValue(20);
        mPhotoEditor.setBrushSize(mBrushSizePicker.getValue() * 3);

        mBrushSizePicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                //Brush width is set to be 3pixels times the value in the size NumberPicker
                mPhotoEditor.setBrushSize(numberPicker.getValue() * 3);
            }
        });

        mOpacityPicker = getView().findViewById(R.id.opacityPicker);
        mOpacityPicker.setMinValue(1);
        mOpacityPicker.setMaxValue(10);
        mOpacityPicker.setValue(10);

        mOpacityPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                //Brush opacity is se to be 10% times the value in the opacity NumberPicker
                mPhotoEditor.setOpacity(numberPicker.getValue() * 10);
            }
        });

        //Sets all of the brush color pickers to be functional
        mWhiteButton = getView().findViewById(R.id.whiteButton);
        applyColorPicker(mWhiteButton);
        mBlackButton = getView().findViewById(R.id.blackButton);
        applyColorPicker(mBlackButton);
        mBlueButton = getView().findViewById(R.id.blueButton);
        applyColorPicker(mBlueButton);
        mSkyBlueButton = getView().findViewById(R.id.skyBlueButton);
        applyColorPicker(mSkyBlueButton);
        mBrownButton = getView().findViewById(R.id.brownButton);
        applyColorPicker(mBrownButton);
        mOrangeButton = getView().findViewById(R.id.orangeButton);
        applyColorPicker(mOrangeButton);
        mRedButton = getView().findViewById(R.id.redButton);
        applyColorPicker(mRedButton);
        mVioletButton = getView().findViewById(R.id.violetButton);
        applyColorPicker(mVioletButton);
        mYellowButton = getView().findViewById(R.id.yellowButton);
        applyColorPicker(mYellowButton);
        mYellowGreenButton = getView().findViewById(R.id.yellowGreenButton);
        applyColorPicker(mYellowGreenButton);
        mGreenButton = getView().findViewById(R.id.greenButton);
        applyColorPicker(mGreenButton);

        //Sets the erase button to be functional
        mEraserButton = getView().findViewById(R.id.eraseButton);
        mEraserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPhotoEditor.brushEraser();
            }
        });
    }

    private void applyColorPicker(final Button button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPhotoEditor.setBrushColor(((ColorDrawable) button.getBackground()).getColor());
            }
        });
    }

    //Helper method to end draw mode when other tools are selected
    private void endDrawMode() {
        mBrushToolBox.setVisibility(GONE);
        mPhotoEditor.setBrushDrawingMode(false);
    }

    private void savePicture() {
        //Current time added to filename
        final File file = new File((Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/pictures/" +
                Calendar.getInstance().getTime().toString() + ".png"));
        try {
            file.createNewFile();
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 52);
                return;
            }
            mPhotoEditor.saveAsFile(file.getAbsolutePath(), new PhotoEditor.OnSaveListener() {
                @Override
                public void onSuccess(@NonNull String imagePath) {
                    try {
                        Camera2BasicFragment.addImageToGallery(file, getContext());
                        Toast.makeText(getActivity(), getResources().getString(R.string.saved) + file, Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Image Saved Successfully");
                        //Goes back to main activity if save is successful
                    } catch (NullPointerException e) {
                        Log.e(TAG, "Failed to add image to gallery");
                    }
                }

                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.e(TAG, "Failed to save Image");
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showSaveDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.select_save);
        builder.setPositiveButton(R.string.save_and_wallpaper, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                savePicture();
                WallpaperManager wallpaperManager = WallpaperManager.getInstance(getContext());
                try {
                    wallpaperManager.setBitmap(mMainImage);
                    getActivity().moveTaskToBack(true);
                } catch (IOException e) {
                    Log.e(TAG, "Setting wallpaper failed");
                }
            }
        });
        builder.setNeutralButton(R.string.just_save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                savePicture();
                getActivity().finish();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
// Create the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //Helper method to set apply the selected filter to the main image
    private void setFilter(ImageView imageView, int resourceId, final PhotoFilter photoFilter) {
        imageView = getView().findViewById(resourceId);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPhotoEditor.setFilterEffect(photoFilter);
            }
        });
    }
}
