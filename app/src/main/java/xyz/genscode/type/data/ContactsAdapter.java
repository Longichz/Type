package xyz.genscode.type.data;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import xyz.genscode.type.ProfileActivity;
import xyz.genscode.type.R;
import xyz.genscode.type.models.User;
import xyz.genscode.type.utils.Contacts.Contact;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactsListView> {
    View view;
    Context context;
    List<Contact> contacts;
    User mUser;

    public ContactsAdapter(Context context, List<Contact> contacts, User mUser) {
        this.context = context;
        this.contacts = contacts;
        this.mUser = mUser;
    }

    @Override
    public ContactsListView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(context).inflate(R.layout.item_contact, parent, false);
        return new ContactsListView(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactsListView holder, int position) {
        Contact contact = contacts.get(position);
        String name = contact.getName();
        String phone = contact.getPhoneNumber();

        String avatarChar = String.valueOf(name.toUpperCase().charAt(0));

        holder.tvName.setText(name);
        holder.tvAvatarChar.setText(avatarChar);

        holder.button.setOnClickListener(view -> {
            System.out.println(phone);
            System.out.println(name);
            Intent intent = new Intent(context, ProfileActivity.class);
            intent.putExtra("phone", phone);
            intent.putExtra("currentUser", mUser);
            intent.putExtra("name", name);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public class ContactsListView extends RecyclerView.ViewHolder {
        TextView tvName, tvAvatarChar;
        View llRoot;
        Button button;
        public ContactsListView(@NonNull View itemView) {
            super(itemView);

            tvName = view.findViewById(R.id.tvContactsListUserName);
            tvAvatarChar = view.findViewById(R.id.tvContactsListAvatarChar);
            llRoot = view.findViewById(R.id.llContactsListRoot);
            button = view.findViewById(R.id.btContactsList);
        }
    }
}
