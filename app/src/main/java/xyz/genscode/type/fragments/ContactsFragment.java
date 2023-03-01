package xyz.genscode.type.fragments;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import xyz.genscode.type.ChatActivity;
import xyz.genscode.type.MainActivity;
import xyz.genscode.type.ProfileActivity;
import xyz.genscode.type.R;
import xyz.genscode.type.data.ContactsAdapter;
import xyz.genscode.type.models.User;
import xyz.genscode.type.utils.Contacts;


public class ContactsFragment extends Fragment {
    public static final String ARG_USER = "arg_user";
    public static final int PERMISSION_READ_CONTACTS = 0;
    View view, llLoading;
    User mUser;
    static List<Contacts.Contact> contacts;
    RecyclerView rvContactsList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUser = (User) getArguments().getSerializable(ARG_USER);
        }
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

        if(mUser == null) {
            ((MainActivity) getContext()).navigate("messenger");
            return;
        }

        Handler handler = new Handler();
        Animation anim = new AlphaAnimation(0.5f, 1.0f); anim.setDuration(250); anim.setRepeatMode(Animation.REVERSE); anim.setRepeatCount(Animation.INFINITE);
        Animation anim2 = new AlphaAnimation(0.5f, 1.0f); anim2.setDuration(250); anim2.setRepeatMode(Animation.REVERSE); anim2.setRepeatCount(Animation.INFINITE);
        Animation anim3 = new AlphaAnimation(0.5f, 1.0f); anim3.setDuration(250); anim3.setRepeatMode(Animation.REVERSE); anim3.setRepeatCount(Animation.INFINITE);
        ImageView loading1 = view.findViewById(R.id.ivContactsLoading1); loading1.startAnimation(anim);
        ImageView loading2 = view.findViewById(R.id.ivContactsLoading2); handler.postDelayed(() -> loading2.startAnimation(anim2), 125);
        ImageView loading3 = view.findViewById(R.id.ivContactsLoading3); handler.postDelayed(() -> loading3.startAnimation(anim3), 250);
        llLoading = view.findViewById(R.id.llContactsLoading);
        llLoading.setVisibility(View.VISIBLE);

        View llStartDialog = view.findViewById(R.id.llContactsStartDialog);
        EditText etSearch = view.findViewById(R.id.etContactsSearch);
        Button btStartDialog = view.findViewById(R.id.btContactsStartDialog);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(view.getContext());
        linearLayoutManager.setSmoothScrollbarEnabled(true);
        rvContactsList = view.findViewById(R.id.rvContactsList);
        rvContactsList.setLayoutManager(linearLayoutManager);

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
            Intent intent = new Intent(getContext(), ProfileActivity.class);
            String phone = etSearch.getText().toString().replaceAll("\\D", "");
            if(String.valueOf(phone.charAt(0)).equals("8") ||
                    String.valueOf(phone.charAt(0)).equals("7")){
                String firstCode = "+7";
                phone = phone.substring(1);
                phone = firstCode + phone;
            }
            String finalPhone = phone;
            intent.putExtra("phone", finalPhone);
            intent.putExtra("currentUser", mUser);
            startActivity(intent);
        });

        getContacts();

    }

    public void getContacts(){
        if(contacts == null) {

            int permissionStatus = ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.READ_CONTACTS);

            if (permissionStatus == PackageManager.PERMISSION_GRANTED) {

                contacts = Contacts.getContacts(view.getContext());
                loadContacts();

            } else {
                ActivityCompat.requestPermissions(((MainActivity) getContext()), new String[]{android.Manifest.permission.READ_CONTACTS},
                        PERMISSION_READ_CONTACTS);
            }
        }else{
            loadContacts();
        }
    }

    public void loadContacts(){
        if (contacts.size() > 0) {
            ContactsAdapter contactsAdapter = new ContactsAdapter(view.getContext(), contacts, mUser);
            rvContactsList.setAdapter(contactsAdapter);
            llLoading.setVisibility(View.GONE);
        } else {
            TextView tvNoContacts = view.findViewById(R.id.tvContactsNoContacts);
            tvNoContacts.setVisibility(View.VISIBLE);
            llLoading.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_READ_CONTACTS:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission granted
                    getContacts();
                } else {
                    // permission denied
                    ((MainActivity) getContext()).navigate("messenger");
                }
                return;
        }
    }
}