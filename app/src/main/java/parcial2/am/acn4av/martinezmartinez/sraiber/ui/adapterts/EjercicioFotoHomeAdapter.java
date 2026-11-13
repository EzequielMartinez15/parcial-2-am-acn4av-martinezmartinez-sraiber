package parcial2.am.acn4av.martinezmartinez.sraiber.ui.adapterts;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import parcial2.am.acn4av.martinezmartinez.sraiber.R;
import parcial2.am.acn4av.martinezmartinez.sraiber.model.EjercicioRutina;

public class EjercicioFotoHomeAdapter
        extends RecyclerView.Adapter<EjercicioFotoHomeAdapter.EjercicioViewHolder> {

    private final List<EjercicioRutina> ejercicios;

    public EjercicioFotoHomeAdapter(List<EjercicioRutina> ejercicios) {
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

        EjercicioRutina ejercicio = ejercicios.get(position);

        holder.nombre.setText(ejercicio.getNombre());

        holder.resumen.setText(
                holder.itemView.getContext().getString(
                        R.string.ejercicio_series_repeticiones,
                        ejercicio.getSeries(),
                        ejercicio.getRepeticiones()
                )
        );

        parcial2.am.acn4av.martinezmartinez.sraiber.FotosEjercicio.cargar(holder.imgEjercicio, ejercicio.getNombreImg());
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