package xyz.genscode.type.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import xyz.genscode.type.fragments.ContactsFragment;

public class Contacts extends ContactsFragment {

    public static List<Contact> getContacts(Context context) {

        List<Contact> contacts = new ArrayList<>();

        // Получаем контент-резолвер для доступа к контактам
        ContentResolver cr = context.getContentResolver();

        // Получаем курсор, содержащий данные о контактах
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

        if (cursor != null && cursor.getCount() > 0) {

            while (cursor.moveToNext()) {
                String id = cursor.getString((int) cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cursor.getString((int) cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                if (Integer.parseInt(cursor.getString((int) cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor phoneCursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);

                    while (phoneCursor.moveToNext()) {
                        String phoneNumber = phoneCursor.getString((int) phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        contacts.add(new Contact(name, phoneNumber));
                    }
                    phoneCursor.close();
                }
            }
            cursor.close();

        }

        return filterContacts(contacts);
    }

    private static List<Contact> filterContacts(List<Contact> list){
        List<Contact> filteredContacts = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Contact contact = list.get(i);
            String phone = contact.getPhoneNumber().replaceAll("\\D", "");
            if(String.valueOf(phone.charAt(0)).equals("8") ||
                    String.valueOf(phone.charAt(0)).equals("7")){
                String firstCode = "+7";
                phone = phone.substring(1);
                phone = firstCode + phone;
            }
            String finalPhone = phone;
            if(PhoneNumberUtils.isGlobalPhoneNumber(finalPhone) && finalPhone.length() >= 10) {
                contact.setPhoneNumber(finalPhone);
                filteredContacts.add(contact);
            }
        }
        return filteredContacts;
    }

    public static class Contact {
        public String name;
        public String phoneNumber;

        public Contact(String name, String phoneNumber) {
            this.name = name;
            this.phoneNumber = phoneNumber;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }
    }
}
