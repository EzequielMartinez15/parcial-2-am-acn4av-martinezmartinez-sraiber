package parcial1.am.acn4av.martinezmartinez.sraiber.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import parcial1.am.acn4av.martinezmartinez.sraiber.R;
import parcial1.am.acn4av.martinezmartinez.sraiber.model.Rutina;

public class RutinaAdapter
        extends RecyclerView.Adapter<RutinaAdapter.RutinaViewHolder> {

    private final List<Rutina> rutinas;

    public RutinaAdapter(List<Rutina> rutinas) {
        this.rutinas = rutinas;
    }

    @NonNull
    @Override
    public RutinaViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_rutina, parent, false);

        return new RutinaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull RutinaViewHolder holder,
            int position) {

        Rutina rutina = rutinas.get(position);

        holder.titulo.setText(
                rutina.getNombre()
        );

        holder.veces.setText(
                "Realizada " +
                        rutina.getVecesRealizada() +
                        " veces"
        );

        SimpleDateFormat sdf =
                new SimpleDateFormat(
                        "dd/MM/yyyy",
                        Locale.getDefault()
                );

        holder.fecha.setText(
                sdf.format(
                        rutina.getUltimaVezRealizada()
                )
        );

        holder.rvEjercicios.setLayoutManager(
                new LinearLayoutManager(
                        holder.itemView.getContext(),
                        LinearLayoutManager.HORIZONTAL,
                        false
                )
        );

        holder.rvEjercicios.setAdapter(
                new EjercicioAdapter(
                        rutina.getEjercicios()
                )
        );
    }

    @Override
    public int getItemCount() {
        return rutinas.size();
    }

    static class RutinaViewHolder
            extends RecyclerView.ViewHolder {

        TextView titulo;
        TextView veces;
        TextView fecha;

        RecyclerView rvEjercicios;

        public RutinaViewHolder(
                @NonNull View itemView) {

            super(itemView);

            titulo =
                    itemView.findViewById(
                            R.id.titulo_card_rutina
                    );

            veces =
                    itemView.findViewById(
                            R.id.subtitulo1_card
                    );

            fecha =
                    itemView.findViewById(
                            R.id.subtitulo2_card
                    );

            rvEjercicios =
                    itemView.findViewById(
                            R.id.rv_ejercicios
                    );
        }
    }
}