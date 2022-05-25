package com.kmit.findafriend.chip;

import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.pchmn.materialchips.model.ChipInterface;

public class HobbyChip implements ChipInterface {
    String label;
    public HobbyChip(String label){
        this.label=label;
    }

    @Override
    public Object getId() {
        return this;
    }

    @Override
    public Uri getAvatarUri() {
        return null;
    }

    @Override
    public Drawable getAvatarDrawable() {
        return null;
    }

    @Override
    public String getLabel() {
        return this.label;
    }

    @Override
    public String getInfo() {
        return null;
    }
}
