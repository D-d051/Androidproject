package com.dipen.sqlite_recview;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static android.app.Activity.RESULT_OK;


public class ItemViewFragment extends Fragment
        implements View.OnClickListener {

    public static final String TAG = "sam_bread";

    public static final int TAKE_PHOTO = 0;
    public static final int CHOOSE_IMAGE = 1;

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public boolean bIsLocationFound;
    LocationManager mLocationManager;

    DatePickerDialog.OnDateSetListener mDateListener;
    Calendar mCalendar;

    EditText mEditText_Title, mEditText_Place, mEditText_Details;
    Button mBtn_Date, mBtn_ShowOnMap, mBtn_Share;
    TextView mTextView_Location;
    ImageView mImageView_Image;

    String mRowId;

    private ListSqliteOpenHelper mOpenHelper;

    public ItemViewFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_item_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bIsLocationFound = false;

        mOpenHelper = new ListSqliteOpenHelper(getContext());

        mRowId = getArguments().getString(ListSqliteOpenHelper.COL_1_ID, "null");

        mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        mCalendar = Calendar.getInstance();

        mDateListener = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                mCalendar.set(Calendar.YEAR, year);
                mCalendar.set(Calendar.MONTH, monthOfYear);
                mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                // update the Text on the button
                long unixTime = mCalendar.getTimeInMillis();
                String formatedDate = mOpenHelper.formatDate(unixTime);
                mBtn_Date.setText(formatedDate);
            }

        };

        mEditText_Title = ((EditText) view.findViewById(R.id.et_title));
        mEditText_Place = ((EditText) view.findViewById(R.id.et_place));
        mEditText_Details = ((EditText) view.findViewById(R.id.et_details));

        mBtn_Date = ((Button) view.findViewById(R.id.btn_date));
        mBtn_Date.setOnClickListener(this);

        if (TextUtils.equals(mRowId, "null")) {
            mBtn_Date.setText(getCurrentDate());
        }

        mBtn_ShowOnMap = ((Button) view.findViewById(R.id.btn_show_on_maps));
        mBtn_ShowOnMap.setOnClickListener(this);
        mBtn_Share = ((Button) view.findViewById(R.id.btn_share));
        mBtn_Share.setOnClickListener(this);

        mTextView_Location = ((TextView) view.findViewById(R.id.tv_location));

        mImageView_Image = ((ImageView) view.findViewById(R.id.iv_image));
        mImageView_Image.setOnClickListener(this);

        if (!TextUtils.equals(mRowId, "null")) {
            populateWithData(mRowId);
        }


        setupLocation();
    }

    public void populateWithData(String rowId) {
        CheckIn checkIn = mOpenHelper.getDataWithId(rowId);
        if (checkIn == null) {
            Toast.makeText(getContext(), "Data Failed to load", Toast.LENGTH_SHORT).show();
            return;
        }

        mEditText_Title.setText(checkIn.title);
        mEditText_Place.setText(checkIn.place);
        mEditText_Details.setText(checkIn.details);

        mBtn_Date.setText(mOpenHelper.formatDate(checkIn.date));
        mCalendar.setTimeInMillis(checkIn.date);
        mTextView_Location.setText(checkIn.location);

        byte[] outImage = checkIn.image;

        if (outImage.length > 0) {
            ByteArrayInputStream imageStream = new ByteArrayInputStream(outImage);
            Bitmap theImage = BitmapFactory.decodeStream(imageStream);

            if (theImage != null) {
                mImageView_Image.setImageBitmap(theImage);
            }
        }



    }

    public void setupLocation() {
        if (TextUtils.equals(mRowId, "null")) {
            Log.d(TAG, "onViewCreated: new item");
            if (checkLocationPermission()) {
                Log.d(TAG, "onViewCreated: have permissions");
                mTextView_Location.setText("Fetching location, this may take some time...");

                mLocationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        if (location != null) {
                            String strLocation = location.getLatitude() + "\n" + location.getLongitude();
                            mTextView_Location.setText(strLocation);
                            bIsLocationFound = true;
                        }
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {

                    }

                    @Override
                    public void onProviderEnabled(String provider) {

                    }

                    @Override
                    public void onProviderDisabled(String provider) {

                    }
                }, null);

            }
        }
    }

    public boolean updateData() {
        if (TextUtils.equals(mRowId, "null")) {
            return false;
        }

        boolean hasEmptyFields = false;

        String errorMessage = "Field Cannot be empty";

        String title = mEditText_Title.getText().toString();
        String place = mEditText_Place.getText().toString();
        String details = mEditText_Details.getText().toString();

        if (TextUtils.isEmpty(title)) {
            mEditText_Title.setError(errorMessage);
            hasEmptyFields = true;
        }
        if (TextUtils.isEmpty(place)) {
            mEditText_Place.setError(errorMessage);
            hasEmptyFields = true;
        }
        if (TextUtils.isEmpty(details)) {
            mEditText_Details.setError(errorMessage);
            hasEmptyFields = true;
        }

        Bitmap bitmap = ((BitmapDrawable) mImageView_Image.getDrawable()).getBitmap();
        byte[] imageInByte = {};

        if (bitmap != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            imageInByte = stream.toByteArray();
        }

        String location = mTextView_Location.getText().toString();

        if (hasEmptyFields) {
            return false;
        }

        CheckIn checkIn = new CheckIn(title, place, details, mCalendar.getTimeInMillis(), location, imageInByte);

        return mOpenHelper.updateRowWithId(mRowId, checkIn);
    }

    public boolean saveData() {
        boolean hasEmptyFields = false;

        String errorMessage = "Field Cannot be empty";

        String title = mEditText_Title.getText().toString();
        String place = mEditText_Place.getText().toString();
        String details = mEditText_Details.getText().toString();

        if (TextUtils.isEmpty(title)) {
            mEditText_Title.setError(errorMessage);
            hasEmptyFields = true;
        }
        if (TextUtils.isEmpty(place)) {
            mEditText_Place.setError(errorMessage);
            hasEmptyFields = true;
        }
        if (TextUtils.isEmpty(details)) {
            mEditText_Details.setError(errorMessage);
            hasEmptyFields = true;
        }

        String location = "N/A";

        if (bIsLocationFound) {
            location = mTextView_Location.getText().toString();
        }

        Bitmap bitmap = ((BitmapDrawable) mImageView_Image.getDrawable()).getBitmap();
        byte[] imageInByte = {};

        if (bitmap != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            imageInByte = stream.toByteArray();
        }

        if (hasEmptyFields) {
            return false;
        }

        CheckIn checkIn = new CheckIn(title, place, details, System.currentTimeMillis(), location, imageInByte);

        return mOpenHelper.insertData(checkIn);
    }

    public boolean deleteCurrentData() {
        if (TextUtils.equals(mRowId, "null")) {
            return false;
        }

        return mOpenHelper.deleteDataWithID(mRowId);
    }

    private String getCurrentDate() {

        long unixTime = System.currentTimeMillis();

        DateFormat formatter = new SimpleDateFormat("EEE, d MMM yyyy"); // "EEE, d MMM yyyy HH:mm:ss"
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(unixTime);
        return formatter.format(calendar.getTime());

    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        switch (viewId) {
            case R.id.iv_image:
                showImagePickerDialog();
                break;
            case R.id.btn_date:
                showDatePickerDialog();
                break;
            case R.id.btn_show_on_maps:
                showOnMap();
                break;
            case R.id.btn_share:
                share();
                break;
            default:

                break;
        }
    }

    private void share() {
        String title = mEditText_Title.getText().toString();
        String place = mEditText_Place.getText().toString();
        String date = mBtn_Date.getText().toString();

        String shareBody = String.format("%s,%s was visited on %s", title, place, date);
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Check In");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, "Share Using"));
    }

    private void showOnMap() {

        String[] locationData = mTextView_Location.getText().toString().split("\n");

        if (locationData.length == 2) {
            String uri = String.format("geo:%s,%s", locationData[0], locationData[1]);

            Uri gmmIntentUri = Uri.parse(uri);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivity(mapIntent);
            }
        } else {
            Toast.makeText(getContext(), "Invalid Location", Toast.LENGTH_SHORT).show();
        }

    }

    public void showDatePickerDialog() {

        new DatePickerDialog(
                getContext(),
                mDateListener,
                mCalendar.get(Calendar.YEAR),
                mCalendar.get(Calendar.MONTH),
                mCalendar.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    public void showImagePickerDialog() {
        final AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();

        alertDialog.setTitle("Select Image");

        alertDialog.setMessage("Please choose or take photo");

        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Take Photo", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                takePhotoFromCamera();
            }
        });

        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Choose Image", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                chooseImageFromGallery();
            }
        });

        alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.cancel();
            }
        });

        alertDialog.show();

    }

    public void takePhotoFromCamera() {
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePicture, TAKE_PHOTO);
    }

    public void chooseImageFromGallery() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto , CHOOSE_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case TAKE_PHOTO:
                if(resultCode == RESULT_OK){
                    Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                    mImageView_Image.setImageBitmap(bitmap);
                }

                break;
            case CHOOSE_IMAGE:
                if(resultCode == RESULT_OK){
                    Uri selectedImage = data.getData();
                    mImageView_Image.setImageURI(selectedImage);
                }
                break;
        }
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(getContext())
                        .setTitle("Need Location permissions")
                        .setMessage("Please allow Location permissions to all the app to read you location")
                        .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                requestPermissions( new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.d(TAG, "onRequestPermissionsResult: called");

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.

                    if (ContextCompat.checkSelfPermission(getContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        setupLocation();
                    }

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    mTextView_Location.setText("Unable to find Location");

                }
            }

        }
    }

}
