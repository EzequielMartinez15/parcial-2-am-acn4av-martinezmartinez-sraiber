package parcial2.am.acn4av.martinezmartinez.sraiber;

import android.app.Dialog;
import android.os.Bundle;
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

import parcial2.am.acn4av.martinezmartinez.sraiber.model.Ejercicio;

// Usé DialogFragment para que el formulario se conserve al girar el dispositivo.
public class AgregarModificarEjercicioDialog extends DialogFragment {

    private static final String RUTINA = "rutina";
    private static final String NOMBRE = "nombre";
    private static final String SERIES = "series";
    private static final String REPETICIONES = "repeticiones";
    private static final String EDITAR = "editar";

    private static final String POSICION = "posicion";

    public interface ListenerAgregarEjercicio {
        boolean onAgregarEjercicio(String rutina, String nombre, int series, int repeticiones);

    }
    public interface ListenerEditarEjercicio {
        //agregué el métod para editar un ejercicio
        boolean onEditarEjercicio(int posicion, String nombre, int series, int repeticiones);

    }

    public static AgregarModificarEjercicioDialog crearAgregar(String rutina) {
        AgregarModificarEjercicioDialog dialog = new AgregarModificarEjercicioDialog();
        Bundle argumentos = new Bundle();
        argumentos.putString(RUTINA, rutina);
        argumentos.putBoolean(EDITAR, false);
        dialog.setArguments(argumentos);
        return dialog;
    }
    public static AgregarModificarEjercicioDialog crearEditar(String rutina, Ejercicio ejercicio, int posicion) {
        AgregarModificarEjercicioDialog dialog = new AgregarModificarEjercicioDialog();

        Bundle argumentos = new Bundle();
        argumentos.putString(RUTINA, rutina);
        argumentos.putBoolean(EDITAR, true);
        argumentos.putInt(POSICION, posicion);
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
        TextInputLayout campoNombre = formulario.findViewById(R.id.campo_nombre_ejercicio);
        TextInputLayout campoSeries = formulario.findViewById(R.id.campo_series_ejercicio);
        TextInputLayout campoRepeticiones = formulario.findViewById(R.id.campo_repeticiones_ejercicio);
        MaterialAutoCompleteTextView nombre = formulario.findViewById(R.id.input_nombre_ejercicio);
        java.util.List<String> nombres = new java.util.ArrayList<>();
        for (Ejercicio ejercicio : EjercicioRepository.getEjercicios()) {
            nombres.add(ejercicio.getNombre());
        }
        nombre.setAdapter(new ArrayAdapter<>(builder.getContext(), android.R.layout.simple_dropdown_item_1line, nombres));
        nombre.setThreshold(1);
        TextInputEditText series = formulario.findViewById(R.id.input_series_ejercicio);
        TextInputEditText repeticiones = formulario.findViewById(R.id.input_repeticiones_ejercicio);
        TextView errorGuardado = formulario.findViewById(R.id.error_guardado_ejercicio);

        //editar dice si el dialog va a ser de edición o para agregar y que agregue la información si es necesaria
        boolean editar = requireArguments().getBoolean(EDITAR, false);
        String rutina = requireArguments().getString(RUTINA);

        if (editar) {

            nombre.setText(requireArguments().getString(NOMBRE), false);

            series.setText(String.valueOf(requireArguments().getInt(SERIES)));

            repeticiones.setText(String.valueOf(requireArguments().getInt(REPETICIONES)));
        }

        AlertDialog dialog = builder
                .setTitle(editar ? getString(R.string.editar_ejercicio) : getString(R.string.agregar_ejercicio_titulo, rutina))
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
                    campoNombre.setError(nombreIngresado.isEmpty()
                            ? getString(R.string.ejercicio_nombre_error) : null);
                    campoSeries.setError(cantidadSeries == 0
                            ? getString(R.string.ejercicio_numero_error) : null);
                    campoRepeticiones.setError(cantidadRepeticiones == 0
                            ? getString(R.string.ejercicio_numero_error) : null);
                    errorGuardado.setVisibility(View.GONE);
                    if (nombreIngresado.isEmpty()) {
                        nombre.requestFocus();
                        return;
                    }
                    if (cantidadSeries == 0 || cantidadRepeticiones == 0) {
                        (cantidadSeries == 0 ? series : repeticiones).requestFocus();
                        return;
                    }
                    boolean guardado;

                    if (editar) {
                        guardado = ((ListenerEditarEjercicio) requireActivity()).onEditarEjercicio(requireArguments().getInt(POSICION), nombreIngresado, cantidadSeries, cantidadRepeticiones);
                    } else {
                        guardado = ((ListenerAgregarEjercicio) (getParentFragment() instanceof ListenerAgregarEjercicio ? getParentFragment() : requireActivity())).onAgregarEjercicio(rutina, nombreIngresado, cantidadSeries, cantidadRepeticiones);
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
