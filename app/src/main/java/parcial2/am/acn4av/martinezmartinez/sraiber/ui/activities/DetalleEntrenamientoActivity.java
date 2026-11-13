package parcial2.am.acn4av.martinezmartinez.sraiber.ui.activities;

import android.os.Bundle;
import android.widget.*;
import androidx.core.content.ContextCompat;
import java.text.*;
import java.util.*;

import parcial2.am.acn4av.martinezmartinez.sraiber.R;
import parcial2.am.acn4av.martinezmartinez.sraiber.model.Entrenamiento;

public class DetalleEntrenamientoActivity extends PantallaDatosActivity {
    @Override protected void onCreate(Bundle b) { super.onCreate(b); if (!isFinishing()) observar(); }
    @Override protected void mostrar() {
        contenido.removeAllViews(); if (esperando()) return;
        UiEntrenamiento.boton(contenido, R.string.volver, false, v -> finish());
        Entrenamiento e = datos.buscarEntrenamiento(getIntent().getStringExtra("entrenamiento_id"));
        if (e == null) { UiEntrenamiento.texto(contenido, getString(R.string.sin_entrenamiento), 18, true); return; }
        UiEntrenamiento.texto(contenido, e.nombre, 30, true);
        UiEntrenamiento.texto(contenido, getString(R.string.sesion_resumen,
                new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date(e.fin)),
                e.minutos(), e.totalSeries()), 16, false);
        UiEntrenamiento.texto(contenido, getString(R.string.comparacion_leyenda), 14, false);
        for (Entrenamiento.Movimiento m : e.ejercicios) {
            LinearLayout card = UiEntrenamiento.tarjeta(contenido); UiEntrenamiento.foto(card, m.nombre, m.foto);
            UiEntrenamiento.texto(card, m.nombre, 24, true);
            Entrenamiento.Movimiento previo = anterior(e, m);
            if (previo == null) UiEntrenamiento.texto(card, getString(R.string.sin_comparacion), 14, false);
            TextView cantidad = UiEntrenamiento.texto(card, getString(R.string.series_comparadas,
                    previo == null ? "" : flecha(m.series.size(), previo.series.size()), m.series.size()), 16, true);
            if (previo != null) cantidad.setTextColor(color(m.series.size(), previo.series.size()));
            for (int i = 0; i < m.series.size(); i++) {
                Entrenamiento.Serie s = m.series.get(i);
                if (!s.valida()) continue;
                Entrenamiento.Serie antes = previo != null && i < previo.series.size() && previo.series.get(i).valida() ? previo.series.get(i) : null;
                LinearLayout fila = new LinearLayout(this); fila.setOrientation(LinearLayout.VERTICAL); card.addView(fila);
                UiEntrenamiento.texto(fila, getString(R.string.serie_numero, i + 1), 14, true);
                String peso = NumberFormat.getNumberInstance().format(s.kilos());
                TextView kg = UiEntrenamiento.texto(fila, (antes == null ? "" : flecha(s.kilos(), antes.kilos()) + " ")
                        + getString(R.string.valor_peso, peso), 18, true);
                TextView reps = UiEntrenamiento.texto(fila, (antes == null ? "" : flecha(s.reps(), antes.reps()) + " ")
                        + getString(R.string.valor_repeticiones, s.reps()), 18, true);
                if (antes != null) { kg.setTextColor(color(s.kilos(), antes.kilos())); reps.setTextColor(color(s.reps(), antes.reps())); }
                else if (previo != null) UiEntrenamiento.texto(fila, getString(R.string.serie_nueva), 12, false);
            }
            if (!m.notas.isEmpty()) UiEntrenamiento.texto(card, getString(R.string.notas_guardadas, m.notas), 16, false);
        }
        if (!e.notas.isEmpty()) UiEntrenamiento.texto(contenido, getString(R.string.notas_guardadas, e.notas), 16, false);
    }
    private Entrenamiento.Movimiento anterior(Entrenamiento actual, Entrenamiento.Movimiento buscado) {
        // Busqué la última sesión anterior de ese ejercicio, aunque haya sido en otra rutina.
        for (Entrenamiento e : datos.historial) if (e.fin < actual.fin && !e.id.equals(actual.id))
            for (Entrenamiento.Movimiento m : e.ejercicios) if ((!m.ejercicioId.isEmpty() && !buscado.ejercicioId.isEmpty())
                    ? m.ejercicioId.equals(buscado.ejercicioId) && m.clave.substring(m.clave.lastIndexOf('#') + 1)
                            .equals(buscado.clave.substring(buscado.clave.lastIndexOf('#') + 1))
                    : m.clave.equals(buscado.clave)) return m;
        return null;
    }
    private String flecha(double a, double b) { return a > b ? "↑" : a < b ? "↓" : "="; }
    private int color(double a, double b) { return ContextCompat.getColor(this,
            a > b ? R.color.marber_success : a < b ? R.color.marber_error : R.color.marber_text_medium); }
}
