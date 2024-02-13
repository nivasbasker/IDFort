package com.zio.idfort.ui;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.zio.idfort.R;
import com.zio.idfort.data.DocsDAO;
import com.zio.idfort.data.DocsDB;
import com.zio.idfort.data.DocsEntity;
import com.zio.idfort.databinding.ActivityAddDetailsBinding;
import com.zio.idfort.utils.Constants;
import com.zio.idfort.utils.DataExtractor;
import com.zio.idfort.utils.OCRSpaceCall;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class AddDetails extends AppCompatActivity {

    private String doc_name = "";
    private Uri uri = null;
    private File file;
    EditText id, name, dob, gender;
    TextView doc;

    ActivityAddDetailsBinding binding;
    PopupWindow popupWindow = null;

    private final ActivityResultLauncher<Intent> getImage =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {

                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        boolean isSuccess = data.getBooleanExtra("success", false);
                        if (isSuccess) {
                            Log.d(Constants.TAG, "Successfully got the image");
                            file = new File(getCacheDir(), Constants.TEMP_FILE);
                            selected();
                        } else fin();
                    } else fin();
                } else fin();
            });

    private void fin() {
        Toast.makeText(this, "cancelled", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        id = binding.docId;
        name = binding.docName;
        dob = binding.dob;
        gender = binding.gender;
        doc = binding.doc;


        Intent intent = getIntent();
        doc_name = intent.getStringExtra("Name");
        doc.setText("Document : " + doc_name);

        getImage.launch(new Intent(this, CamActivity.class));

        //file = new File(getCacheDir(), "temp.jpg");selected();


    }

    private void openPop() {
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        popupWindow = new PopupWindow(inflater.inflate(R.layout.popup_loading, null, false), binding.getRoot().getWidth(), binding.getRoot().getHeight(), true);

        popupWindow.setAnimationStyle(androidx.appcompat.R.style.Animation_AppCompat_Dialog);
        popupWindow.showAtLocation(binding.getRoot(), Gravity.CENTER, 0, 0);
    }

    private void closePop() {
        if (popupWindow != null && popupWindow.isShowing()) {
            // Dismiss the PopupWindow
            popupWindow.dismiss();
        }
    }

    private String convertFileToBase64() {
        try {
            //InputStream iStream = getContentResolver().openInputStream(fileUri);
            InputStream iStream = new FileInputStream(file);
            byte[] inputData = getBytes(iStream);
            iStream.close();
            return Base64.encodeToString(inputData, Base64.NO_WRAP);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private void selected() {
        openPop();
        String base64 = "data:image/jpg;base64," + convertFileToBase64();
        Log.d(Constants.TAG, base64);

        OCRSpaceCall OCRcall = new OCRSpaceCall(this);
        OCRcall.SetResultListener(base64, new OCRSpaceCall.ResultListenerCallback() {
            @Override
            public void onActionSuccess(String response) {
                extract(response);
            }

            @Override
            public void onActionFailure(String Error) {
                Toast.makeText(AddDetails.this, Error, Toast.LENGTH_SHORT).show();
                fin();
            }
        });
    }

    private void copyFile() {
        if (doc_name.isEmpty()) {
            Toast.makeText(this, "Something Went Wrong, Please try again", Toast.LENGTH_SHORT).show();
            return;
        }
        try {

            InputStream iStream = new FileInputStream(file);
            byte[] inputData = getBytes(iStream);

            FileOutputStream fos = new FileOutputStream(new File(getFilesDir(), doc_name + ".jpg"));
            fos.write(inputData);
            Log.d(Constants.TAG, "File copied");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void AddAndUpdate(String id, String p_name) {
        DocsEntity entity = new DocsEntity();
        entity.setDocument_name(doc_name);
        entity.setUri("");
        entity.setId(id);
        entity.setName(p_name);

        DocsDB db = Room.databaseBuilder(this, DocsDB.class, "DOCS").allowMainThreadQueries().build();
        DocsDAO docsDao = db.docsdao();
        docsDao.insertDoc(entity);
        db.close();

        Log.d(Constants.TAG, "Added to DB");

    }


    public void save_new_document(View view) {

        String doc_id = id.getText().toString(), doc_name = name.getText().toString();
        if (!doc_id.isEmpty() && !doc_name.isEmpty()) {
            try {
                copyFile();
                AddAndUpdate(doc_id, doc_name);
            } catch (UnknownError e) {
                e.printStackTrace();
            }
            finish();

        } else {
            Toast.makeText(this, "Please fill the personal identification information (PII) manually", Toast.LENGTH_SHORT).show();
        }
    }

    private void extract(String txt) {

        Map<String, String> result = DataExtractor.adhaarReader(txt);
        id.setText(result.get("UID"));
        name.setText(result.get("Name"));
        dob.setText(result.get("DOB"));
        gender.setText(result.get("Sex"));

        closePop();
    }


}