package parcial1.am.acn4av.martinezmartinez.sraiber.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import parcial1.am.acn4av.martinezmartinez.sraiber.R;
import parcial1.am.acn4av.martinezmartinez.sraiber.model.Ejercicio;

public class EjercicioFotoHomeAdapter
        extends RecyclerView.Adapter<EjercicioFotoHomeAdapter.EjercicioViewHolder> {

    private final List<Ejercicio> ejercicios;

    public EjercicioFotoHomeAdapter(List<Ejercicio> ejercicios) {
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
        holder.nombre.setText(ejercicio.getNombre());
        holder.resumen.setText(holder.itemView.getContext().getString(
                R.string.ejercicio_series_repeticiones, ejercicio.getSeries(), ejercicio.getRepeticiones()));

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
        TextView nombre;
        TextView resumen;

        public EjercicioViewHolder(@NonNull View itemView) {
            super(itemView);
            nombre = itemView.findViewById(R.id.nombre_ejercicio);
            resumen = itemView.findViewById(R.id.resumen_ejercicio);

            imgEjercicio =
                    itemView.findViewById(R.id.img_ejercicio);
        }
    }
}