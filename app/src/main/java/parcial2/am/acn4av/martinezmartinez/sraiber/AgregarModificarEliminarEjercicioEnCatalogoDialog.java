package parcial2.am.acn4av.martinezmartinez.sraiber;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import android.widget.ArrayAdapter;

import parcial2.am.acn4av.martinezmartinez.sraiber.model.EjercicioCatalogo;

public class AgregarModificarEliminarEjercicioEnCatalogoDialog extends DialogFragment {

    private static final String ID = "id";
    private static final String NOMBRE = "nombre";
    private static final String GRUPO = "grupo";
    private static final String FOTO = "foto";
    private static final String EDITAR = "editar";

    private ListenerAgregarEjercicioCatalogo listenerAgregar;
    private ListenerEditarEjercicioCatalogo listenerEditar;

    private ListenerBorrarEjercicioCatalogo listenerBorrar;
    public void setListenerAgregar(
            ListenerAgregarEjercicioCatalogo listener) {

        this.listenerAgregar = listener;
    }
    public void setListenerEditar(
            ListenerEditarEjercicioCatalogo listener) {

        this.listenerEditar = listener;
    }
    public void setListenerBorrar(
            ListenerBorrarEjercicioCatalogo listener) {

        this.listenerBorrar = listener;
    }
    public interface ListenerAgregarEjercicioCatalogo {
        boolean onAgregarEjercicioCatalogo(String nombre, String grupo, String nombreFoto);
    }

    public interface ListenerEditarEjercicioCatalogo {
        boolean onEditarEjercicioCatalogo(String id, String nombre, String grupo, String nombreFoto);
    }

    public interface ListenerBorrarEjercicioCatalogo {
        boolean onBorrarEjercicioCatalogo(String id);
    }

    public static AgregarModificarEliminarEjercicioEnCatalogoDialog crearAgregar() {

        AgregarModificarEliminarEjercicioEnCatalogoDialog dialog = new AgregarModificarEliminarEjercicioEnCatalogoDialog();

        Bundle argumentos = new Bundle();

        argumentos.putBoolean(EDITAR, false);

        dialog.setArguments(argumentos);

        return dialog;
    }

    public static AgregarModificarEliminarEjercicioEnCatalogoDialog crearEditar(EjercicioCatalogo ejercicio) {

        AgregarModificarEliminarEjercicioEnCatalogoDialog dialog = new AgregarModificarEliminarEjercicioEnCatalogoDialog();

        Bundle argumentos = new Bundle();

        argumentos.putBoolean(EDITAR, true);

        argumentos.putString(ID, ejercicio.getId());

        argumentos.putString(NOMBRE, ejercicio.getNombre());

        argumentos.putString(GRUPO, ejercicio.getGrupoMuscular());

        argumentos.putString(FOTO, ejercicio.getNombreFoto());

        dialog.setArguments(argumentos);

        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(
            @Nullable Bundle savedInstanceState) {

        MaterialAlertDialogBuilder builder =
                new MaterialAlertDialogBuilder(
                        requireContext(),
                        R.style.ThemeOverlay_Marber_Dialog);

        View formulario =
                LayoutInflater.from(
                                builder.getContext())
                        .inflate(
                                R.layout.dialog_agregar_modificar_ejercicio_catalogo,
                                null
                        );

        TextInputLayout campoNombre =
                formulario.findViewById(
                        R.id.campo_nombre_ejercicio);

        TextInputLayout campoGrupo =
                formulario.findViewById(
                        R.id.campo_grupo_muscular);

        TextInputLayout campoFoto =
                formulario.findViewById(
                        R.id.campo_nombre_foto);

        TextInputEditText nombre =
                formulario.findViewById(
                        R.id.input_nombre_ejercicio);

        MaterialAutoCompleteTextView grupo =
                formulario.findViewById(
                        R.id.input_grupo_muscular);

        TextInputEditText foto =
                formulario.findViewById(
                        R.id.input_nombre_foto);

        String[] grupos = {
                "Biceps",
                "Triceps",
                "Pecho",
                "Espalda",
                "Hombros",
                "Piernas",
                "Gluteos",
                "Abdominales",
                "Cardio"
        };

        grupo.setAdapter(
                new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        grupos
                )
        );

        boolean editar =
                requireArguments()
                        .getBoolean(
                                EDITAR,
                                false
                        );

        if (editar) {

            nombre.setText(
                    requireArguments()
                            .getString(
                                    NOMBRE
                            )
            );

            grupo.setText(
                    requireArguments()
                            .getString(
                                    GRUPO
                            ),
                    false
            );

            foto.setText(
                    requireArguments()
                            .getString(
                                    FOTO
                            )
            );
        }

        AlertDialog dialog =
                builder
                        .setTitle(
                                editar
                                        ? "Editar ejercicio"
                                        : "Agregar ejercicio"
                        )
                        .setView(formulario)
                        .setNegativeButton(
                                "Cancelar",
                                null
                        )
                        .setPositiveButton(
                                "Guardar",
                                null
                        )
                        .setNeutralButton(
                                editar ? "Borrar" : null,
                                null
                        )
                        .create();

        dialog.setOnShowListener(
                v -> {

                    dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                            .setOnClickListener(
                                    click -> {

                                        String nombreIngresado =
                                                texto(nombre);

                                        String grupoIngresado =
                                                grupo.getText()
                                                        .toString()
                                                        .trim();

                                        String fotoIngresada =
                                                texto(foto);

                                        campoNombre.setError(null);
                                        campoGrupo.setError(null);
                                        campoFoto.setError(null);

                                        if (nombreIngresado.isEmpty()) {

                                            campoNombre.setError(
                                                    "Ingrese un nombre"
                                            );

                                            return;
                                        }

                                        if (grupoIngresado.isEmpty()) {

                                            campoGrupo.setError(
                                                    "Seleccione grupo muscular"
                                            );

                                            return;
                                        }

                                        if (fotoIngresada.isEmpty()) {

                                            campoFoto.setError(
                                                    "Ingrese nombre de foto"
                                            );

                                            return;
                                        }

                                        boolean guardado;

                                        if (editar) {

                                            guardado =
                                                    listenerEditar
                                                            .onEditarEjercicioCatalogo(
                                                                    requireArguments().getString(ID),
                                                                    nombreIngresado,
                                                                    grupoIngresado,
                                                                    fotoIngresada
                                                            );


                                        } else {

                                            guardado =
                                                    listenerAgregar
                                                            .onAgregarEjercicioCatalogo(
                                                                    nombreIngresado,
                                                                    grupoIngresado,
                                                                    fotoIngresada
                                                            );

                                        }

                                        if (guardado) {
                                            dismiss();
                                        }
                                    }
                            );

                    if (editar) {

                        dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
                                .setTextColor(requireContext().getColor(R.color.marber_error));

                        dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
                                .setOnClickListener(v2 -> {

                                    String idEjercicio = requireArguments().getString(ID);

                                    boolean guardado = listenerBorrar
                                            .onBorrarEjercicioCatalogo(
                                                    idEjercicio
                                            );

                                    if (guardado) {
                                        dismiss();
                                    }
                                });
                    }
                });


        return dialog;
    }

    private String texto(
            TextInputEditText campo) {

        return campo.getText() == null
                ? ""
                : campo.getText()
                .toString()
                .trim();
    }
}