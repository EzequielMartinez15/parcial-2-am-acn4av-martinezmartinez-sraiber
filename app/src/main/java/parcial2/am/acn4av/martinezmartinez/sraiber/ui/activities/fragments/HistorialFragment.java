package parcial2.am.acn4av.martinezmartinez.sraiber.ui.activities.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.*;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.text.*;
import java.util.*;

import parcial2.am.acn4av.martinezmartinez.sraiber.FechasEntrenamiento;
import parcial2.am.acn4av.martinezmartinez.sraiber.R;
import parcial2.am.acn4av.martinezmartinez.sraiber.Sesion;
import parcial2.am.acn4av.martinezmartinez.sraiber.ui.activities.UiEntrenamiento;
import parcial2.am.acn4av.martinezmartinez.sraiber.model.Entrenamiento;
import parcial2.am.acn4av.martinezmartinez.sraiber.ui.activities.DetalleEntrenamientoActivity;

public class HistorialFragment extends DatosFragment {
    private Calendar mes = Calendar.getInstance();
    private long diaSeleccionado;
    private String rutina = "";
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        mes.set(Calendar.DAY_OF_MONTH, 1);
        if (b != null) { mes.setTimeInMillis(b.getLong("mes")); diaSeleccionado = b.getLong("dia"); rutina = b.getString("rutina", ""); }
    }
    @Override public void onSaveInstanceState(Bundle b) {
        b.putLong("mes", mes.getTimeInMillis()); b.putLong("dia", diaSeleccionado); b.putString("rutina", rutina);
        super.onSaveInstanceState(b);
    }
    @Override protected void mostrar() {
        contenido.removeAllViews(); if (esperando()) return;
        UiEntrenamiento.texto(contenido, getString(R.string.historial_titulo), 30, true);
        filtroRutina();
        LinearLayout fila = new LinearLayout(requireContext()); contenido.addView(fila);
        MaterialButton anterior = new MaterialButton(requireContext()); anterior.setText("‹"); anterior.setContentDescription(getString(R.string.mes_anterior));
        fila.addView(anterior, new LinearLayout.LayoutParams(UiEntrenamiento.dp(requireContext(), 56), -2));
        MaterialButton titulo = new MaterialButton(requireContext());
        titulo.setText(new SimpleDateFormat("MMMM yyyy", new Locale("es", "AR")).format(mes.getTime())); titulo.setAllCaps(false);
        titulo.setContentDescription(getString(R.string.mes_ano)); fila.addView(titulo, new LinearLayout.LayoutParams(0, -2, 1));
        MaterialButton siguiente = new MaterialButton(requireContext()); siguiente.setText("›"); siguiente.setContentDescription(getString(R.string.mes_siguiente));
        fila.addView(siguiente, new LinearLayout.LayoutParams(UiEntrenamiento.dp(requireContext(), 56), -2));
        anterior.setOnClickListener(v -> moverMes(-1)); siguiente.setOnClickListener(v -> moverMes(1));
        titulo.setOnClickListener(v -> elegirMes());
        calendario();
        UiEntrenamiento.texto(contenido, getString(R.string.calendario_leyenda), 12, false);
        if (diaSeleccionado == 0) {
            UiEntrenamiento.texto(contenido, getString(R.string.seleccionar_semana), 16, false); return;
        }
        long inicio = FechasEntrenamiento.semana(diaSeleccionado);
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM", Locale.getDefault());
        UiEntrenamiento.texto(contenido, getString(R.string.semana_rango, formato.format(new Date(inicio)),
                formato.format(new Date(FechasEntrenamiento.sumarDias(inicio, 6)))), 20, true);
        boolean hay = false;
        for (Entrenamiento e : datos.historial) if (coincide(e) && enMes(e.fin) && FechasEntrenamiento.enSemana(e.fin, diaSeleccionado)) {
            hay = true; LinearLayout card = UiEntrenamiento.tarjeta(contenido);
            UiEntrenamiento.texto(card, e.nombre, 22, true);
            UiEntrenamiento.texto(card, getString(R.string.sesion_resumen,
                    new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date(e.fin)),
                    e.minutos(), e.totalSeries()), 14, false);
            UiEntrenamiento.boton(card, R.string.ver_entrenamiento, false, v -> {
                Intent intent = new Intent(requireContext(), DetalleEntrenamientoActivity.class);
                intent.putExtra("entrenamiento_id", e.id); Sesion.agregarUsuario(intent); startActivity(intent);
            });
        }
        if (!hay) UiEntrenamiento.texto(contenido, getString(R.string.semana_sin_entrenamientos), 16, false);
    }
    private boolean coincide(Entrenamiento e) { return rutina.isEmpty() || rutina.equals(e.rutinaId); }
    private boolean enMes(long fecha) {
        Calendar c = Calendar.getInstance(); c.setTimeInMillis(fecha);
        return c.get(Calendar.MONTH) == mes.get(Calendar.MONTH) && c.get(Calendar.YEAR) == mes.get(Calendar.YEAR);
    }
    private void filtroRutina() {
        LinkedHashMap<String, String> opciones = new LinkedHashMap<>(); opciones.put("", getString(R.string.todas_rutinas));
        for (Entrenamiento e : datos.historial) opciones.put(e.rutinaId, e.nombre);
        List<String> ids = new ArrayList<>(opciones.keySet());
        if (!ids.contains(rutina)) rutina = "";
        Spinner spinner = new Spinner(requireContext()); spinner.setMinimumHeight(UiEntrenamiento.dp(requireContext(), 56));
        spinner.setContentDescription(getString(R.string.elegir_rutina));
        spinner.setAdapter(new ArrayAdapter<>(requireContext(), R.layout.item_selector_entrenamiento, new ArrayList<>(opciones.values())));
        spinner.setSelection(ids.indexOf(rutina)); contenido.addView(spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onNothingSelected(AdapterView<?> parent) { }
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int p, long id) {
                if (!rutina.equals(ids.get(p))) { rutina = ids.get(p); mostrar(); }
            }
        });
    }
    private void moverMes(int cantidad) { mes.add(Calendar.MONTH, cantidad); diaSeleccionado = 0; mostrar(); }
    private void elegirMes() {
        LinearLayout fila = new LinearLayout(requireContext());
        NumberPicker meses = new NumberPicker(requireContext()); meses.setMinValue(0); meses.setMaxValue(11);
        meses.setDisplayedValues(Arrays.copyOf(new DateFormatSymbols(new Locale("es", "AR")).getMonths(), 12)); meses.setValue(mes.get(Calendar.MONTH));
        NumberPicker anos = new NumberPicker(requireContext()); anos.setMinValue(2000); anos.setMaxValue(2100); anos.setValue(mes.get(Calendar.YEAR));
        fila.addView(meses, new LinearLayout.LayoutParams(0, -2, 1)); fila.addView(anos, new LinearLayout.LayoutParams(0, -2, 1));
        new MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_Marber_Dialog).setTitle(R.string.mes_ano).setView(fila)
                .setNegativeButton(R.string.ejercicio_cancelar, null).setPositiveButton(R.string.ejercicio_guardar, (d, w) -> {
                    mes.set(anos.getValue(), meses.getValue(), 1); diaSeleccionado = 0; mostrar();
                }).show();
    }
    private void calendario() {
        LinearLayout card = UiEntrenamiento.tarjeta(contenido);
        LinearLayout cabecera = new LinearLayout(requireContext()); card.addView(cabecera);
        for (String dia : getResources().getStringArray(R.array.dias_semana_cortos)) {
            TextView t = new TextView(requireContext()); t.setText(dia); t.setGravity(Gravity.CENTER);
            t.setTextColor(ContextCompat.getColor(requireContext(), R.color.marber_text_medium));
            cabecera.addView(t, new LinearLayout.LayoutParams(0, UiEntrenamiento.dp(requireContext(), 40), 1));
        }
        Calendar cursor = (Calendar)mes.clone(); cursor.set(Calendar.DAY_OF_MONTH, 1);
        int offset = (cursor.get(Calendar.DAY_OF_WEEK) + 5) % 7;
        int cantidad = cursor.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int fila = 0; fila < (offset + cantidad + 6) / 7; fila++) {
            LinearLayout row = new LinearLayout(requireContext()); card.addView(row);
            for (int columna = 0; columna < 7; columna++) {
                int numero = fila * 7 + columna - offset + 1;
                TextView t = new TextView(requireContext()); t.setGravity(Gravity.CENTER); t.setTextSize(15);
                row.addView(t, new LinearLayout.LayoutParams(0, UiEntrenamiento.dp(requireContext(), 48), 1));
                if (numero < 1 || numero > cantidad) continue;
                cursor.set(Calendar.DAY_OF_MONTH, numero); long fecha = FechasEntrenamiento.dia(cursor.getTimeInMillis());
                boolean entrenado = false;
                for (Entrenamiento e : datos.historial) if (coincide(e) && FechasEntrenamiento.mismoDia(e.fin, fecha)) entrenado = true;
                t.setText(String.valueOf(numero)); t.setTextColor(ContextCompat.getColor(requireContext(),
                        entrenado ? R.color.marber_background : R.color.marber_text_high));
                GradientDrawable fondo = new GradientDrawable(); fondo.setShape(GradientDrawable.OVAL);
                fondo.setColor(ContextCompat.getColor(requireContext(), entrenado ? R.color.marber_success
                        : diaSeleccionado != 0 && FechasEntrenamiento.enSemana(fecha, diaSeleccionado)
                        ? R.color.marber_primary_dark : R.color.marber_surface));
                if (diaSeleccionado != 0 && FechasEntrenamiento.enSemana(fecha, diaSeleccionado))
                    fondo.setStroke(UiEntrenamiento.dp(requireContext(), 2), ContextCompat.getColor(requireContext(), R.color.marber_primary_light));
                t.setBackground(fondo);
                String descripcion = DateFormat.getDateInstance(DateFormat.FULL).format(new Date(fecha));
                t.setContentDescription(entrenado ? getString(R.string.dia_entrenado, descripcion) : descripcion);
                t.setOnClickListener(v -> { diaSeleccionado = fecha; mostrar(); });
            }
        }
    }
}
