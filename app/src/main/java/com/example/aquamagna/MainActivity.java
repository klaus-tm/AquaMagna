package com.example.aquamagna;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.color.DynamicColors;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private FrameLayout frameLayout;

    private boolean isShowBluetoothDialogCalled = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_main);

       DynamicColors.applyToActivitiesIfAvailable(this.getApplication());

       bottomNavigationView = findViewById(R.id.bottomNavView);
       frameLayout = findViewById(R.id.frameLayout);

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

    @Override
    protected void onResume() {
        super.onResume();
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frameLayout);
        if (fragment instanceof ScanFragment){

        } else if (fragment instanceof HomeFragment) {
            loadFragment(new HomeFragment(), false);
        } else {
            loadFragment(new ProfileFragment(), false);
        }
    }
}