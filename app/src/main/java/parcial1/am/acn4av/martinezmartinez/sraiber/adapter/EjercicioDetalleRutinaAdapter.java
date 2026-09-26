package parcial1.am.acn4av.martinezmartinez.sraiber.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

import parcial1.am.acn4av.martinezmartinez.sraiber.R;
import parcial1.am.acn4av.martinezmartinez.sraiber.model.Ejercicio;

public class EjercicioDetalleRutinaAdapter
        extends RecyclerView.Adapter<EjercicioDetalleRutinaAdapter.ViewHolder> {

    private final Context context;
    private final List<Ejercicio> ejercicios;

    public interface OnEjercicioDeleteListener { void onDelete(Ejercicio ejercicio);}
    private final OnEjercicioDeleteListener listenerBorrar;

    public EjercicioDetalleRutinaAdapter(Context context, List<Ejercicio> ejercicios, OnEjercicioDeleteListener listenerBorrar) {
        this.context = context;
        this.ejercicios = ejercicios;
        this.listenerBorrar = listenerBorrar;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view = LayoutInflater.from(context)
                .inflate(
                        R.layout.item_ejercicio_detalle,
                        parent,
                        false
                );

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {

        Ejercicio ejercicio = ejercicios.get(position);

        holder.txtNombre.setText(
                ejercicio.getNombre()
        );

        holder.txtSeries.setText(
                "Series: "+ejercicio.getSeries()
        );

        holder.txtRepeticiones.setText(
                "Repeticiones: "+ejercicio.getRepeticiones()
        );

        holder.btnEditar.setOnClickListener(v -> {

            // editar ejercicio
        });

        holder.btnBorrar.setOnClickListener(v -> {

            int posicionActual = holder.getAdapterPosition();

            if (posicionActual != RecyclerView.NO_POSITION) {

                listenerBorrar.onDelete(ejercicios.get(posicionActual));
            }
        });
    }

    @Override
    public int getItemCount() {
        return ejercicios.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgEjercicio;

        TextView txtNombre;
        TextView txtSeries;
        TextView txtRepeticiones;

        MaterialButton btnEditar;
        MaterialButton btnBorrar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgEjercicio =
                    itemView.findViewById(R.id.img_ejercicio);

            txtNombre =
                    itemView.findViewById(R.id.txt_nombre);

            txtSeries =
                    itemView.findViewById(R.id.txt_series);

            txtRepeticiones =
                    itemView.findViewById(R.id.txt_repeticiones);

            btnEditar =
                    itemView.findViewById(R.id.btn_editar_ejercicio);

            btnBorrar =
                    itemView.findViewById(R.id.btn_borrar_ejercicio);
        }
    }
}
