package com.teaminversion.envisionbuddy;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.content.FileProvider;

import com.canhub.cropper.CropImageContract;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.canhub.cropper.CropImage;
import com.canhub.cropper.CropImageView;
import com.canhub.cropper.CropImageActivity;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Result;

public class HomeFragment extends Fragment {

    String mCurrentPhotoPath;
    static MainRecyclerViewAdapter mainAdapter;
    private static final int REQUEST_CAMERA_PERMISSION = 101;
    private static final int REQUEST_GALLERY_PERMISSION = 102;
    private static final int REQUEST_IMAGE_CAPTURE = 103;
    private static final int REQUEST_PICK_IMAGE = 104;
    private Uri imageUri;

    private void showImageSourceDialog() {
        CharSequence[] options = {"Camera", "Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose an option");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        if (checkCameraPermission()) {
                            dispatchTakePictureIntent();
                        } else {
                            requestCameraPermission();
                        }
                        break;
                    case 1:
                        if (checkGalleryPermission()) {
                            openGallery();
                        } else {
                            requestGalleryPermission();
                        }
                        break;
                }
            }
        });
        builder.show();
    }


    private File photoFile;
    static ArrayList<Map<String, String>> recentList = new ArrayList<>();
    static String resultText;
    Button continueButton;
    TextView textView;
    ActivityResultLauncher<String> mGetontent;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("com.teaminversion.envisionbuddy", Context.MODE_PRIVATE);
        recentList.clear();
        try {
            recentList = (ArrayList<Map<String, String>>) ObjectSerializer.deserialize(sharedPreferences.getString("recentList", ObjectSerializer.serialize(new ArrayList<Map<String, String>>())));
        } catch (IOException e) {
            e.printStackTrace();
        }

        resultText = "";
        RecyclerView mainRecyclerView = root.findViewById(R.id.mainRecyclerView);
        mainAdapter = new MainRecyclerViewAdapter(recentList, getActivity());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, true);
        layoutManager.setStackFromEnd(true);
        mainRecyclerView.setLayoutManager(layoutManager);
        mainRecyclerView.setAdapter(mainAdapter);

        mainAdapter.notifyDataSetChanged();

        FloatingActionButton logoutButton = root.findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        FloatingActionButton scanButton = root.findViewById(R.id.scanButton);
        scanButton.setOnClickListener(v -> {
            if (getActivity().checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            } else {
                showImageSourceDialog();
                //dispatchTakePictureIntent();
                //mGetontent.launch("image/*");

            }
            /*if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
            } else {
                dispatchTakePictureIntent();
            }*/

        });
//        mGetontent = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
//            @Override
//            public void onActivityResult(Uri o) {
//                Intent intent = new Intent(getContext(),CropperActivity.class);
//                intent.putExtra("DATA", resultText.toString());
//                startActivityForResult(intent,101);
//            }
//        });


        ImageButton clearImageButton = root.findViewById(R.id.clearImageButton);
        clearImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recentList.clear();
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("com.teaminversion.envisionbuddy", Context.MODE_PRIVATE);
                try {
                    sharedPreferences.edit().putString("recentList", ObjectSerializer.serialize(recentList)).apply();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mainAdapter.notifyDataSetChanged();
            }
        });

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DocumentReference userDocRef = db.collection("users").document(userId);

        userDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    // Retrieve quiz scores arraylist from Firestore
                    ArrayList<Integer> quizScores = (ArrayList<Integer>) documentSnapshot.get("quiz_scores");

                    List<Entry> entries = new ArrayList<>();
                    for (int i = 0; i < quizScores.size(); i++) {
                        // Print the value of each quiz score
                        Log.d("Quiz Score", "Value at index " + i + ": " + quizScores.get(i));
                        long k = ((Number) quizScores.get(i)).longValue();
                        entries.add(new Entry(i+1, k));
                    }


                    // Step 3: Plot the graph using a graph plotting library
                    LineChart lineChart = root.findViewById(R.id.lineChart);
                    LineDataSet dataSet = new LineDataSet(entries, "Quiz Scores");
//                    dataSet.setValueFormatter(new ValueFormatter() {
//                        @Override
//                        public String getFormattedValue(float value) {
//                            return "Quiz Scores";
//                        }
//                    });
//                    dataSet.setValueTextSize(12f);
//                    dataSet.setValueTextColor(Color.BLACK);
                    dataSet.setLineWidth(3f);
                    LineData lineData = new LineData(dataSet);
                    lineChart.setData(lineData);
                    lineChart.setDescription(null);
                    lineChart.invalidate(); // Refresh the chart
                } else {
                    Log.d("Fetch Quiz Scores", "Document not found");
                }
            }
        });


        return root;
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();

        // Navigate to login activity
        Intent intent = new Intent(getContext(), loginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear the activity stack
        startActivity(intent);
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        mCurrentPhotoPath = image.getAbsolutePath();
        Log.d("Image URI",mCurrentPhotoPath);
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (photoFile != null) {
                imageUri = FileProvider.getUriForFile(getActivity(), getActivity().getApplicationContext().getPackageName() + ".provider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
//        if (getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//
//            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_STORAGE_PERMISSION_CODE);
//        } else {
//            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//
//            if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
//                photoFile = null;
//                try {
//                    photoFile = createImageFile();
//                    Log.d("ImageFile", "Created");
//                } catch (IOException ex) {
//                    ex.printStackTrace();
//                }
//
//                if (photoFile != null) {
////                    CropImage.activity()
////                            .setGuidelines(CropImageView.Guidelines.ON)
////                            .start(getContext(),this);
//
//
//                }
//            }
//
//        }
//        Intent i = new Intent();
//        i.setType("image/*");
//        i.setAction(Intent.ACTION_GET_CONTENT);
//
//        // pass the constant to compare it
//        // with the returned requestCode
//        startActivityForResult(Intent.createChooser(i, "Select Picture"), 200);
    }
    private void openGallery() {
        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhotoIntent, REQUEST_PICK_IMAGE);
    }
    // Method to check camera permission
    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    // Method to request camera permission
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
    }

    // Method to check gallery permission
    private boolean checkGalleryPermission() {
        return ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    // Method to request gallery permission
    private void requestGalleryPermission() {
        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_GALLERY_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getActivity(), "Camera permission granted", Toast.LENGTH_LONG).show();
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(getActivity(), "Camera permission denied", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == REQUEST_GALLERY_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getActivity(), "Storage permission granted", Toast.LENGTH_LONG).show();
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(getActivity(), "Storage permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("requestCode", String.valueOf(requestCode));
        Log.d("resultCode", String.valueOf(resultCode));
        try {
            if (resultCode == Activity.RESULT_OK) {
                switch (requestCode) {
                    case REQUEST_IMAGE_CAPTURE:
                        // Image captured from camera
                        // Use the imageUri here
                        Bitmap bitmap1 = null;
                        try {
                            bitmap1 = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (bitmap1 != null) {
                            InputImage image = InputImage.fromBitmap(bitmap1, 0);
                            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
                            //TextRecognizer recognizer = TextRecognition.getClient();
                            Task<Text> result = recognizer.process(image)
                                    .addOnSuccessListener(visionText -> processTextBlock(visionText))
                                    .addOnFailureListener(e -> Toast.makeText(getActivity(), "Failed", Toast.LENGTH_SHORT).show());
                        }
                        //Toast.makeText(getActivity(), "Image captured from camera: " + imageUri.toString(), Toast.LENGTH_SHORT).show();
                        break;
                    case REQUEST_PICK_IMAGE:
                        // Image picked from gallery
                        Uri resultUri = data.getData();
                        // Use the selectedImage Uri here
                        Bitmap bitmap2 = null;
                        try {
                            bitmap2 = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), resultUri);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (bitmap2 != null) {
                            InputImage image = InputImage.fromBitmap(bitmap2, 0);
                            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
                            //TextRecognizer recognizer = TextRecognition.getClient();
                            Task<Text> result = recognizer.process(image)
                                    .addOnSuccessListener(visionText -> processTextBlock(visionText))
                                    .addOnFailureListener(e -> Toast.makeText(getActivity(), "Failed", Toast.LENGTH_SHORT).show());
                        }
                        //Toast.makeText(getActivity(), "Image picked from gallery: " + resultUri.toString(), Toast.LENGTH_SHORT).show();
                        break;
                }
            }
            else {
                Log.d("CropResult", "resultCode is not RESULT_OK");
                //Toast.makeText(getActivity(), "Crop failed: " + cropResult.getError(), Toast.LENGTH_SHORT).show();
            }
//            if(resultCode== RESULT_OK && requestCode==200) {
//                //String resultString = data.getStringExtra("RESULT");
//                Uri resultUri = data.getData();;
//                if(resultUri!=null) {
//                    //resultUri = Uri.parse(resultString);
//                    Log.d("CropResult URI", String.valueOf(resultUri));
//                    Bitmap bitmap = null;
//                    try {
//                        bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), resultUri);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    if (bitmap != null) {
//                        InputImage image = InputImage.fromBitmap(bitmap, 0);
//                        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
//                        //TextRecognizer recognizer = TextRecognition.getClient();
//                        Task<Text> result = recognizer.process(image)
//                                .addOnSuccessListener(visionText -> processTextBlock(visionText))
//                                .addOnFailureListener(e -> Toast.makeText(getActivity(), "Failed", Toast.LENGTH_SHORT).show());
//                    }
//                }
//            }
//            else {
//                Log.d("CropResult", "resultCode is not RESULT_OK");
//                //Toast.makeText(getActivity(), "Crop failed: " + cropResult.getError(), Toast.LENGTH_SHORT).show();
//            }
        } catch (Exception error)
        {
            error.printStackTrace();
        }
    }

    private void processTextBlock(Text result) {
        resultText = result.getText();

        for (Text.TextBlock block : result.getTextBlocks()) {
            String blockText = block.getText();
            Point[] blockCornerPoints = block.getCornerPoints();
            Rect blockFrame = block.getBoundingBox();

            for (Text.Line line : block.getLines()) {
                String lineText = line.getText();
                Point[] lineCornerPoints = line.getCornerPoints();
                Rect lineFrame = line.getBoundingBox();

                for (Text.Element element : line.getElements()) {
                    String elementText = element.getText();
                    Point[] elementCornerPoints = element.getCornerPoints();
                    Rect elementFrame = element.getBoundingBox();
                }
            }
        }
        Navigation.findNavController(getView()).navigate(R.id.navigation_text);
    }



}
