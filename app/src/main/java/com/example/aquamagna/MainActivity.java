package com.example.aquamagna;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.elevation.SurfaceColors;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_main);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null){
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        }
       
       getWindow().setNavigationBarColor(SurfaceColors.SURFACE_2.getColor(this));

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavView);
        FrameLayout frameLayout = findViewById(R.id.frameLayout);

       bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
           @Override
           public boolean onNavigationItemSelected(@NonNull MenuItem item) {

               if (item.getItemId() == R.id.navHome){ //user selects to see the recent scans

                    loadFragment(new HomeFragment(), false);

               } else if (item.getItemId() == R.id.navScan){ //user selects to create a new scan

                   loadFragment(new ScanFragment(), false);

               } else { //user selects profile

                   loadFragment(new ProfileFragment(), false);

               }
               return true;
           }
       });

       if (savedInstanceState == null)
           loadFragment(new HomeFragment(), true);
    }

    private void loadFragment (Fragment fragment, boolean isAppInitialised){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if(isAppInitialised)
            fragmentTransaction.add(R.id.frameLayout, fragment);
        else
            fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();
    }
}