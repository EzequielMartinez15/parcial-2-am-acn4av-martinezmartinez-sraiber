package parcial1.am.acn4av.martinezmartinez.sraiber;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

// Usé DialogFragment para que el formulario se conserve al girar el dispositivo.
public class AgregarEjercicioDialog extends DialogFragment {
    private static final String RUTINA = "rutina";

    public interface Listener {
        boolean onAgregarEjercicio(String rutina, String nombre, int series, int repeticiones);
    }

    public static AgregarEjercicioDialog crear(String rutina) {
        AgregarEjercicioDialog dialog = new AgregarEjercicioDialog();
        Bundle argumentos = new Bundle();
        argumentos.putString(RUTINA, rutina);
        dialog.setArguments(argumentos);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(
                requireContext(), R.style.ThemeOverlay_Marber_Dialog);
        View formulario = LayoutInflater.from(builder.getContext())
                .inflate(R.layout.dialog_agregar_ejercicio, null);
        TextInputLayout campoNombre = formulario.findViewById(R.id.campo_nombre_ejercicio);
        TextInputLayout campoSeries = formulario.findViewById(R.id.campo_series_ejercicio);
        TextInputLayout campoRepeticiones = formulario.findViewById(R.id.campo_repeticiones_ejercicio);
        TextInputEditText nombre = formulario.findViewById(R.id.input_nombre_ejercicio);
        TextInputEditText series = formulario.findViewById(R.id.input_series_ejercicio);
        TextInputEditText repeticiones = formulario.findViewById(R.id.input_repeticiones_ejercicio);
        TextView errorGuardado = formulario.findViewById(R.id.error_guardado_ejercicio);
        String rutina = requireArguments().getString(RUTINA);

        AlertDialog dialog = builder
                .setTitle(getString(R.string.agregar_ejercicio_titulo, rutina))
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
                    boolean guardado = ((Listener) requireActivity()).onAgregarEjercicio(
                            rutina, nombreIngresado, cantidadSeries, cantidadRepeticiones);
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

    private String texto(TextInputEditText campo) {
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
