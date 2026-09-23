package parcial1.am.acn4av.martinezmartinez.sraiber.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import parcial1.am.acn4av.martinezmartinez.sraiber.R;
import parcial1.am.acn4av.martinezmartinez.sraiber.model.Ejercicio;

public class EjercicioAdapter
        extends RecyclerView.Adapter<EjercicioAdapter.EjercicioViewHolder> {

    private final List<Ejercicio> ejercicios;

    public EjercicioAdapter(List<Ejercicio> ejercicios) {
        this.ejercicios = ejercicios;
    }

    @NonNull
    @Override
    public EjercicioViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.foto_ejercicio, parent, false);

        return new EjercicioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull EjercicioViewHolder holder,
            int position) {

        Ejercicio ejercicio = ejercicios.get(position);

        holder.imgEjercicio.setImageResource(
                ejercicio.getImagen()
        );
    }

    @Override
    public int getItemCount() {
        return ejercicios.size();
    }

    static class EjercicioViewHolder extends RecyclerView.ViewHolder {

        ImageView imgEjercicio;

        public EjercicioViewHolder(@NonNull View itemView) {
            super(itemView);

            imgEjercicio =
                    itemView.findViewById(R.id.img_ejercicio);
        }
    }
}