package parcial2.am.acn4av.martinezmartinez.sraiber;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import java.util.*;
import java.text.SimpleDateFormat;
import parcial2.am.acn4av.martinezmartinez.sraiber.model.*;

public class InicioFragment extends DatosFragment {
    private String seleccion = "";
    private boolean empezando;
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        if (b != null) seleccion = b.getString("rutina", "");
    }
    @Override public void onSaveInstanceState(Bundle b) { b.putString("rutina", seleccion); super.onSaveInstanceState(b); }
    @Override protected void mostrar() {
        contenido.removeAllViews(); if (esperando()) return;
        UiEntrenamiento.texto(contenido, getString(R.string.entrenar_titulo), 32, true);
        UiEntrenamiento.texto(contenido, getString(R.string.entrenar_subtitulo), 16, false);
        if (datos.activo != null) {
            LinearLayout card = UiEntrenamiento.tarjeta(contenido);
            UiEntrenamiento.texto(card, getString(R.string.entrenamiento_pendiente), 14, false);
            UiEntrenamiento.texto(card, datos.activo.nombre, 24, true);
            UiEntrenamiento.texto(card, getString(R.string.progreso_entrenamiento,
                    datos.activo.seriesHechas(), datos.activo.totalSeries()), 14, false);
            UiEntrenamiento.boton(card, R.string.continuar_entrenamiento, true, v -> abrirActivo(datos.activo.id));
        } else {
            for (Entrenamiento e : datos.historial) if (FechasEntrenamiento.mismoDia(e.fin, System.currentTimeMillis())) {
                LinearLayout hoy = UiEntrenamiento.tarjeta(contenido);
                UiEntrenamiento.texto(hoy, getString(R.string.entrenamiento_hoy, e.nombre), 22, true);
                UiEntrenamiento.boton(hoy, R.string.ver_entrenamiento, false, v -> abrirDetalle(e.id)); break;
            }
            selectorRutina();
        }
        UiEntrenamiento.texto(contenido, getString(R.string.ultimos_entrenamientos), 22, true);
        if (datos.historial.isEmpty()) UiEntrenamiento.texto(contenido, getString(R.string.historial_vacio), 16, false);
        for (int i = 0; i < Math.min(3, datos.historial.size()); i++) {
            Entrenamiento e = datos.historial.get(i);
            LinearLayout card = UiEntrenamiento.tarjeta(contenido);
            UiEntrenamiento.texto(card, e.nombre, 22, true);
            UiEntrenamiento.texto(card, getString(R.string.sesion_resumen,
                    new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date(e.fin)),
                    e.minutos(), e.totalSeries()), 14, false);
            UiEntrenamiento.boton(card, R.string.ver_entrenamiento, false, v -> abrirDetalle(e.id));
        }
    }
    private void selectorRutina() {
        LinearLayout card = UiEntrenamiento.tarjeta(contenido);
        UiEntrenamiento.texto(card, getString(R.string.elegir_rutina), 22, true);
        List<Rutina> rutinas = datos.rutinasConEstadisticas();
        if (rutinas.isEmpty()) { UiEntrenamiento.texto(card, getString(R.string.sin_rutinas_entrenar), 16, false); return; }
        List<String> nombres = new ArrayList<>(); int posicion = 0;
        for (int i = 0; i < rutinas.size(); i++) {
            nombres.add(rutinas.get(i).getNombre()); if (rutinas.get(i).getId().equals(seleccion)) posicion = i;
        }
        Spinner spinner = new Spinner(requireContext()); spinner.setMinimumHeight(UiEntrenamiento.dp(requireContext(), 56));
        spinner.setContentDescription(getString(R.string.elegir_rutina));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.item_selector_entrenamiento, nombres);
        spinner.setAdapter(adapter); spinner.setSelection(posicion); card.addView(spinner);
        TextView resumen = UiEntrenamiento.texto(card, "", 14, false);
        MaterialButton empezar = UiEntrenamiento.boton(card, R.string.empezar_entrenamiento, true, v -> {
            Rutina r = datos.buscarRutina(seleccion); if (r == null || empezando) return;
            if (r.getEjercicios().isEmpty()) {
                Snackbar.make(contenido, R.string.rutina_vacia_entrenar, Snackbar.LENGTH_LONG).show(); return;
            }
            empezando = true; v.setEnabled(false);
            datos.empezar(r).addOnCompleteListener(requireActivity(), tarea -> {
                empezando = false;
                if (!isAdded() || getView() == null) return;
                if (tarea.isSuccessful()) abrirActivo(tarea.getResult().id);
                else { mostrar(); Snackbar.make(contenido, R.string.datos_error_red, Snackbar.LENGTH_LONG).show(); }
            });
        });
        empezar.setEnabled(!empezando);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onNothingSelected(AdapterView<?> parent) { }
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int p, long id) {
                Rutina r = rutinas.get(p); seleccion = r.getId(); int series = 0;
                for (Ejercicio e : r.getEjercicios()) series += e.getSeries();
                resumen.setText(getString(R.string.rutina_resumen, r.getEjercicios().size(), series));
            }
        });
        seleccion = rutinas.get(posicion).getId();
    }
    private void abrirActivo(String id) {
        Intent intent = new Intent(requireContext(), EntrenamientoActivity.class); intent.putExtra("entrenamiento_id", id);
        Sesion.agregarUsuario(intent); startActivity(intent);
    }
    private void abrirDetalle(String id) {
        Intent intent = new Intent(requireContext(), DetalleEntrenamientoActivity.class); intent.putExtra("entrenamiento_id", id);
        Sesion.agregarUsuario(intent); startActivity(intent);
    }
}
