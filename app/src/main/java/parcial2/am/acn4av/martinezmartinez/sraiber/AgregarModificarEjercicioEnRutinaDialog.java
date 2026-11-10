package parcial2.am.acn4av.martinezmartinez.sraiber;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.ArrayAdapter;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import parcial2.am.acn4av.martinezmartinez.sraiber.Repository.EjercicioRepository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

import parcial2.am.acn4av.martinezmartinez.sraiber.Repository.RutinaRepository;
import parcial2.am.acn4av.martinezmartinez.sraiber.model.EjercicioCatalogo;
import parcial2.am.acn4av.martinezmartinez.sraiber.model.EjercicioRutina;
import parcial2.am.acn4av.martinezmartinez.sraiber.model.Rutina;

// Usé DialogFragment para que el formulario se conserve al girar el dispositivo.
public class AgregarModificarEjercicioEnRutinaDialog extends DialogFragment {

    private static final String RUTINA = "rutina";
    private static final String NOMBRE = "nombre";
    private static final String SERIES = "series";
    private static final String REPETICIONES = "repeticiones";
    private static final String EDITAR = "editar";
    private static final String EJERCICIO_ID = "ejercicio_id";
    private static final String DESDE_EJERCICIO = "desde_ejercicio";

    private static final String POSICION = "posicion";
    private String rutinaAGuardar;
    private EjercicioCatalogo ejercicioSeleccionado;
    private ListenerAgregarEjercicio listenerAgregar;
    public void setListenerAgregar(ListenerAgregarEjercicio listener) {
        listenerAgregar = listener;
    }

    public interface ListenerAgregarEjercicio {
        boolean onAgregarEjercicio(String rutina, EjercicioCatalogo ejercicio, int series, int repeticiones);
    }
    public interface ListenerEditarEjercicio {
        boolean onEditarEjercicio(int posicion, String idEnFirebase, String nombre, int series, int repeticiones);

    }

    public static AgregarModificarEjercicioEnRutinaDialog crearAgregar(String rutina) {
        AgregarModificarEjercicioEnRutinaDialog dialog = new AgregarModificarEjercicioEnRutinaDialog();
        Bundle argumentos = new Bundle();
        argumentos.putString(RUTINA, rutina);
        argumentos.putBoolean(EDITAR, false);
        dialog.setArguments(argumentos);
        return dialog;
    }
    public static AgregarModificarEjercicioEnRutinaDialog crearAgregarDesdeEjercicio(EjercicioCatalogo ejercicio) {
        AgregarModificarEjercicioEnRutinaDialog dialog = new AgregarModificarEjercicioEnRutinaDialog();
        Bundle argumentos = new Bundle();
        argumentos.putBoolean(DESDE_EJERCICIO, true);
        argumentos.putString(EJERCICIO_ID, ejercicio.getId());
        argumentos.putString(RUTINA, "una rutina");
        dialog.setArguments(argumentos);
        return dialog;
    }
    public static AgregarModificarEjercicioEnRutinaDialog crearEditar(String rutina, EjercicioRutina ejercicio, int posicion) {
        AgregarModificarEjercicioEnRutinaDialog dialog = new AgregarModificarEjercicioEnRutinaDialog();

        Bundle argumentos = new Bundle();
        argumentos.putString(RUTINA, rutina);
        argumentos.putBoolean(EDITAR, true);
        argumentos.putInt(POSICION, posicion);
        argumentos.putString(EJERCICIO_ID, ejercicio.getIdEnFirebase());
        argumentos.putString(NOMBRE, ejercicio.getNombre());
        argumentos.putInt(SERIES, ejercicio.getSeries());
        argumentos.putInt(REPETICIONES, ejercicio.getRepeticiones());
        dialog.setArguments(argumentos);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(
                requireContext(), R.style.ThemeOverlay_Marber_Dialog);
        View formulario = LayoutInflater.from(builder.getContext())
                .inflate(R.layout.dialog_agregar_modificar_ejercicio, null);

        boolean desdeEjercicio = requireArguments().getBoolean(DESDE_EJERCICIO, false);
        //Si edita el ejercicio o viene desde ejercicio para agregarlo a una rutina siempre se guarda la id
        String idEjercicio = requireArguments().getString(EJERCICIO_ID);
        TextView txtEjercicioSeleccionado = formulario.findViewById(R.id.txtEjercicioSeleccionado);
        TextInputLayout campoRutina = formulario.findViewById(R.id.campo_rutina);
        MaterialAutoCompleteTextView rutinaSeleccionada = formulario.findViewById(R.id.input_rutina);
        MaterialAutoCompleteTextView filtroGrupo = formulario.findViewById(R.id.input_grupo_muscular);
        TextInputLayout campoNombre = formulario.findViewById(R.id.campo_nombre_ejercicio);
        TextInputLayout campoGrupo = formulario.findViewById(R.id.campo_grupo_muscular);
        TextInputLayout campoSeries = formulario.findViewById(R.id.campo_series_ejercicio);
        TextInputLayout campoRepeticiones = formulario.findViewById(R.id.campo_repeticiones_ejercicio);
        MaterialAutoCompleteTextView nombre = formulario.findViewById(R.id.input_nombre_ejercicio);
        nombre.setThreshold(1);
        TextInputEditText series = formulario.findViewById(R.id.input_series_ejercicio);
        TextInputEditText repeticiones = formulario.findViewById(R.id.input_repeticiones_ejercicio);
        TextView errorGuardado = formulario.findViewById(R.id.error_guardado_ejercicio);

        List<String> nombresRutina = new ArrayList<>();

        for (Rutina rutina : RutinaRepository.getRutinas(requireContext())) {
            nombresRutina.add(rutina.getNombre());
        }
        rutinaSeleccionada.setAdapter(
                new ArrayAdapter<>(
                        builder.getContext(),
                        R.layout.item_dropdown_marber,
                        nombresRutina
                )
        );

        List<EjercicioCatalogo> ejerciciosCatalogo = new ArrayList<>();
        ejerciciosCatalogo.addAll(EjercicioRepository.getCatalogoCache());
        ejerciciosCatalogo.addAll(EjercicioRepository.getCatalogoCreadosUserCache());
        java.util.Set<String> grupos = new java.util.HashSet<>();

        //Si entra desde la parte de ejercicios
        if (desdeEjercicio) {
            for (EjercicioCatalogo ejercicio : ejerciciosCatalogo) {
                if (ejercicio.getId().equals(idEjercicio)) {
                    ejercicioSeleccionado = ejercicio;
                    break;
                }
            }

            campoGrupo.setVisibility(View.GONE);
            campoNombre.setVisibility(View.GONE);
            txtEjercicioSeleccionado.setVisibility(View.VISIBLE);
            if (ejercicioSeleccionado != null) {
                txtEjercicioSeleccionado.setText(ejercicioSeleccionado.getNombre());
            }
        }else{
            campoRutina.setVisibility(View.GONE);
        }

        for (EjercicioCatalogo ejercicio : ejerciciosCatalogo) {
            grupos.add(ejercicio.getGrupoMuscular());
        }
        //editar dice si el dialog va a ser de edición o para agregar y que agregue la información si es necesaria
        boolean editar = requireArguments().getBoolean(EDITAR, false);
        rutinaAGuardar = requireArguments().getString(RUTINA);

        rutinaSeleccionada.setOnItemClickListener((parent, view, position, id) -> {
            rutinaAGuardar = rutinaSeleccionada.getText().toString();
        });

        filtroGrupo.setAdapter(
                new ArrayAdapter<>(
                        builder.getContext(),
                        R.layout.item_dropdown_marber,
                        new java.util.ArrayList<>(grupos)
                )
        );

        List<String> nombresEjercicios = new ArrayList<>();
        for (EjercicioCatalogo ejercicio : ejerciciosCatalogo) {
            nombresEjercicios.add(ejercicio.getNombre());
        }
        nombre.setAdapter(
                new ArrayAdapter<>(
                        builder.getContext(),
                        R.layout.item_dropdown_marber,
                        nombresEjercicios
                )
        );
        filtroGrupo.setOnClickListener(v -> filtroGrupo.showDropDown());
        campoGrupo.setEndIconOnClickListener(v -> filtroGrupo.showDropDown());
        campoNombre.setEndIconOnClickListener(v -> nombre.showDropDown());
        nombre.setOnClickListener(v -> nombre.showDropDown());


        //Guiarda el filtro del grupo y filtra por el grupoMuscuolar seleccionado
        filtroGrupo.setOnItemClickListener((parent, view, position, id) -> {
            String grupo = (String) parent.getItemAtPosition(position);

            java.util.List<String> nombresFiltrados = new java.util.ArrayList<>();

            for (EjercicioCatalogo ejercicio : ejerciciosCatalogo) {
                if (grupo.equals(ejercicio.getGrupoMuscular())) {
                    nombresFiltrados.add(ejercicio.getNombre());
                }
            }

            nombre.setText("", false);

            nombre.setAdapter(
                    new ArrayAdapter<>(
                            builder.getContext(),
                            R.layout.item_dropdown_marber,
                            nombresFiltrados
                    )
            );
            nombre.showDropDown();
        });
        //Elige el nombre del ejercicio seleccionado de la lista.
        nombre.setOnItemClickListener((parent, view, position, id) -> {
            String nombreElegido = (String) parent.getItemAtPosition(position);

            for (EjercicioCatalogo ejercicio : ejerciciosCatalogo) {
                if (ejercicio.getNombre().equals(nombreElegido)) {

                    ejercicioSeleccionado = ejercicio;
                    break;
                }
            }
        });

        if (editar) {

            String nombreEjercicioAEditar = requireArguments().getString(NOMBRE);

            for (EjercicioCatalogo ejercicio : ejerciciosCatalogo) {
                if (ejercicio.getNombre().equals(nombreEjercicioAEditar)) {
                    ejercicioSeleccionado = ejercicio;
                    filtroGrupo.setText(ejercicio.getGrupoMuscular(), false);
                    nombre.setText(ejercicio.getNombre(), false);
                    break;
                }
            }

            series.setText(String.valueOf(requireArguments().getInt(SERIES)));

            repeticiones.setText(String.valueOf(requireArguments().getInt(REPETICIONES)));
        }

        AlertDialog dialog = builder
                .setTitle(editar ? getString(R.string.editar_ejercicio) : getString(R.string.agregar_ejercicio_titulo, rutinaAGuardar))
                .setView(formulario)
                .setNegativeButton(R.string.ejercicio_cancelar, null)
                .setPositiveButton(R.string.ejercicio_guardar, null)
                .create();

        // Reemplacé el clic de Guardar para que el diálogo no se cierre si hay errores.
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String nombreIngresado = texto(nombre);
                    int cantidadSeries = numeroPositivo(texto(series));
                    int cantidadRepeticiones = numeroPositivo(texto(repeticiones));
                    campoNombre.setError(ejercicioSeleccionado == null
                            ? getString(R.string.ejercicio_nombre_error) : null);
                    campoSeries.setError(cantidadSeries == 0
                            ? getString(R.string.ejercicio_numero_error) : null);
                    campoRepeticiones.setError(cantidadRepeticiones == 0
                            ? getString(R.string.ejercicio_numero_error) : null);
                    errorGuardado.setVisibility(View.GONE);
                    if (ejercicioSeleccionado == null) {
                        nombre.requestFocus();
                        return;
                    }
                    if (cantidadSeries == 0 || cantidadRepeticiones == 0) {
                        (cantidadSeries == 0 ? series : repeticiones).requestFocus();
                        return;
                    }
                    boolean guardado;

                    if (editar) {
                        guardado = ((ListenerEditarEjercicio) requireActivity()).onEditarEjercicio(requireArguments().getInt(POSICION),idEjercicio, nombreIngresado, cantidadSeries, cantidadRepeticiones);
                    } else {
                        guardado = listenerAgregar.onAgregarEjercicio(rutinaAGuardar, ejercicioSeleccionado, cantidadSeries, cantidadRepeticiones);
                    }
                    if (guardado) {
                        dismiss();
                    } else {
                        errorGuardado.setVisibility(View.VISIBLE);
                    }
                }));
        repeticiones.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
                return true;
            }
            return false;
        });
        return dialog;
    }


    private String texto(EditText campo) {
        return campo.getText() == null ? "" : campo.getText().toString().trim();
    }

    private int numeroPositivo(String valor) {
        try {
            int numero = Integer.parseInt(valor);
            return numero > 0 ? numero : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
