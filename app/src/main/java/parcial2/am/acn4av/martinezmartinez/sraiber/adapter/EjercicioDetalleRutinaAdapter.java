package parcial2.am.acn4av.martinezmartinez.sraiber.adapter;

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

import parcial2.am.acn4av.martinezmartinez.sraiber.R;
import parcial2.am.acn4av.martinezmartinez.sraiber.model.EjercicioRutina;

public class EjercicioDetalleRutinaAdapter
        extends RecyclerView.Adapter<EjercicioDetalleRutinaAdapter.ViewHolder> {

    private final Context context;
    private final List<EjercicioRutina> ejercicios;

    public interface OnEjercicioDeleteListener { void onDelete(EjercicioRutina ejercicio);}
    private final OnEjercicioDeleteListener listenerBorrar;
    public interface OnEjercicioEditListener { void onEdit(EjercicioRutina ejercicio);}
    private final OnEjercicioEditListener listenerEdit;

    public EjercicioDetalleRutinaAdapter(Context context, List<EjercicioRutina> ejercicios, OnEjercicioDeleteListener listenerBorrar, OnEjercicioEditListener listenerEdit) {
        this.context = context;
        this.ejercicios = ejercicios;
        this.listenerBorrar = listenerBorrar;
        this.listenerEdit = listenerEdit;
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

        EjercicioRutina ejercicio = ejercicios.get(position);

        holder.imgEjercicio.setContentDescription(context.getString(R.string.foto_de_ejercicio, ejercicio.getNombre()));
        int imagen = holder.itemView.getContext()
                .getResources()
                .getIdentifier(
                        ejercicio.getNombreImg(),
                        "drawable",
                        holder.itemView.getContext().getPackageName()
                );

        holder.imgEjercicio.setImageResource(
                imagen != 0
                        ? imagen
                        : R.drawable.ic_marber_dumbbell
        );

        holder.txtNombre.setText(
                ejercicio.getNombre()
        );

        holder.txtSeries.setText(
                context.getString(R.string.detalle_series, ejercicio.getSeries())
        );

        holder.txtRepeticiones.setText(
                context.getString(R.string.detalle_repeticiones, ejercicio.getRepeticiones())
        );

        holder.btnEditar.setOnClickListener(v -> {

            int posicionActual = holder.getBindingAdapterPosition();

            if (posicionActual != RecyclerView.NO_POSITION) {

                listenerEdit.onEdit(ejercicios.get(posicionActual));
            }
        });

        holder.btnBorrar.setOnClickListener(v -> {

            int posicionActual = holder.getBindingAdapterPosition();

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
