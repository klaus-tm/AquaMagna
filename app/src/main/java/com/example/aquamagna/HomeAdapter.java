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

    HomeAdapter(List<ScanData> scanDataList, Context context){
        this.scanDataList = scanDataList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScanData scanData = scanDataList.get(position);
        String[] dateTime = scanData.getDate().split(", ");
        holder.dateRow.setText("Date: " + dateTime[0].replace(",", ""));
        holder.timeRow.setText("Time: " + dateTime[1]);
        holder.pHRow.setText("pH: " + scanData.getPh());
        holder.turbidityRow.setText("Turbidity: " + scanData.getTurbidity());
        holder.conductivityRow.setText("Conductivity: " + scanData.getConductivity());

        String[] parts = scanData.getLocation().split(", ");
        String latitudeString = parts[0].split(": ")[1];
        String longitudeString = parts[1].split(": ")[1];

        float latitude = Float.parseFloat(latitudeString);
        float longitude = Float.parseFloat(longitudeString);

        String adress = getAddressFromCoordinates(context, latitude, longitude);
        holder.addressRow.setText(adress);

        checkValues(scanData, holder);
        holder.cardView.setOnClickListener(new View.OnClickListener() {
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

    private boolean isDark(Context context){
        int currentMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return currentMode == UI_MODE_NIGHT_YES;
    }

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

    @Override
    public int getItemCount() {
        return scanDataList.size();
    }

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
