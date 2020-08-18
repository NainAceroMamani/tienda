package com.nain.tienda.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.nain.tienda.R;
import com.nain.tienda.activities.Comprador.InfoActivity;
import com.nain.tienda.models.Tienda;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class TiendaAdapter extends FirestoreRecyclerAdapter<Tienda, TiendaAdapter.ViewHolder> {

    Context context;

    public TiendaAdapter(FirestoreRecyclerOptions<Tienda> options, Context context){
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull TiendaAdapter.ViewHolder holder, int position, @NonNull Tienda tienda) {
        DocumentSnapshot document = getSnapshots().getSnapshot(position);
        final String tienda_id = document.getId();

        holder.textViewName.setText(tienda.getNombre());
        holder.textViewDescription.setText(tienda.getDescripcion());
        holder.textViewTelefono.setText(tienda.getTelefono());
        if(tienda.getUrl_imagen() != null){
            if(!tienda.getUrl_imagen().isEmpty()){
                Picasso.with(context).load(tienda.getUrl_imagen()).into(holder.circleImageViewTienda);
            }
        }
        holder.viewHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, InfoActivity.class);
                intent.putExtra("id", tienda_id);
                context.startActivity(intent);
            }
        });

    }

    @NonNull
    @Override
    public TiendaAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_tiendas, parent, false);
        return new ViewHolder(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName, textViewDescription, textViewTelefono;
        CircleImageView circleImageViewTienda;
        View viewHolder;

        public ViewHolder(View view){
            super(view);
            textViewName = view.findViewById(R.id.txt_name_tienda);
            textViewDescription = view.findViewById(R.id.txt_description_tienda);
            textViewTelefono = view.findViewById(R.id.txt_telefono_tienda);
            circleImageViewTienda = view.findViewById(R.id.imageViewTienda);
            viewHolder = view;
        }
    }
}
