package xyz.genscode.type.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.provider.Settings;
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

import java.util.ArrayList;
import java.util.List;

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
    static List<Contacts.Contact> contacts = new ArrayList<>();
    static ArrayList<Contacts.Contact> searchContacts = new ArrayList<>();
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
            assert getContext() != null;
            ((MainActivity) getContext()).navigate("messenger");
            return;
        }

        //Запуск анимации для loading
        Handler handler = new Handler();
        Animation anim = new AlphaAnimation(0.5f, 1.0f); anim.setDuration(250); anim.setRepeatMode(Animation.REVERSE); anim.setRepeatCount(Animation.INFINITE);
        Animation anim2 = new AlphaAnimation(0.5f, 1.0f); anim2.setDuration(250); anim2.setRepeatMode(Animation.REVERSE); anim2.setRepeatCount(Animation.INFINITE);
        Animation anim3 = new AlphaAnimation(0.5f, 1.0f); anim3.setDuration(250); anim3.setRepeatMode(Animation.REVERSE); anim3.setRepeatCount(Animation.INFINITE);
        ImageView loading1 = view.findViewById(R.id.ivContactsLoading1); loading1.startAnimation(anim);
        ImageView loading2 = view.findViewById(R.id.ivContactsLoading2); handler.postDelayed(() -> loading2.startAnimation(anim2), 125);
        ImageView loading3 = view.findViewById(R.id.ivContactsLoading3); handler.postDelayed(() -> loading3.startAnimation(anim3), 250);
        llLoading = view.findViewById(R.id.llContactsLoading);

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
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(PhoneNumberUtils.isGlobalPhoneNumber(charSequence.toString())){ //Похож ли, введенный в поиск текст на номер телефона

                    //Если номер длиннее 10 чисел, показываем кнопку глобального поиска по номеру
                    if(charSequence.length() >= 10) {
                        llStartDialog.setVisibility(View.VISIBLE);
                        btStartDialog.setText(charSequence);
                    }

                    searchByNumber(charSequence.toString().trim());
                }else{
                    llStartDialog.setVisibility(View.GONE);

                    searchByName(charSequence.toString().trim());
                }

                if(charSequence.length() == 0){
                    loadContacts(); //Если в поиске ничего нету, загружаем полный список контактов
                }
            }

            @Override public void afterTextChanged(Editable editable) {}
            @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
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

    //Ищем контакт по номеру
    private void searchByNumber(String searchNumber){
        if(contacts.size() > 0){
            searchContacts.clear();

            for (Contacts.Contact contact:contacts) {
                String contactNumber = contact.getPhoneNumber();
                if(contactNumber.contains(searchNumber)){
                    searchContacts.add(contact);
                }
            }

            loadContacts(true); //true, значит в адаптер передастся searchContacts список

        }
    }

    //Ищем по имени
    private void searchByName(String searchName){
        if(contacts.size() > 0){
            searchContacts.clear();

            for (Contacts.Contact contact:contacts) {
                String contactName = contact.getName().toLowerCase();
                if(contactName.contains(searchName.toLowerCase())){
                    searchContacts.add(contact);
                }
            }

            loadContacts(true); //true, значит в адаптер передастся searchContacts список

        }
    }

    //Создание списка contacts
    public void getContacts(){
        if(contacts.size() == 0) { //контакты еще не загружены
            assert getContext() != null;
            int permissionStatus = ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.READ_CONTACTS);

            if (permissionStatus == PackageManager.PERMISSION_GRANTED) { //Есть ли разрешение на чтение контактов?
                Thread thr = new Thread(() -> {
                    contacts = Contacts.getContacts(view.getContext()); //Обращаемся к Utils.Contacts для получения всех валидных и форматированных контактов

                    assert getActivity() != null;
                    getActivity().runOnUiThread(this::loadContacts); //Отображаем контакты из списка
                });
                thr.start();

            } else { //разрешения на чтение контактов нету

                if (shouldShowRequestPermissionRationale(
                        Manifest.permission.READ_CONTACTS)) { //Можем ли мы запросить окно с разрешением?
                    assert getActivity() != null;
                    ((MainActivity) getActivity()).dialog.showMessage(
                            getResources().getString(R.string.contacts_permission_header),
                            getResources().getString(R.string.contacts_permission_content),
                            getResources().getString(R.string.contacts_permission_allow),
                            getResources().getString(R.string.contacts_permission_back)
                    );
                    Button messageButton1 = ((MainActivity) getActivity()).dialog.getMessageButton1();
                    Button messageButton2 = ((MainActivity) getActivity()).dialog.getMessageButton2();

                    messageButton1.setOnClickListener(view -> {
                        requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSION_READ_CONTACTS);
                        ((MainActivity) getActivity()).dialog.hideMessage();
                    });
                    messageButton2.setOnClickListener(view -> {
                        loadContacts();
                        ((MainActivity) getActivity()).dialog.hideMessage();
                    });
                }else{ //Пользователь не соглашается на чтение контактов, оставляем текст об этом.
                    onPermissionDenied();
                }
            }
        }else{
            loadContacts();
        }
    }

    //Отображение контактов из arraylist contacts или searchContacts
    public void loadContacts(){
        loadContacts(false);
    }

    public void loadContacts(boolean isSearch){ //isSearch, значит в адаптер передастся searchContacts список, иначе все контакты
        TextView tvNoContacts = view.findViewById(R.id.tvContactsNoContacts); //tv Не найден контакт или у вас нету контактов
        tvNoContacts.setVisibility(View.GONE);

        llLoading.setVisibility(View.VISIBLE);
        if (!isSearch) {
            ContactsAdapter contactsAdapter = new ContactsAdapter(view.getContext(), contacts, mUser); //Создаем адаптер со всеми контактами
            rvContactsList.setAdapter(contactsAdapter);

            llLoading.setVisibility(View.GONE);

            if (contacts.size() == 0){
                //Нету контактов
                tvNoContacts = view.findViewById(R.id.tvContactsNoContacts);
                tvNoContacts.setText(getResources().getString(R.string.contacts_no_contacts));
                tvNoContacts.setVisibility(View.VISIBLE);
            }
        }else{
            ContactsAdapter contactsAdapter = new ContactsAdapter(view.getContext(), searchContacts, mUser); //Создаем адаптер с найденными контактами
            rvContactsList.setAdapter(contactsAdapter);

            llLoading.setVisibility(View.GONE);

            if (searchContacts.size() == 0){
                //Нету найденных контактов
                tvNoContacts.setText(getResources().getString(R.string.contacts_zero_find_contacts));
                tvNoContacts.setVisibility(View.VISIBLE);
            }
        }
    }

    //Получаем ответ на запрос чтения контактов
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_READ_CONTACTS:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // разрешение есть
                    getContacts();
                } else {
                    // разрешения нет
                    onPermissionDenied();
                }
                break;
        }
    }

    //Запрещено читать контакты
    private void onPermissionDenied(){
        //Устанавливаем кнопку для ручного разрешения на чтение контактов
        TextView tvNoContacts = view.findViewById(R.id.tvContactsNoContacts);
        tvNoContacts.setText(getResources().getString(R.string.contacts_permission_denied));
        tvNoContacts.setVisibility(View.VISIBLE);
        tvNoContacts.setOnClickListener(view -> {
            assert getActivity() != null;
            ((MainActivity) getActivity()).dialog.showMessage(
                    getResources().getString(R.string.contacts_permission_header),
                    getResources().getString(R.string.contacts_permission_content),
                    getResources().getString(R.string.contacts_permission_allow),
                    getResources().getString(R.string.contacts_permission_back)
            );
            Button messageButton1 = ((MainActivity) getActivity()).dialog.getMessageButton1();
            Button messageButton2 = ((MainActivity) getActivity()).dialog.getMessageButton2();

            messageButton1.setOnClickListener(view1 -> {
                ((MainActivity) getActivity()).dialog.hideMessage();

                if(shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)){
                    requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSION_READ_CONTACTS);
                }else{
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                }
            });
            messageButton2.setOnClickListener(view1 -> {
                loadContacts();
                ((MainActivity) getActivity()).dialog.hideMessage();
            });
        });
        llLoading.setVisibility(View.GONE);
    }

}