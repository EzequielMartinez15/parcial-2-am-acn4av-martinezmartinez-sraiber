package parcial2.am.acn4av.martinezmartinez.sraiber.ui.adapterts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Set;

import parcial2.am.acn4av.martinezmartinez.sraiber.ui.dialogs.AgregarModificarEjercicioEnRutinaDialog;
import parcial2.am.acn4av.martinezmartinez.sraiber.ui.dialogs.AgregarModificarEliminarEjercicioEnCatalogoDialog;
import parcial2.am.acn4av.martinezmartinez.sraiber.R;
import parcial2.am.acn4av.martinezmartinez.sraiber.model.EjercicioCatalogo;

public class EjerciciosAdapterEnActivityEjercicios extends RecyclerView.Adapter<EjerciciosAdapterEnActivityEjercicios.ViewHolder> {

    private final List<EjercicioCatalogo> ejercicios;
    private final Context context;
    private Set<String> idsEjerciciosUsuario;
    private final AgregarModificarEliminarEjercicioEnCatalogoDialog.ListenerEditarEjercicioCatalogo listenerEditar;
    private final AgregarModificarEliminarEjercicioEnCatalogoDialog.ListenerBorrarEjercicioCatalogo listenerBorrar;
    private final AgregarModificarEjercicioEnRutinaDialog.ListenerAgregarEjercicio listenerAgregarEjercicio;
//Este es para agregar un ejercicio a una rutina

    public EjerciciosAdapterEnActivityEjercicios(
            List<EjercicioCatalogo> ejercicios,
            Context context,
            Set<String> idsEjerciciosUsuario,
            AgregarModificarEliminarEjercicioEnCatalogoDialog
                    .ListenerEditarEjercicioCatalogo listenerEditar,
            AgregarModificarEliminarEjercicioEnCatalogoDialog.
                            ListenerBorrarEjercicioCatalogo listenerBorrar,
            AgregarModificarEjercicioEnRutinaDialog.
                    ListenerAgregarEjercicio listenerAgregarEjercicio) {

        this.ejercicios = ejercicios;
        this.context = context;
        this.idsEjerciciosUsuario = idsEjerciciosUsuario;
        this.listenerEditar = listenerEditar;
        this.listenerBorrar = listenerBorrar;
        this.listenerAgregarEjercicio = listenerAgregarEjercicio;
    }
    public void actualizarIdsUsuario(
            Set<String> nuevosIds){

        idsEjerciciosUsuario = nuevosIds;

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(
                        R.layout.item_ejercicio_activity_ejercicios,
                        parent,
                        false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        EjercicioCatalogo ejercicio =
                ejercicios.get(position);

        holder.txtNombre.setText(
                ejercicio.getNombre());

        holder.txtGrupo.setText(
                ejercicio.getGrupoMuscular());

        parcial2.am.acn4av.martinezmartinez.sraiber.FotosEjercicio.cargar(holder.imgEjercicio, ejercicio.getNombreFoto());

        boolean editable =
                idsEjerciciosUsuario.contains(
                        ejercicio.getId());

        holder.btnEditarEjercicio.setVisibility(
                editable
                        ? View.VISIBLE
                        : View.GONE
        );
        holder.btnEditarEjercicio.setOnClickListener(v -> {

            AgregarModificarEliminarEjercicioEnCatalogoDialog dialog =
                    AgregarModificarEliminarEjercicioEnCatalogoDialog
                            .crearEditar(ejercicio);

            dialog.setListenerEditar(listenerEditar);
            dialog.setListenerBorrar(listenerBorrar);

            dialog.show(
                    ((FragmentActivity) context)
                            .getSupportFragmentManager(),
                    "editarEjercicio"
            );
        });

        holder.btnAgregarARutina.setOnClickListener(v -> {
            AgregarModificarEjercicioEnRutinaDialog dialog =
                    AgregarModificarEjercicioEnRutinaDialog
                            .crearAgregarDesdeEjercicio(ejercicio);

            dialog.setListenerAgregar(listenerAgregarEjercicio);
            dialog.show(
                    ((FragmentActivity) context)
                            .getSupportFragmentManager(),
                    "agregarRutina"
            );

        });
    }

    @Override
    public int getItemCount() {
        return ejercicios.size();
    }

    public static class ViewHolder
            extends RecyclerView.ViewHolder {

        ImageView imgEjercicio;
        TextView txtNombre;
        TextView txtGrupo;
        MaterialButton btnEditarEjercicio;
        MaterialButton btnAgregarARutina;

        public ViewHolder(
                @NonNull View itemView) {

            super(itemView);

            imgEjercicio =
                    itemView.findViewById(
                            R.id.imgEjercicio);

            txtNombre =
                    itemView.findViewById(
                            R.id.txtNombre);

            txtGrupo =
                    itemView.findViewById(
                            R.id.txtMusculo);

            btnEditarEjercicio = itemView.findViewById(R.id.btnEditarEjercicio);
            btnAgregarARutina = itemView.findViewById(R.id.btnAgregarARutina);
        }
    }
}