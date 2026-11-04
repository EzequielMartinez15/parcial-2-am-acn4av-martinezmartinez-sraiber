package parcial2.am.acn4av.martinezmartinez.sraiber.adapter;

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

import parcial2.am.acn4av.martinezmartinez.sraiber.R;
import parcial2.am.acn4av.martinezmartinez.sraiber.model.Rutina;

public class RutinaAdapter
        extends RecyclerView.Adapter<RutinaAdapter.RutinaViewHolder> {

    private final List<Rutina> rutinas;
    public static final String EJERCICIO_AGREGADO = "ejercicio_agregado";

    public interface OnAgregarEjercicioListener {
        void onAgregarEjercicio(Rutina rutina);
    }
    public interface OnDetalleListener {
        void OnDetalleListener(Rutina rutina);
    }
    private final OnAgregarEjercicioListener listener;
    private final OnDetalleListener listenerDetalle;

    public RutinaAdapter(List<Rutina> rutinas, OnAgregarEjercicioListener listener, OnDetalleListener listenerDetalle) {
        this.rutinas = rutinas;
        this.listener = listener;
        this.listenerDetalle = listenerDetalle;
    }

    @NonNull
    @Override
    public RutinaViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_rutina_home, parent, false);

        return new RutinaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull RutinaViewHolder holder,
            int position) {

        Rutina rutina = rutinas.get(position);
        holder.agregar.setOnClickListener(v -> listener.onAgregarEjercicio(rutina));
        holder.btnDetalle.setOnClickListener(v -> listenerDetalle.OnDetalleListener(rutina));
        holder.titulo.setText(
                rutina.getNombre()
        );

        holder.veces.setText(
                holder.itemView.getResources().getQuantityString(R.plurals.rutina_realizaciones,
                        rutina.getVecesRealizada(), rutina.getVecesRealizada())
        );

        SimpleDateFormat sdf =
                new SimpleDateFormat(
                        "dd/MM/yyyy",
                        Locale.getDefault()
                );

        holder.fecha.setText(
                rutina.getVecesRealizada() == 0 ? holder.itemView.getContext().getString(R.string.sin_entrenamientos_previos) : sdf.format(
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
                new EjercicioFotoHomeAdapter(
                        rutina.getEjercicios()
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull RutinaViewHolder holder, int position,
                                 @NonNull List<Object> payloads) {
        onBindViewHolder(holder, position);
        if (payloads.contains(EJERCICIO_AGREGADO)) {
            holder.rvEjercicios.scrollToPosition(rutinas.get(position).getEjercicios().size() - 1);
        }
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
        View agregar;
        View btnDetalle;

        public RutinaViewHolder(
                @NonNull View itemView) {

            super(itemView);
            agregar = itemView.findViewById(R.id.btn_agregar_ejercicio);
            btnDetalle = itemView.findViewById(R.id.btn_detalle_rutina);

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
