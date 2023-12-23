package com.seven.zion.blinknotifier;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class dialogBox extends DialogFragment {

    RadioButton bu1,bu2,bu3;
    EditText custom;
    Button okButton;
    RadioGroup group;
    SharedPreferences preferences;
    optionListener listener;
    String TAG;
    public dialogBox(){

    }

    public interface optionListener{
       void onOptionChanged(String tag);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_resource,container);
        bu1 = (RadioButton)view.findViewById(R.id.but1);
        bu2 = (RadioButton)view.findViewById(R.id.but2);
        bu3 = (RadioButton)view.findViewById(R.id.but3);
        custom = (EditText)view.findViewById(R.id.custom_text);
        okButton =(Button)view.findViewById(R.id.ok_button);
        group = (RadioGroup)view.findViewById(R.id.radioGroup);
        RadioButton button = view.findViewById(group.getCheckedRadioButtonId());
        listener = (optionListener)getActivity();
        preferences = getActivity().getApplicationContext().getSharedPreferences("notifeye",0);
        final String option,type =getArguments().getString("Type");
        TAG = type;
        switch (type)
        {
            case "notifeye":
                bu1.setText(R.string.n);
                bu2.setText(R.string.real);
                bu3.setText(R.string.notify_only);
                custom.setVisibility(View.GONE);
                option = preferences.getString("notifeye","Normal");
                if (option.equals("Normal"))
                    bu1.setChecked(true);
                else if (option.equals("Real Time Detection(Beta)"))
                    bu2.setChecked(true);
                else
                    bu3.setChecked(true);
                break;
            case "duration":
                bu1.setText(R.string.ten);
                bu2.setText(R.string.twenty);
                bu3.setText(R.string.thirty);
                option = preferences.getString("duration",getString(R.string.twenty));
                if (option.equals(getString(R.string.ten)))
                    bu1.setChecked(true);
                else if(option.equals(getString(R.string.twenty)))
                    bu2.setChecked(true);
                else
                    bu3.setChecked(true);
                break;
            case "noOfBlinks":
                bu1.setText(R.string.tenB);
                bu2.setText(R.string.fifteenB);
                bu3.setText(R.string.twentyB);
                option = preferences.getString("noOfBlinks",getString(R.string.tenB));
                if (option.equals(getString(R.string.tenB)))
                    bu1.setChecked(true);
                else if(option.equals(getString(R.string.twentyB)))
                    bu3.setChecked(true);
                else
                    bu2.setChecked(true);
                break;
        }
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (type)
                {
                    case "notifeye":
                        SharedPreferences.Editor editor = preferences.edit();
                        if (group.getCheckedRadioButtonId()==R.id.but1)
                            editor.putString("notifeye","Normal");
                        else if (group.getCheckedRadioButtonId()==R.id.but2)
                            editor.putString("notifeye","Real Time Detection(Beta)");
                        else
                            editor.putString("notifeye","Notification Only");

                        editor.commit();
                        listener.onOptionChanged(TAG);
                        dismiss();
                        break;
                    case "noOfBlinks":
                        SharedPreferences.Editor editor2 = preferences.edit();
                        if (!TextUtils.isEmpty(custom.getText().toString())) {
                            if (Integer.parseInt(custom.getText().toString())<7)
                            {
                                Toast.makeText(getActivity(),"value can't be less than 7",Toast.LENGTH_LONG).show();
                                break;
                            }
                            editor2.putString("noOfBlinks", custom.getText().toString() + " Blinks");
                        }
                        else if (group.getCheckedRadioButtonId()==R.id.but1)
                            editor2.putString("noOfBlinks",getString(R.string.tenB));
                        else if (group.getCheckedRadioButtonId() == R.id.but2)
                            editor2.putString("noOfBlinks",getString(R.string.fifteenB));
                        else
                            editor2.putString("noOfBlinks",getString(R.string.twentyB));

                        editor2.commit();
                        listener.onOptionChanged(TAG);
                        dismiss();
                        break;

                    case "duration":
                        SharedPreferences.Editor editor3 = preferences.edit();
                        if (!TextUtils.isEmpty(custom.getText().toString())) {
                            if (Integer.parseInt(custom.getText().toString())<5){
                                Toast.makeText(getActivity(),"Value can't be less than 5",Toast.LENGTH_LONG).show();
                                break;
                            }
                            editor3.putString("duration", custom.getText().toString() + " Minutes");
                        }
                       else if (group.getCheckedRadioButtonId()==R.id.but1)
                            editor3.putString("duration",getString(R.string.ten));
                        else if (group.getCheckedRadioButtonId() == R.id.but2)
                            editor3.putString("duration",getString(R.string.twenty));
                        else
                            editor3.putString("duration",getString(R.string.thirty));

                        editor3.commit();
                        listener.onOptionChanged(TAG);
                        dismiss();
                        break;

                }
            }
        });
        return view;

    }
}
