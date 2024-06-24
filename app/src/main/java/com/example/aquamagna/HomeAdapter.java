package com.example.aquamagna;

import static android.content.res.Configuration.UI_MODE_NIGHT_YES;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Geocoder;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.location.Address;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aquamagna.dataClasses.ScanData;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder> {

    private List<ScanData> scanDataList;
    private Context context;

    /**
     * constructor of the adapter. Gets scan data and context of the fragment
     * @param scanDataList
     * @param context
     */
    HomeAdapter(List<ScanData> scanDataList, Context context){
        this.scanDataList = scanDataList;
        this.context = context;
    }

    /**
     * Method which sets up what layout element is used to populate the recycler view
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_card, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Method which populates the elements from the layout element used in the recycler view
     * @param holder The ViewHolder which should be updated to represent the contents of the
     *        item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScanData scanData = scanDataList.get(position);
        String[] dateTime = scanData.getDate().split(", ");
        holder.dateRow.setText("Date: " + dateTime[0].replace(",", ""));
        holder.timeRow.setText("Time: " + dateTime[1]);
        holder.pHRow.setText("pH: " + scanData.getPh());
        holder.turbidityRow.setText("Turbidity: " + scanData.getTurbidity());
        holder.conductivityRow.setText("Conductivity: " + scanData.getConductivity());

        String[] parts = scanData.getLocation().split(",");
        float latitude = Float.parseFloat(parts[0].trim());
        float longitude = Float.parseFloat(parts[1].trim());

        String adress = getAddressFromCoordinates(context, latitude, longitude);
        holder.addressRow.setText(adress);

        checkValues(scanData, holder);
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            /**
             * Handler for the card press. It spawns the warning where the app will go to Google Maps
             * @param view
             */
            @Override
            public void onClick(View view) {
                MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(context);
                materialAlertDialogBuilder.setTitle("View location")
                        .setIcon(R.drawable.location)
                        .setMessage("You are about to exit the app in order to check the location of the scan. Do you wish to continue?")
                        .setPositiveButton("Let's go", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Uri gmmIntentUri = Uri.parse("geo:" + latitude + "," + longitude + "?q=" + latitude + "," + longitude);
                                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                                mapIntent.setPackage("com.google.android.apps.maps");
                                context.startActivity(mapIntent);
                            }
                        });
                materialAlertDialogBuilder.show();
            }
        });
    }

    /**
     * Method which gets the address name and number using a geocoder for the latitude and longitude provided
     * @param context
     * @param latitude
     * @param longitude
     * @return
     */
    private String getAddressFromCoordinates(Context context, float latitude, float longitude) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        String addressString = "";

        try{
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && addresses.size() > 0){
                Address address = addresses.get(0);
                String[] addressLine = address.getAddressLine(0).split(", ");
                addressString = addressLine[0];
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        return addressString;
    }

    /**
     * method used to check if the device theme is dark. Used for different colo schemes of the UI elements
     * @param context
     * @return true or false if the dark mode is enabled
     */
    private boolean isDark(Context context){
        int currentMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return currentMode == UI_MODE_NIGHT_YES;
    }

    /**
     * method used to put the scan data between the global standards and dictate the color scheme for the UI elements
     * @param scanData
     * @param holder
     */
    private void checkValues(ScanData scanData, ViewHolder holder) {
        float phThreshold = 7.5f;

        if (Math.abs(phThreshold - Float.parseFloat(scanData.getPh())) <= 1f && Float.parseFloat(scanData.getTurbidity()) <= 1f && Float.parseFloat(scanData.getConductivity()) <= 0.8f){
            //the water is within the standards. Make the card green
            if (isDark(context))
                setCard(holder, R.color.md_theme_dark_onSecondaryContainer, R.color.md_theme_dark_secondaryContainer);
            else
                setCard(holder, R.color.md_theme_light_onSecondaryContainer, R.color.md_theme_light_secondaryContainer);
        }
        else if (Math.abs(phThreshold - Float.parseFloat(scanData.getPh())) > 2f || Float.parseFloat(scanData.getTurbidity()) > 5f || Float.parseFloat(scanData.getConductivity()) > 2.5f){
            if (isDark(context))
                setCard(holder, R.color.md_theme_dark_onErrorContainer, R.color.md_theme_dark_errorContainer);
            else
                setCard(holder, R.color.md_theme_light_onErrorContainer, R.color.md_theme_light_errorContainer);
        }
        else {
            if (isDark(context))
                setCard(holder, R.color.md_theme_dark_onTertiaryContainer, R.color.md_theme_dark_tertiaryContainer);
            else
                setCard(holder, R.color.md_theme_light_onTertiaryContainer, R.color.md_theme_light_tertiaryContainer);
        }
    }

    /**
     * method which gives the layout elements color based on the standard color scheme shown before
     * @param holder
     * @param colorTextID
     * @param colorContainerID
     */
    private void setCard(ViewHolder holder, int colorTextID, int colorContainerID) {
        int colorText = ContextCompat.getColor(holder.itemView.getContext(), colorTextID);
        int colorContainer = ContextCompat.getColor(holder.itemView.getContext(), colorContainerID);

        holder.addressRow.setTextColor(colorText);
        holder.dateRow.setTextColor(colorText);
        holder.timeRow.setTextColor(colorText);
        holder.pHRow.setTextColor(colorText);
        holder.turbidityRow.setTextColor(colorText);
        holder.conductivityRow.setTextColor(colorText);
        holder.cardView.setCardBackgroundColor(colorContainer);
    }

    /**
     * method which returns how many layout elements need to be created in the recycler view
     * @return
     */
    @Override
    public int getItemCount() {
        return scanDataList.size();
    }

    /**
     * class which gets the UI elements of the layout used to populate recycler view,in order to be used to insert the data from the received list
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView addressRow;
        TextView dateRow;
        TextView timeRow;
        TextView pHRow;
        TextView turbidityRow;
        TextView conductivityRow;
        MaterialCardView cardView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            addressRow = itemView.findViewById(R.id.addressRow);
            dateRow = itemView.findViewById(R.id.dateRow);
            timeRow = itemView.findViewById(R.id.timeRow);
            pHRow = itemView.findViewById(R.id.pHRow);
            turbidityRow = itemView.findViewById(R.id.turbidityRow);
            conductivityRow = itemView.findViewById(R.id.conductivityRow);
            cardView = itemView.findViewById(R.id.list_card);
        }
    }
}
