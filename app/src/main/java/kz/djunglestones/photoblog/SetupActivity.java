package kz.djunglestones.photoblog;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.w3c.dom.Text;

import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private android.support.v7.widget.Toolbar setupToolbar;
    private CircleImageView setupImage;
    private Uri mainImageUri = null;

    private EditText setup_name;
    private Button setup_btn;
    private ProgressBar setupProgress;

    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        setupToolbar = (android.support.v7.widget.Toolbar)findViewById(R.id.setupToolbar);
        setSupportActionBar(setupToolbar);
        getSupportActionBar().setTitle("Account Setup");

        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        setup_name = (EditText)findViewById(R.id.setup_name);
        setup_btn = (Button)findViewById(R.id.setup_btn);

        setupProgress = (ProgressBar)findViewById(R.id.setup_progress);


        setup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String user_name = setup_name.getText().toString();

                if(!TextUtils.isEmpty(user_name) && mainImageUri!=null){

                    String user_id = firebaseAuth.getCurrentUser().getUid();
                    setupProgress.setVisibility(View.VISIBLE);

                    StorageReference imagePath = storageReference.child("profile_images").child(user_id+".jpg");
                    imagePath.putFile(mainImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                            if(task.isSuccessful()){

                                Uri download_uri = task.getResult().getDownloadUrl();
                                Toast.makeText(SetupActivity.this,"The Image is Uploaded",Toast.LENGTH_LONG).show();

                            }else{
                                String error = task.getException().getMessage();
                                Toast.makeText(SetupActivity.this,"Error: "+error,Toast.LENGTH_LONG).show();
                                System.out.print("Error: "+error);

                            }

                            setupProgress.setVisibility(View.INVISIBLE);
                        }
                    });

                }

            }
        });

        setupImage = (CircleImageView)findViewById(R.id.setup_image);

        setupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){

                    if (ContextCompat.checkSelfPermission(SetupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){

                        Toast.makeText(SetupActivity.this,"Permission Denied",Toast.LENGTH_LONG).show();
                        ActivityCompat.requestPermissions(SetupActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                    }
                    else{
                        CropImage.activity()
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .setAspectRatio(1,1)
                                .start(SetupActivity.this);
                    }
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mainImageUri = result.getUri();

                setupImage.setImageURI(mainImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

            }
        }
    }
}
