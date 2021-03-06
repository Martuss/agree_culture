package com.capstone.agree_culture;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.capstone.agree_culture.Fragments.MainMenu;
import com.capstone.agree_culture.Fragments.MenuMessages;
import com.capstone.agree_culture.Fragments.MenuMyCart;
import com.capstone.agree_culture.Fragments.MenuOrders;
import com.capstone.agree_culture.Fragments.MenuProducts;
import com.capstone.agree_culture.Fragments.MenuPurchaseHistory;
import com.capstone.agree_culture.Helper.GlobalString;
import com.capstone.agree_culture.Helper.Helper;
import com.capstone.agree_culture.Model.Messages;
import com.capstone.agree_culture.Model.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.rpc.Help;

import java.io.IOException;
import java.io.Serializable;
import java.util.UUID;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private FirebaseAuth mAuth;
    private FirebaseUser mUser = null;

    private FirebaseFirestore mDatabase;

    private StorageReference mStorage;
    private StorageReference mUserPhoto;

    private User currentUser;

    private ImageView user_photo;
    private TextView user_full_name;
    private TextView user_email;

    /**
     * Fragment
     * Fragment Transaction Variable
     */
    private FragmentTransaction fragment_transaction;
    private int fragment = R.id.main_frame_content;

    /**
     * Fragments
     * variables
     */
    private MainMenu main_menu = new MainMenu();
    private MenuMessages messages = new MenuMessages();
    private MenuProducts menu_products = new MenuProducts();
    private MenuMyCart menuMyCart = new MenuMyCart();
    private MenuOrders menuOrders = new MenuOrders();
    private MenuPurchaseHistory menuPurchaseHistory = new MenuPurchaseHistory();


    /**
     * Menu
     */
    private Menu menu;

    private Context cont;

    private final static String USERS = "users";

    private final static int USER_PHOTO = 1012;
    private final static int PERMISSION_USER_PHOTO = 1013;
    private final static int UPDATE_USER = 1016;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cont = this;

        if (!Helper.isFirestoreSettingsInitialize) {
            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                    .setTimestampsInSnapshotsEnabled(true)
                    .build();
            FirebaseFirestore.getInstance().setFirestoreSettings(settings);

            Helper.isFirestoreSettingsInitialize = true;
        }

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseFirestore.getInstance();

        mStorage = FirebaseStorage.getInstance().getReference();

        mUser = mAuth.getCurrentUser();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        menu = navigationView.getMenu();

        user_photo = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.user_photo);
        user_full_name = (TextView) navigationView.getHeaderView(0).findViewById(R.id.user_full_name);
        user_email = (TextView) navigationView.getHeaderView(0).findViewById(R.id.user_email);


        fragment_transaction = getSupportFragmentManager().beginTransaction();
        fragment_transaction.replace(fragment, main_menu);
        fragment_transaction.commit();


        if (mUser != null) {

            invalidateOptionsMenu();

            mDatabase.collection(USERS).document(mUser.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {

                    currentUser = documentSnapshot.toObject(User.class);
                    currentUser.setDocumentId(mUser.getUid());

                    if (currentUser != null) {

                        Helper.currentUser = currentUser;

                        if (currentUser.getRole().equals(GlobalString.CUSTOMER) || currentUser.getRole().contains(GlobalString.CUSTOMER)) {
                            menu.findItem(R.id.nav_products).setVisible(false);
                            menu.findItem(R.id.nav_orders).setVisible(false);
                        } else if (currentUser.getRole().equals(GlobalString.DISTRIBUTOR)) {

                        } else if (currentUser.getRole().equals(GlobalString.SUPPLIER)) {
                            menu.findItem(R.id.nav_my_cart).setVisible(false);
                            menu.findItem(R.id.nav_purchase_history).setVisible(false);
                        }

                        user_photo.setOnClickListener(new ChangePhoto());

                        if(!TextUtils.isEmpty(currentUser.getPhoto())){
                            Glide.with(getApplicationContext()).load(currentUser.getPhoto()).placeholder(R.drawable.imageview_rectangular).into(user_photo);
                        }

                    }

                    main_menu.initializedHome(currentUser);

                    user_full_name.setText(getResources().getString(R.string.home_full_name, currentUser.getFull_name(), currentUser.getRole()));
                    //currentUser.getFull_name() + getResources().getString(R.string.open_parenthesis_space) + currentUser.getRole()  + getResources().getString(R.string.close_parenthesis_space));
                    user_email.setText(mUser.getEmail());

                }
            });
        } else {
            menu.findItem(R.id.nav_products).setVisible(false);
            menu.findItem(R.id.nav_messages).setVisible(false);
            menu.findItem(R.id.nav_my_cart).setVisible(false);
            menu.findItem(R.id.nav_orders).setVisible(false);
            menu.findItem(R.id.nav_logout).setVisible(false);
            menu.findItem(R.id.nav_purchase_history).setVisible(false);
            menu.findItem(R.id.nav_account_edit).setVisible(false);
        }


        /**
         * Getting ITEXMO API
         */
        mDatabase.collection(GlobalString.SETTINGS).document(GlobalString.ITEXMO).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){

                    try{
                        DocumentSnapshot document = task.getResult();
                        Helper.ITEXMO_API = (String) document.get(GlobalString.API);
                    }
                    catch (Exception ex){
                        Helper.ITEXMO_API = Helper.ITEXMO_API_CURRENT;
                    }

                }
                else{
                    try{
                        throw task.getException();
                    }
                    catch (Exception ex){
                        ex.printStackTrace();

                        Helper.ITEXMO_API = Helper.ITEXMO_API_CURRENT;
                    }
                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        if (mUser == null) {
            getMenuInflater().inflate(R.menu.main, menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.home_login) {

            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if(mUser != null){

            if (id == R.id.nav_home) {
                // Handle the camera action
                fragment_transaction = getSupportFragmentManager().beginTransaction();
                fragment_transaction.replace(fragment, main_menu);
                fragment_transaction.commit();

            }
            else if (id == R.id.nav_messages) {
                fragment_transaction = getSupportFragmentManager().beginTransaction();
                fragment_transaction.replace(fragment, messages);
                fragment_transaction.commit();

            }
            else if (id == R.id.nav_products) {
                fragment_transaction = getSupportFragmentManager().beginTransaction();
                fragment_transaction.replace(fragment, menu_products);
                fragment_transaction.commit();
            }
            else if(id == R.id.nav_my_cart){
                fragment_transaction = getSupportFragmentManager().beginTransaction();
                fragment_transaction.replace(fragment, menuMyCart);
                fragment_transaction.commit();
            }
            else if(id == R.id.nav_orders){
                fragment_transaction = getSupportFragmentManager().beginTransaction();
                fragment_transaction.replace(fragment, menuOrders);
                fragment_transaction.commit();
            }
            else if(id == R.id.nav_purchase_history){
                fragment_transaction = getSupportFragmentManager().beginTransaction();
                fragment_transaction.replace(fragment, menuPurchaseHistory);
                fragment_transaction.commit();
            }
            else if(id == R.id.nav_account_edit){
                Intent intent = new Intent(this, SignUpActivity.class);
                intent.putExtra("user", (Serializable) currentUser);
                startActivityForResult(intent, UPDATE_USER);
            }
            else if (id == R.id.nav_logout) {

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Logout");
                builder.setMessage("Continue Logout?");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAuth.signOut();
                        Intent intent = new Intent(cont, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        finish();
                        startActivity(intent);

                    }
                }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                builder.create().show();

            }


        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }


    private void requestPhoto(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, USER_PHOTO);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(requestCode == USER_PHOTO && resultCode == RESULT_OK && data.getData() != null){
            Log.d("USER_PHOTO", "Result is okay");

            final Uri filePath = data.getData();

            mUserPhoto = mStorage.child(GlobalString.USER + "/" + UUID.randomUUID().toString());
            UploadTask uploadTask = mUserPhoto.putFile(filePath);


            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {

                    if(!task.isSuccessful()){
                        throw task.getException();
                    }

                    return mUserPhoto.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {

                    if(task.isSuccessful()){

                        WriteBatch batch = mDatabase.batch();
                        DocumentReference ref = mDatabase.collection(GlobalString.USER).document(currentUser.getDocumentId());

                        batch.update(ref, "photo", task.getResult().toString());
                        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){

                                    try {
                                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                                        user_photo.setImageBitmap(bitmap);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.home_photo_upload_success), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                    else{
                        try{
                            throw task.getException();
                        }catch (Exception e){
                            e.getStackTrace();
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        }

                    }

                }
            });



        }
        else if(requestCode == UPDATE_USER && resultCode == RESULT_OK){
            currentUser = Helper.currentUser;

            user_full_name.setText(getString(R.string.home_full_name, currentUser.getFull_name(), currentUser.getRole()));
            user_email.setText(mUser.getEmail());
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){
            case PERMISSION_USER_PHOTO:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    requestPhoto();
                }
                break;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    class ChangePhoto implements View.OnClickListener {


        @Override
        public void onClick(View v) {

            AlertDialog.Builder builder = new AlertDialog.Builder(cont);
            builder.setTitle(getResources().getString(R.string.home_photo_title));
            builder.setMessage(getResources().getString(R.string.home_photo_message));
            builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_USER_PHOTO);
                        }else{
                            requestPhoto();
                        }
                    } else {
                        requestPhoto();
                    }

                }

            }).setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            builder.create().show();

        }

    }

}