package com.daclink.gymlog_v_sp22;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.daclink.gymlog_v_sp22.DB.AppDataBase;
import com.daclink.gymlog_v_sp22.DB.GymLogDAO;
import com.daclink.gymlog_v_sp22.databinding.ActivityMainBinding;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String USER_ID_KEY = "com.daclink.gymlog_v_sp22.userIdKey";
    private static final String PREFENCES_KEY = "com.daclink.gymlog_v_sp22.PREFENCES_KEY";
    private ActivityMainBinding binding;

    private TextView mMainDisplay;
    private EditText mExercise;
    private EditText mWeight;
    private EditText mReps;

    private Button mSubmitButton;
    private GymLogDAO mGymLogDAO;

    private List<GymLog> mGymLogList;

    private int mUserId = -1;

    private SharedPreferences  mPreferences = null;
    private User mUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getDatabase();
        checkForUser();
        addUserToPreference(mUserId);
        loginUser(mUserId);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mMainDisplay = binding.mainGymLogDisplay;
        mExercise = binding.mainExerciseEditText;
        mWeight = binding.mainWeightEditText;
        mReps = binding.mainRepsEditText;
        mSubmitButton = binding.mainSubmitButton;

        mMainDisplay.setMovementMethod(new ScrollingMovementMethod());



        refreshDisplay();

        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                refreshDisplay();
                submitGymLog();
            }
        });
    }

    private void loginUser(int userId) {
        mUser = mGymLogDAO.getUserByUserId(userId);
        invalidateOptionsMenu();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(mUser != null){

            MenuItem item = menu.findItem(R.id.item1);
            item.setTitle(mUser.getUserName());
        }
        return super.onPrepareOptionsMenu(menu);
    }

    private void addUserToPreference(int userId) {
        if(mPreferences == null){
            getPrefs();
        }
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(USER_ID_KEY, userId);
    }

    private void getDatabase(){
        mGymLogDAO = Room.databaseBuilder(this, AppDataBase.class, AppDataBase.DATABASE_NAME)
                .allowMainThreadQueries()
                .build()
                .GymLogDAO();

    }
    private void checkForUser() {
        mUserId = getIntent().getIntExtra(USER_ID_KEY, -1);
        //do we have a user in the preferences
        if(mUserId != -1){
            return;
        }

        if(mPreferences == null){
            getPrefs();
        }
        mUserId = mPreferences.getInt(USER_ID_KEY, -1);



        if(mUserId != -1){
            return;
        }

        List<User> users = mGymLogDAO.getAllUsers();
        if(users.size() <= 0){
            User defaultUser = new User("daclink", "dac123");
            User altUser = new User("drew", "dac123");

            mGymLogDAO.insert(defaultUser,altUser);

        }

        Intent intent = LoginActivity.intentFactory(this);
        startActivity(intent);


    }

    private void getPrefs() {
        mPreferences = this.getSharedPreferences(PREFENCES_KEY,Context.MODE_PRIVATE);
    }

    private void logoutUser(){
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setMessage(R.string.logout);

        alertBuilder.setPositiveButton(getString(R.string.yes),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        clearUserFromIntent();
                        clearUserFromPref();
                        mUserId = -1;
                        checkForUser();
                    }


                });
//        alertBuilder.setNegativeButton(getString(R.string.no),
//                new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//
//
//                    }
//                });
        alertBuilder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //dont need anything
            }
        });
        alertBuilder.create().show();
    }
    private void clearUserFromIntent(){
        getIntent().putExtra(USER_ID_KEY, -1);
    }
    private void clearUserFromPref() {
        addUserToPreference(-1);
    }
    private GymLog getValuesFromDisplay(){
        String exercise = "No records found";
        double weight = 0.0;
        int reps = 0;
        exercise = mExercise.getText().toString();
        try {
            weight = Double.parseDouble(mWeight.getText().toString());

        }catch (NumberFormatException e){
            Log.d("GYMLOG", "Could't convert weight");

        }

        try {
            reps = Integer.parseInt(mReps.getText().toString());

        }catch (NumberFormatException e){
            Log.d("GYMLOG", "Coudn't convert reps");
        }

        GymLog log = new GymLog(exercise, reps, weight, mUserId);

        return log;

    }


    private void submitGymLog(){
         String exercise = mExercise.getText().toString();
         double weight = Double.parseDouble(mWeight.getText().toString());
         int reps = Integer.parseInt(mReps.getText().toString());

         GymLog log = new GymLog(exercise, reps, weight, mUserId);
         mGymLogDAO.insert(log);
    }

    private void refreshDisplay(){
        mGymLogList = mGymLogDAO.getGymLogsByUserId(mUserId);
        if(! mGymLogList.isEmpty()){
            StringBuilder sb = new StringBuilder();
            for(GymLog log: mGymLogList){
                sb.append(log.toString());
            }
            mMainDisplay.setText(sb.toString());
        }else{
            mMainDisplay.setText(R.string.no_logs_message);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.example_menu,menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.item1:
                logoutUser();
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    public static Intent intentFactory(Context context,int userId){
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(USER_ID_KEY, userId);
        return intent;
    }
}