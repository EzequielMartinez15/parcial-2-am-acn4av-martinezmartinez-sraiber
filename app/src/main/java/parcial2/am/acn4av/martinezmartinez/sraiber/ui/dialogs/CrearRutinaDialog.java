package parcial2.am.acn4av.martinezmartinez.sraiber.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.text.InputType;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import java.util.*;

import parcial2.am.acn4av.martinezmartinez.sraiber.R;
import parcial2.am.acn4av.martinezmartinez.sraiber.Repository.*;
import parcial2.am.acn4av.martinezmartinez.sraiber.ui.activities.UiEntrenamiento;
import parcial2.am.acn4av.martinezmartinez.sraiber.model.*;

public class CrearRutinaDialog extends DialogFragment {
    public static class Estado extends ViewModel {
        String nombre = "", series = "3", reps = "10";
        Set<String> elegidos = new HashSet<>();
        Task<Void> guardado;
    }
    private Estado estado;
    private TextInputEditText nombre, series, reps;
    private final Map<String, CheckBox> opciones = new LinkedHashMap<>();
    @NonNull @Override public Dialog onCreateDialog(Bundle saved) {
        estado = new ViewModelProvider(this).get(Estado.class);
        if (saved != null && estado.guardado == null) {
            estado.nombre = saved.getString("nombre", ""); estado.series = saved.getString("series", "3");
            estado.reps = saved.getString("reps", "10");
            estado.elegidos.addAll(saved.getStringArrayList("elegidos") == null ? Collections.emptyList() : saved.getStringArrayList("elegidos"));
        }
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_Marber_Dialog);
        LinearLayout contenido = UiEntrenamiento.columna(builder.getContext());
        int margen = UiEntrenamiento.dp(requireContext(), 20); contenido.setPadding(margen, margen, margen, margen);
        ScrollView scroll = new ScrollView(builder.getContext()); scroll.addView(contenido);
        nombre = UiEntrenamiento.campo(contenido, R.string.nombre_rutina_nueva, estado.nombre, InputType.TYPE_CLASS_TEXT);
        UiEntrenamiento.texto(contenido, getString(R.string.rutina_nueva_ayuda), 14, false);
        series = UiEntrenamiento.campo(contenido, R.string.ejercicio_series, estado.series, InputType.TYPE_CLASS_NUMBER);
        reps = UiEntrenamiento.campo(contenido, R.string.ejercicio_repeticiones, estado.reps, InputType.TYPE_CLASS_NUMBER);
        opciones.clear();
        List<EjercicioCatalogo> catalogo = EjercicioRepository.todos();
        for (EjercicioCatalogo ejercicio : catalogo) {
            CheckBox check = new CheckBox(builder.getContext()); check.setText(ejercicio.getNombre());
            check.setTextColor(requireContext().getColor(R.color.marber_text_high));
            check.setButtonTintList(android.content.res.ColorStateList.valueOf(requireContext().getColor(R.color.marber_primary_light)));
            check.setChecked(estado.elegidos.contains(ejercicio.getId()));
            opciones.put(ejercicio.getId(), check); contenido.addView(check);
        }
        AlertDialog dialog = builder.setTitle(R.string.crear_rutina).setView(scroll)
                .setNegativeButton(R.string.ejercicio_cancelar, null)
                .setPositiveButton(R.string.ejercicio_guardar, null).create();
        dialog.setOnShowListener(v -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(click -> {
                conservar();
                if (estado.guardado != null && !estado.guardado.isComplete()) return;
                DatosUsuario datos = DatosUsuario.obtener(requireContext());
                if (!datos.listo) { nombre.setError(getString(R.string.cargando_datos)); return; }
                if (estado.nombre.isEmpty() || estado.nombre.length() > 80 || datos.rutinas.stream()
                        .anyMatch(r -> r.getNombre().equalsIgnoreCase(estado.nombre))) {
                    nombre.setError(getString(R.string.rutina_nombre_error)); return;
                }
                int cantidadSeries, repeticiones;
                try {
                    cantidadSeries = Integer.parseInt(estado.series); repeticiones = Integer.parseInt(estado.reps);
                    if (cantidadSeries < 1 || cantidadSeries > 50 || repeticiones < 1 || repeticiones > 10000) throw new NumberFormatException();
                } catch (NumberFormatException e) { series.setError(getString(R.string.ejercicio_numero_error)); return; }
                List<EjercicioRutina> ejercicios = new ArrayList<>();
                for (EjercicioCatalogo e : catalogo) if (estado.elegidos.contains(e.getId()))
                    ejercicios.add(new EjercicioRutina(e.getId(), e.getNombre(), cantidadSeries, repeticiones, e.getNombreFoto()));
                if (ejercicios.isEmpty()) { nombre.setError(getString(R.string.rutina_ejercicios_error)); return; }
                Rutina rutina = new Rutina(estado.nombre, ejercicios, 0, new Date(0));
                rutina.setId(UUID.randomUUID().toString());
                estado.guardado = datos.guardarRutina(rutina);
                observar(dialog);
            });
            if (estado.guardado != null) observar(dialog);
        });
        return dialog;
    }
    private void observar(AlertDialog dialog) {
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        estado.guardado.addOnCompleteListener(t -> {
            if (!isAdded() || getDialog() != dialog) return;
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
            if (t.isSuccessful()) {
                Toast.makeText(requireContext(), R.string.rutina_creada, Toast.LENGTH_SHORT).show(); dismiss();
            } else nombre.setError(getString(R.string.operacion_error));
        });
    }
    private void conservar() {
        estado.nombre = nombre.getText().toString().trim(); estado.series = series.getText().toString(); estado.reps = reps.getText().toString();
        estado.elegidos.clear(); for (Map.Entry<String, CheckBox> e : opciones.entrySet()) if (e.getValue().isChecked()) estado.elegidos.add(e.getKey());
    }
    @Override public void onSaveInstanceState(@NonNull Bundle out) {
        conservar(); out.putString("nombre", estado.nombre); out.putString("series", estado.series); out.putString("reps", estado.reps);
        out.putStringArrayList("elegidos", new ArrayList<>(estado.elegidos)); super.onSaveInstanceState(out);
    }
    @Override public void onDestroyView() { conservar(); super.onDestroyView(); }
}
