package com.fameg.petrolbrain_android.adapters;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fameg.petrolbrain_android.R;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.util.List;

public class CommentAdapter extends ArrayAdapter<String> {

    public CommentAdapter(Context context, int resource, List<String> data) {
        super(context, resource, data);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        String item = getItem(position);
        LayoutInflater infl = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = infl.inflate(R.layout.comment_item, null);
        TextView descrComentario = (TextView) v.findViewById(R.id.textoComentario);
        descrComentario.setTextColor(Color.BLACK);
        descrComentario.setText(item);
        return v;
    }
}
