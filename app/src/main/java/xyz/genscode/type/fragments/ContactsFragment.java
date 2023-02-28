package xyz.genscode.type.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import xyz.genscode.type.ChatActivity;
import xyz.genscode.type.EditActivity;
import xyz.genscode.type.R;


public class ContactsFragment extends Fragment {

    View view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_contacts, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View llStartDialog = view.findViewById(R.id.llContactsStartDialog);
        EditText etSearch = view.findViewById(R.id.etContactsSearch);
        Button btStartDialog = view.findViewById(R.id.btContactsStartDialog);

        etSearch.setFilters(new InputFilter[]{new InputFilter.LengthFilter(32)});
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(PhoneNumberUtils.isGlobalPhoneNumber(charSequence.toString()) && charSequence.length() >= 10){
                    llStartDialog.setVisibility(View.VISIBLE);
                    btStartDialog.setText(charSequence);
                }else{
                    llStartDialog.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        btStartDialog.setOnClickListener(view1 -> {
            Intent intent = new Intent(getContext(), ChatActivity.class);
            intent.putExtra("chatId", "-NPFMZOnUuSGRTo7x28s");
            intent.putExtra("currentUserId", "XAf0nSdiwwPPtbMddF6OFgb5eRM2");
            intent.putExtra("id", "taCiXupiJFdxTv3310AGOuHOW8t2");
            intent.putExtra("name", "Калама Евлампий");
            startActivity(intent);
        });
    }
}