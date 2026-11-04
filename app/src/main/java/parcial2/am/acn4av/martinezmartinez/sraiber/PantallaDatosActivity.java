package parcial2.am.acn4av.martinezmartinez.sraiber;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.*;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import parcial2.am.acn4av.martinezmartinez.sraiber.Repository.DatosUsuario;

public abstract class PantallaDatosActivity extends AppCompatActivity {
    protected LinearLayout contenido;
    protected DatosUsuario datos;
    @Override protected void onCreate(Bundle estado) {
        super.onCreate(estado);
        if (!Sesion.exigir(this)) return;
        EdgeToEdge.enable(this); setContentView(R.layout.activity_seccion);
        LinearLayout root = findViewById(R.id.seccion_root);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            androidx.core.graphics.Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            androidx.core.graphics.Insets teclado = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(barras.left, barras.top, barras.right, Math.max(barras.bottom, teclado.bottom)); return insets;
        });
        WindowCompat.getInsetsController(getWindow(), root).setAppearanceLightStatusBars(false);
        WindowCompat.getInsetsController(getWindow(), root).setAppearanceLightNavigationBars(false);
        contenido = findViewById(R.id.seccion_contenido);
        datos = DatosUsuario.obtener(this);
        findViewById(R.id.seccion_salir).setOnClickListener(v -> new MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_Marber_Dialog)
                .setTitle(R.string.cerrar_sesion).setMessage(R.string.confirmar_cerrar_sesion)
                .setNegativeButton(R.string.ejercicio_cancelar, null)
                .setPositiveButton(R.string.confirmar_si, (d, w) -> Sesion.cerrar(this)).show());
    }
    @Override protected void onStart() { super.onStart(); if (!isFinishing()) Sesion.exigir(this); }
    protected void observar() {
        datos.cambios.observe(this, cambio -> { estadoDatos(); mostrar(); });
    }
    protected void estadoDatos() {
        TextView estado = findViewById(R.id.seccion_estado);
        String error = datos.error.getValue();
        estado.setText(error != null && !error.isEmpty() ? error : getString(!datos.listo ? R.string.cargando_datos
                : datos.pendientes ? R.string.datos_pendientes : datos.desdeCache ? R.string.datos_cache : R.string.datos_sincronizados));
        estado.setOnClickListener(v -> datos.reintentar(this));
    }
    protected boolean esperando() {
        if (datos.listo) return false;
        UiEntrenamiento.texto(contenido, getString(R.string.cargando_datos), 20, true);
        UiEntrenamiento.boton(contenido, R.string.reintentar, false, v -> datos.reintentar(this));
        return true;
    }
    protected abstract void mostrar();
}
