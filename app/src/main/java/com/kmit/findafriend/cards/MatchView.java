package com.kmit.findafriend.cards;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.github.islamkhsh.CardSliderAdapter;
import com.kmit.findafriend.R;
import com.kmit.findafriend.core.Dashboard;

import java.util.ArrayList;

public class MatchView extends CardSliderAdapter<MatchView.UserCard> {

    private ArrayList<Match> matches;



    public MatchView(ArrayList<Match> matches){
        this.matches = matches;
    }

    @Override
    public int getItemCount(){
        return matches.size();
    }

    @Override
    public UserCard onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_page, parent, false);


        return new UserCard(view);
    }
    private void alert(String title, String message, Context context){
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        message=message.replace(",","\n\n");
        message=message.replace("[","");
        message=message.replace("]","");
        alert.setTitle(title);
        alert.setMessage(message);
        alert.show();
    }
    @Override
    public void bindVH(UserCard movieViewHolder, int i) {
        try {
            View view=movieViewHolder.m;
            int viewType=i;
            TextView personName = view.findViewById(R.id.personname);
            personName.setText(this.matches.get(viewType).personname);
            TextView personHobbies = view.findViewById(R.id.personHobbies);
            personHobbies.setText(this.matches.get(viewType).othersHobbies.toString());
            TextView yourHobbies = view.findViewById(R.id.yourhobbies);
            yourHobbies.setText(this.matches.get(viewType).yourhobbies.toString());
            TextView similarHobbies = view.findViewById(R.id.similarhobbies);
            similarHobbies.setText(this.matches.get(viewType).similarhobbies.toString());
            TextView messagesuggestion=view.findViewById(R.id.messagesuggestion);

            ImageView user_image=view.findViewById(R.id.personImage);

            user_image.setImageBitmap(this.matches.get(viewType).bm);
            String s[] = (String[]) this.matches.get(viewType).similarhobbies.toArray(new String[this.matches.get(viewType).similarhobbies.size()]);
            messagesuggestion.setText(this.matches.get(viewType).convos.toString());
            Button suggestbutton=view.findViewById(R.id.suggestbutton);
            suggestbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alert("Suggested Message",messagesuggestion.getText().toString(),view.getContext());
                }
            });
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }



    class UserCard extends RecyclerView.ViewHolder {
        public View m;
        public UserCard(View view){
            super(view);
            this.m=view;
        }
    }
}
