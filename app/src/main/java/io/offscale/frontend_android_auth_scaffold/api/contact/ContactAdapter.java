package io.offscale.frontend_android_auth_scaffold.api.contact;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import io.offscale.frontend_android_auth_scaffold.R;
import io.offscale.frontend_android_auth_scaffold.utils.PrimitiveArrayAdapter;

public final class ContactAdapter extends PrimitiveArrayAdapter<Contact> {
    public ContactAdapter(final Context context, final Contact[] contacts) {
        super(context, contacts);
    }

    @Override
    @NonNull
    public final View getView(final int position, View convertView,
                              final @NonNull ViewGroup parent) {
        // Get the data item for this position
        final Contact contact = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.contact_item, parent, false);

        // Lookup view for data population
        final TextView name = (TextView) convertView.findViewById(R.id.contact_item_name);
        final TextView email = (TextView) convertView.findViewById(R.id.contact_item_email);
        final TextView owner = (TextView) convertView.findViewById(R.id.contact_item_owner);
        // Populate the data into the template view using the data object
        name.setText(contact.getName());
        email.setText(contact.getEmail());
        owner.setText(contact.getOwner());
        // Return the completed view to render on screen
        return convertView;
    }
}
