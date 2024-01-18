package com.zio.idfort;


import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.zio.idfort.data.DocsEntity;
import com.zio.idfort.utils.Constants;

import java.io.File;
import java.util.List;

public class AdapterFiles extends RecyclerView.Adapter<AdapterFiles.ViewHolder> {

    Context context;
    List<DocsEntity> FilesList;

    public AdapterFiles(Context context, List<DocsEntity> filesList) {
        this.context = context;
        FilesList = filesList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.layout_file, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DocsEntity model = FilesList.get(position);
        holder.title.setText(model.getDocument_name());

        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDetails(model.getId(), model.getName(), model.getDocument_name());
            }
        });

    }

    private Uri geturi(String fileName) {
        Uri fileUri = null;
        File requestFile = new File(context.getFilesDir(), fileName);
        try {
            fileUri = FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".fileprovider",
                    requestFile);
        } catch (IllegalArgumentException e) {
            Log.d(Constants.TAG, "Couldn't get the file");
        }
        return fileUri;
    }

    private void openDetails(String id, String p_name, String document_name) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(R.layout.popup_details);

        //LinearLayout copy = bottomSheetDialog.findViewById(R.id.copyLinearLayout);
        TextView id_view = bottomSheetDialog.findViewById(R.id.document_id);
        TextView name_view = bottomSheetDialog.findViewById(R.id.document_p_name);
        MaterialButton open_button = bottomSheetDialog.findViewById(R.id.document_open);
        MaterialButton share_button = bottomSheetDialog.findViewById(R.id.document_share);

        id_view.setText(id);
        name_view.setText(p_name);

        open_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri fileUri = geturi(document_name + ".jpg");
                if (fileUri != null) {
                    send_doc(true, fileUri);
                } else Toast.makeText(context, "something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
        share_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Uri fileUri = geturi(document_name + ".jpg");
                if (fileUri != null) {
                    Log.d(Constants.TAG, fileUri.toString());
                    send_doc(false, fileUri);
                } else Toast.makeText(context, "something went wrong", Toast.LENGTH_SHORT).show();
            }
        });

        bottomSheetDialog.show();
    }

    private void sendit(Uri fileUri) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/jpg");
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        shareIntent.setData(fileUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        context.startActivity(Intent.createChooser(shareIntent, "Share PDF"));
    }

    private void send_doc(boolean type, Uri fileUri) {

        Intent sendIntent = new Intent();

        sendIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        sendIntent.setDataAndType(
                fileUri,
                context.getContentResolver().getType(fileUri));
        sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        if (type) sendIntent.setAction(Intent.ACTION_VIEW);
        else sendIntent.setAction(Intent.ACTION_SEND);

        // Try to invoke the intent.
        try {
            context.startActivity(Intent.createChooser(sendIntent, type ? "open/view" : "Share"));
            //context.startActivity(sendIntent);
        } catch (ActivityNotFoundException e) {
            // Define what your app should do if no activity can handle the intent.
        }
    }

    @Override
    public int getItemCount() {
        return FilesList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView title;
        public ImageButton share;
        public CardView card;

        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            share = itemView.findViewById(R.id.sharebtn);
            card = itemView.findViewById(R.id.card);
        }
    }
}
