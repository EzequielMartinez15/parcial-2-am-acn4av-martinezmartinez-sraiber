package parcial2.am.acn4av.martinezmartinez.sraiber.ui.activities;

import parcial2.am.acn4av.martinezmartinez.sraiber.Navegacion;

public class SeccionPendienteActivity extends HomeActivity {
    @Override protected void onCreate(android.os.Bundle state) {
        if (!getIntent().hasExtra("seccion")) getIntent().putExtra("seccion", Navegacion.EJERCICIOS);
        super.onCreate(state);
    }
}
