package com.fameg.petrolbrain_android.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.fameg.petrolbrain_android.R;

public class PlacePhotoFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_place_photo, container, false);
        ImageView imagem = (ImageView) rootView.findViewById(R.id.fotoDoLocal);
        Bitmap bitmap = getArguments().getParcelable("IMAGEM");
        imagem.setImageBitmap(Bitmap.createBitmap(bitmap));
        return rootView;
    }
}
