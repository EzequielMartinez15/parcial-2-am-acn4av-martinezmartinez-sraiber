package parcial2.am.acn4av.martinezmartinez.sraiber;

import android.os.Bundle;
import android.text.*;
import android.view.View;
import android.widget.*;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import java.util.*;
import parcial2.am.acn4av.martinezmartinez.sraiber.model.Entrenamiento;

public class EntrenamientoActivity extends PantallaDatosActivity {
    private EntrenamientoViewModel modelo;
    private boolean dibujado;
    private TextView progreso, guardando;
    private final List<View> controles = new ArrayList<>();

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b); if (isFinishing()) return;
        modelo = new ViewModelProvider(this).get(EntrenamientoViewModel.class);
        findViewById(R.id.seccion_salir).setVisibility(View.GONE);
        modelo.cambios.observe(this, cambio -> actualizarOperacion());
        observar();
    }
    @Override protected void mostrar() {
        if (dibujado) return;
        contenido.removeAllViews(); if (esperando()) return;
        String id = getIntent().getStringExtra("entrenamiento_id");
        if (modelo.entrenamiento == null) {
            if (datos.activo == null || !datos.activo.id.equals(id)) {
                UiEntrenamiento.texto(contenido, getString(R.string.sin_entrenamiento), 18, true);
                UiEntrenamiento.boton(contenido, R.string.volver, false, v -> finish()); return;
            }
            modelo.iniciar(datos, datos.activo);
        }
        dibujado = true;
        UiEntrenamiento.boton(contenido, R.string.conservar_borrador, false, v -> { modelo.persistir(); finish(); });
        UiEntrenamiento.texto(contenido, modelo.entrenamiento.nombre, 30, true);
        UiEntrenamiento.texto(contenido, getString(R.string.peso_corporal_ayuda), 14, false);
        progreso = UiEntrenamiento.texto(contenido, "", 18, true);
        guardando = UiEntrenamiento.texto(contenido, getString(R.string.guardar_pendiente), 14, false);
        for (Entrenamiento.Movimiento m : modelo.entrenamiento.ejercicios) {
            LinearLayout card = UiEntrenamiento.tarjeta(contenido);
            UiEntrenamiento.foto(card, m.nombre); UiEntrenamiento.texto(card, m.nombre, 24, true);
            for (int i = 0; i < m.series.size(); i++) agregarSerie(card, m.series.get(i), i + 1);
            TextInputEditText notas = UiEntrenamiento.campo(card, R.string.notas_ejercicio, m.notas,
                    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            notas.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1000)});
            observarTexto(notas, texto -> { m.notas = texto; modelo.modificar(); }); controles.add(notas);
        }
        TextInputEditText notas = UiEntrenamiento.campo(contenido, R.string.notas_entrenamiento, modelo.entrenamiento.notas,
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        notas.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2000)});
        observarTexto(notas, texto -> { modelo.entrenamiento.notas = texto; modelo.modificar(); }); controles.add(notas);
        controles.add(UiEntrenamiento.boton(contenido, R.string.finalizar_entrenamiento, true, v -> {
            if (!modelo.entrenamiento.completo()) { Snackbar.make(contenido, R.string.entrenamiento_incompleto, Snackbar.LENGTH_LONG).show(); return; }
            new MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_Marber_Dialog)
                    .setTitle(R.string.finalizar_entrenamiento).setMessage(R.string.finalizar_confirmacion)
                    .setNegativeButton(R.string.ejercicio_cancelar, null)
                    .setPositiveButton(R.string.ejercicio_guardar, (d, w) -> modelo.finalizar()).show();
        }));
        controles.add(UiEntrenamiento.boton(contenido, R.string.descartar_entrenamiento, false, v ->
                new MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_Marber_Dialog)
                        .setTitle(R.string.descartar_entrenamiento).setMessage(R.string.descartar_confirmacion)
                        .setNegativeButton(R.string.ejercicio_cancelar, null)
                        .setPositiveButton(R.string.eliminar, (d, w) -> modelo.descartar()).show()));
        actualizarOperacion();
    }
    private void agregarSerie(LinearLayout padre, Entrenamiento.Serie s, int numero) {
        UiEntrenamiento.texto(padre, getString(R.string.serie_numero, numero), 16, true);
        LinearLayout fila = new LinearLayout(this); padre.addView(fila);
        LinearLayout izquierda = UiEntrenamiento.columna(this), derecha = UiEntrenamiento.columna(this);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, -2, 1); p.setMarginEnd(UiEntrenamiento.dp(this, 8));
        fila.addView(izquierda, p); fila.addView(derecha, new LinearLayout.LayoutParams(0, -2, 1));
        TextInputEditText peso = UiEntrenamiento.campo(izquierda, R.string.peso_kg, s.peso,
                InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        TextInputEditText reps = UiEntrenamiento.campo(derecha, R.string.repeticiones_corto, s.repeticiones, InputType.TYPE_CLASS_NUMBER);
        peso.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});
        reps.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        MaterialCheckBox hecha = new MaterialCheckBox(this); hecha.setText(R.string.serie_hecha); hecha.setChecked(s.realizada);
        hecha.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.marber_text_high));
        padre.addView(hecha); controles.add(peso); controles.add(reps); controles.add(hecha);
        hecha.setOnCheckedChangeListener((button, checked) -> {
            if (checked && !s.valida()) {
                hecha.setChecked(false); Snackbar.make(contenido, R.string.serie_error, Snackbar.LENGTH_LONG).show(); return;
            }
            s.realizada = checked; modelo.modificar(); actualizarProgreso();
        });
        observarTexto(peso, texto -> { s.peso = texto; if (!s.valida()) hecha.setChecked(false); modelo.modificar(); });
        observarTexto(reps, texto -> { s.repeticiones = texto; if (!s.valida()) hecha.setChecked(false); modelo.modificar(); });
    }
    private interface CambioTexto { void cambiar(String texto); }
    private void observarTexto(TextInputEditText input, CambioTexto cambio) {
        input.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            public void onTextChanged(CharSequence s, int start, int before, int count) { cambio.cambiar(s.toString()); }
            public void afterTextChanged(Editable e) { }
        });
    }
    private void actualizarProgreso() {
        if (progreso != null) progreso.setText(getString(R.string.progreso_entrenamiento,
                modelo.entrenamiento.seriesHechas(), modelo.entrenamiento.totalSeries()));
    }
    private void actualizarOperacion() {
        if (modelo == null) return;
        if (modelo.mensaje != 0) {
            Toast.makeText(this, modelo.mensaje, Toast.LENGTH_LONG).show(); modelo.mensaje = 0;
        }
        if (modelo.terminado) { finish(); return; }
        for (View control : controles) control.setEnabled(!modelo.ocupado);
        if (guardando != null) guardando.setVisibility(modelo.ocupado ? View.VISIBLE : View.GONE);
        actualizarProgreso();
    }
    @Override protected void onPause() { if (modelo != null) modelo.persistir(); super.onPause(); }
}
