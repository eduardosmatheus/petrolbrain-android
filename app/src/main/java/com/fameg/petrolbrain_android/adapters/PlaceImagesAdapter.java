package com.fameg.petrolbrain_android.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcel;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.fameg.petrolbrain_android.fragments.PlacePhotoFragment;

import java.io.Serializable;
import java.util.List;

public class PlaceImagesAdapter extends FragmentPagerAdapter {

    protected Context context;
    private List<Bitmap> bitmaps;

    public PlaceImagesAdapter(FragmentManager fm, Context c, List<Bitmap> data) {
        super(fm);
        this.context = c;
        this.bitmaps = data;
    }

    @Override
    public Fragment getItem(int position) {
        PlacePhotoFragment fr = new PlacePhotoFragment();
        Bundle bundle = new Bundle();

        Bitmap bitmap = bitmaps.get(position);
        bundle.putParcelable("IMAGEM", bitmap);
        fr.setArguments(bundle);
        return fr;
    }

    @Override
    public int getCount() {
        return bitmaps.size();
    }
}
