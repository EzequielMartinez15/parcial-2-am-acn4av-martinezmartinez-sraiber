package parcial2.am.acn4av.martinezmartinez.sraiber.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import parcial2.am.acn4av.martinezmartinez.sraiber.R;
import parcial2.am.acn4av.martinezmartinez.sraiber.Repository.RutinaRepository;
import parcial2.am.acn4av.martinezmartinez.sraiber.Sesion;
import parcial2.am.acn4av.martinezmartinez.sraiber.ui.adapterts.EjercicioDetalleRutinaAdapter;
import parcial2.am.acn4av.martinezmartinez.sraiber.model.EjercicioCatalogo;
import parcial2.am.acn4av.martinezmartinez.sraiber.model.EjercicioRutina;
import parcial2.am.acn4av.martinezmartinez.sraiber.model.Rutina;
import parcial2.am.acn4av.martinezmartinez.sraiber.ui.dialogs.AgregarModificarEjercicioEnRutinaDialog;

public class DetalleRutinaActivity extends AppCompatActivity implements
        AgregarModificarEjercicioEnRutinaDialog.ListenerAgregarEjercicio,
        AgregarModificarEjercicioEnRutinaDialog.ListenerEditarEjercicio {
    private View main;
    private RecyclerView rvEjercicios;
    private EjercicioDetalleRutinaAdapter adapter;
    private Rutina rutinaActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Sesion.exigir(this)) return;
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detalle_rutina);
        main = findViewById(R.id.detalle_root);
        WindowCompat.getInsetsController(getWindow(), main).setAppearanceLightStatusBars(false);
        WindowCompat.getInsetsController(getWindow(), main).setAppearanceLightNavigationBars(false);
        ViewCompat.setOnApplyWindowInsetsListener(main, (v, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(barras.left, barras.top, barras.right, barras.bottom);
            return insets;
        });
        findViewById(R.id.btn_volver).setOnClickListener(v -> finish());
        // Esperé la carga de Firestore antes de buscar la rutina al recrear la pantalla.
        parcial2.am.acn4av.martinezmartinez.sraiber.Repository.DatosUsuario datos =
                parcial2.am.acn4av.martinezmartinez.sraiber.Repository.DatosUsuario.obtener(this);
        datos.cambios.observe(this, cambio -> { if (datos.listo) cargarRutina(); });
        datos.error.observe(this, error -> {
            if (error != null && !error.isEmpty()) Snackbar.make(main, error, Snackbar.LENGTH_LONG).show();
        });
    }

    private void cargarRutina() {
        Rutina anterior = rutinaActual;
        String nombre = getIntent().getStringExtra("nombre_rutina");
        String id = getIntent().getStringExtra("rutina_id");
        rutinaActual = null;
        for (Rutina rutina : RutinaRepository.getRutinas(this)) {
            if (id != null ? rutina.getId().equals(id) : rutina.getNombre().equals(nombre)) rutinaActual = rutina;
        }
        if (rutinaActual == null) {
            finish();
            return;
        }
        if (anterior != null && parcial2.am.acn4av.martinezmartinez.sraiber.Repository.DatosUsuario.datosRutina(anterior)
                .equals(parcial2.am.acn4av.martinezmartinez.sraiber.Repository.DatosUsuario.datosRutina(rutinaActual))) {
            rutinaActual = anterior;
            return;
        }
        ((TextView) findViewById(R.id.txtTituloRutina)).setText(rutinaActual.getNombre());
        findViewById(R.id.btn_volver).setOnClickListener(v -> finish());
        rvEjercicios = findViewById(R.id.rvEjercicios);
        rvEjercicios.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EjercicioDetalleRutinaAdapter(this, rutinaActual.getEjercicios(),
                this::confirmarBorradoEjercicio, ejercicio -> {
                    if (getSupportFragmentManager().findFragmentByTag("ejercicio") == null) {
                        AgregarModificarEjercicioEnRutinaDialog.crearEditar(rutinaActual.getNombre(), ejercicio,
                                rutinaActual.getEjercicios().indexOf(ejercicio))
                                .show(getSupportFragmentManager(), "ejercicio");
                    }
                });
        rvEjercicios.setAdapter(adapter);
        findViewById(R.id.btnAgregarEjercicio).setOnClickListener(v -> {
            if (getSupportFragmentManager().findFragmentByTag("ejercicio") == null) {
                AgregarModificarEjercicioEnRutinaDialog.crearAgregar(rutinaActual.getNombre())
                        .show(getSupportFragmentManager(), "ejercicio");
            }
        });
        findViewById(R.id.btnBorrarRutina).setOnClickListener(v -> confirmarBorradoRutina());
        actualizarResumen();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!isFinishing()) Sesion.exigir(this);
    }

    private void actualizarResumen() {
        int cantidad = rutinaActual.getEjercicios().size();
        ((TextView) findViewById(R.id.txtResumenRutina)).setText(
                getResources().getQuantityString(R.plurals.cantidad_ejercicios, cantidad, cantidad));
        findViewById(R.id.detalle_vacio).setVisibility(cantidad == 0 ? View.VISIBLE : View.GONE);
    }

    private void confirmarBorradoEjercicio(EjercicioRutina ejercicio) {
        new MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_Marber_Dialog)
                .setTitle(R.string.eliminar_ejercicio)
                .setMessage(getString(R.string.confirmar_eliminar_ejercicio, ejercicio.getNombre()))
                .setNegativeButton(R.string.ejercicio_cancelar, null)
                .setPositiveButton(R.string.eliminar, (dialog, which) -> {
                    int posicion = rutinaActual.getEjercicios().indexOf(ejercicio);
                    if (posicion < 0) return;
                    RutinaRepository.eliminarEjercicio(this, rutinaActual, ejercicio)
                            .addOnSuccessListener(this, v -> Snackbar.make(main, R.string.ejercicio_eliminado, Snackbar.LENGTH_SHORT).show());
                    adapter.notifyItemRemoved(posicion);
                    actualizarResumen();
                    Snackbar.make(main, R.string.operacion_guardando, Snackbar.LENGTH_SHORT).show();
                }).show();
    }

    private void confirmarBorradoRutina() {
        new MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_Marber_Dialog)
                .setTitle(R.string.txt_borrar_rutina)
                .setMessage(getString(R.string.confirmar_eliminar_rutina, rutinaActual.getNombre()))
                .setNegativeButton(R.string.ejercicio_cancelar, null)
                .setPositiveButton(R.string.eliminar, (dialog, which) -> {
                    RutinaRepository.eliminarRutina(this, rutinaActual);
                    // Usé un Toast porque la pantalla se cierra y un Snackbar desaparecería con ella.
                    android.widget.Toast.makeText(getApplicationContext(), R.string.rutina_eliminada,
                            android.widget.Toast.LENGTH_SHORT).show();
                    finish();
                }).show();
    }

    @Override
    public boolean onAgregarEjercicio(String rutina, EjercicioCatalogo ejercicioCatalogo, int series, int repeticiones) {
        if (!rutinaActual.getNombre().equals(rutina)) return false;
        try {
            com.google.android.gms.tasks.Task<Void> guardado = RutinaRepository.agregarEjercicio(this, rutinaActual, new EjercicioRutina(ejercicioCatalogo.getId(), ejercicioCatalogo.getNombre(), series,
                    repeticiones, ejercicioCatalogo.getNombreFoto()));
            int posicion = rutinaActual.getEjercicios().size() - 1;
            adapter.notifyItemInserted(posicion);
            actualizarResumen();
            rvEjercicios.smoothScrollToPosition(posicion);
            Snackbar.make(main, R.string.operacion_guardando, Snackbar.LENGTH_SHORT).show();
            guardado.addOnSuccessListener(this, v -> Snackbar.make(main, R.string.ejercicio_agregado, Snackbar.LENGTH_SHORT).show());
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    @Override
    public boolean onEditarEjercicio(int posicion, String idEnFirebase, String nombre, int series, int repeticiones) {
        if (posicion < 0 || posicion >= rutinaActual.getEjercicios().size()) return false;
        try {
            // Recuperé el ejercicio de la lista actual para soportar la rotación del formulario.
            com.google.android.gms.tasks.Task<Void> guardado = RutinaRepository.editarEjercicio(this, rutinaActual, rutinaActual.getEjercicios().get(posicion),
                    idEnFirebase, nombre, series, repeticiones);
            adapter.notifyItemChanged(posicion);
            Snackbar.make(main, R.string.operacion_guardando, Snackbar.LENGTH_SHORT).show();
            guardado.addOnSuccessListener(this, v -> Snackbar.make(main, R.string.ejercicio_editado, Snackbar.LENGTH_SHORT).show());
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }
}
