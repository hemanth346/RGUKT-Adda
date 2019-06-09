package com.example.rguktadda;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private android.support.v7.widget.Toolbar mainToolBar;
    private Button fireBaseButton;
    private DatabaseReference mDatabase;
    private EditText emailField;
    private  EditText password;
    private EditText username;

    private FloatingActionButton addPostButton;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseFirestore firebaseFirestore;

    private String currentUserId;

    private BottomNavigationView mainBottomNav;

   // private HomeFragment homeFragment;
    private NotificationFragment notificationFragment;
    private AccountFragment accountFragment;
    private HomeFragment homeFragment;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        mainToolBar =  findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolBar);
        addPostButton = findViewById(R.id.add_post_button);

        mainBottomNav = findViewById(R.id.main_bottom_nav);

        //FRAGMENTS
        homeFragment = new HomeFragment();
        notificationFragment = new NotificationFragment();
        accountFragment = new AccountFragment();

        replaceFragment(homeFragment);
        mainBottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){

                    case R.id.bottom_home:
                        replaceFragment(homeFragment);
                        return true;

                    case R.id.bottom_account:
                        replaceFragment(accountFragment);
                        return true;
                    case R.id.bottom_notification:
                        replaceFragment(notificationFragment);
                        return true;



                }
                return false;
            }
        });




        addPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent addNewPostActivity = new Intent(MainActivity.this, NewPostActivity.class);
                startActivity(addNewPostActivity);
            }
        });
        //Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        //startActivity(loginIntent);


        /*emailField = findViewById(R.id.email);
        password = findViewById(R.id.password);
        username = findViewById(R.id.username);
        fireBaseButton = findViewById(R.id.signupButton);
        mDatabase= FirebaseDatabase.getInstance().getReference();

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if(firebaseAuth.getCurrentUser() != null){
                    //user loggedin
                }else{

                }
            }
        };*/


        /*fireBaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String uName = username.getText().toString().trim();
                String pass = password.getText().toString().trim();
                String emailId = emailField.getText().toString().trim();

                HashMap<String , String> dataMap = new HashMap<>();
                dataMap.put("name",uName);
                dataMap.put("email",emailId);
                dataMap.put("password",pass);

                //mDatabase.child("name").setValue("sagar");
                mDatabase.push().setValue(dataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(MainActivity.this,"Saved... ",Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(MainActivity.this, "Error occured while saving..",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });*/
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser  = FirebaseAuth.getInstance().getCurrentUser();

        if(currentUser == null){

           sendToLoginPage();

        }else{
            currentUserId = currentUser.getUid();

            firebaseFirestore.collection("Users").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){

                        if(!task.getResult().exists()){

                            Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
                            startActivity(setupIntent);
                            finish();
                        }
                    }else{

                        String errorMessage = task.getException().getMessage();
                        Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG);

                    }
                }
            });
        }

    }

    public  void sendToLoginPage(){
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_logout:
                logout();
                return true;
            case R.id.action_setup:
                showSetupScreen();
                return true;
            default:
                return true;
        }

    }

    public  void logout(){

        mAuth.signOut();
        sendToLoginPage();
    }
    public void showSetupScreen(){
        Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
        startActivity(setupIntent);
        finish();
    }

    private  void replaceFragment(Fragment fragment){

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_container, fragment);
        fragmentTransaction.commit();
    }
}
