package com.kmit.findafriend.cards;

import android.graphics.Bitmap;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Match {
    public String personname;
    public List<String> yourhobbies;
    public List<String> othersHobbies;
    public Collection<String> similarhobbies;
    public ArrayList<String> convos;
    public Bitmap bm;
    public Match(String personname, List<String> yourhobbies, List<String> othersHobbies, Collection<String> similarhobbies, Bitmap bm,ArrayList<String> convos){
        this.personname=personname;
        this.yourhobbies=yourhobbies;
        this.othersHobbies=othersHobbies;
        this.similarhobbies=similarhobbies;
        this.convos=convos;
        this.bm=bm;
    }
}
