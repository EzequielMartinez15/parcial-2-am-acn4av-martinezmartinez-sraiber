package parcial2.am.acn4av.martinezmartinez.sraiber;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.FrameLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.activity.OnBackPressedCallback;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends PantallaDatosActivity {
    private int seleccion = Navegacion.HOME;
    private BottomNavigationView barra;
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        if (isFinishing()) return;
        LinearLayout root = findViewById(R.id.seccion_root);
        android.view.View scroll = findViewById(R.id.seccion_scroll);
        int posicion = root.indexOfChild(scroll);
        root.removeView(scroll);
        FrameLayout contenedor = new FrameLayout(this);
        contenedor.setId(R.id.contenedor_pestanas);
        root.addView(contenedor, posicion, new LinearLayout.LayoutParams(-1, 0, 1));
        seleccion = state == null ? getIntent().getIntExtra("seccion", Navegacion.HOME)
                : state.getInt("pestana", Navegacion.HOME);
        if (seleccion < Navegacion.EJERCICIOS || seleccion > Navegacion.PERFIL) seleccion = Navegacion.HOME;
        barra = Navegacion.instalar(this, root, seleccion);
        barra.setId(R.id.barra_principal);
        barra.setOnItemSelectedListener(item -> { cambiar(item.getItemId()); return true; });
        cambiar(seleccion);
        datos.cambios.observe(this, cambio -> estadoDatos());
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                if (seleccion != Navegacion.HOME) barra.setSelectedItemId(Navegacion.HOME);
                else { setEnabled(false); getOnBackPressedDispatcher().onBackPressed(); setEnabled(true); }
            }
        });
    }
    private void cambiar(int id) {
        if (getSupportFragmentManager().isStateSaved()) return;
        String etiqueta = "pestana_" + id;
        Fragment destino = getSupportFragmentManager().findFragmentByTag(etiqueta);
        FragmentTransaction cambio = getSupportFragmentManager().beginTransaction().setReorderingAllowed(true);
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment.getId() == R.id.contenedor_pestanas && fragment != destino) {
                cambio.hide(fragment).setMaxLifecycle(fragment, Lifecycle.State.STARTED);
            }
        }
        if (destino == null) {
            if (id == Navegacion.HOME) destino = new InicioFragment();
            else if (id == Navegacion.RUTINAS) destino = new RutinasFragment();
            else if (id == Navegacion.HISTORIAL) destino = new HistorialFragment();
            else if (id == Navegacion.EJERCICIOS) destino = new EjerciciosFragment();
            else destino = new PerfilFragment();
            cambio.add(R.id.contenedor_pestanas, destino, etiqueta);
        } else cambio.show(destino);
        cambio.setMaxLifecycle(destino, Lifecycle.State.RESUMED).setPrimaryNavigationFragment(destino).commitNow();
        seleccion = id;
    }
    // Dejé que las pestañas puedan pedir cambiar de sección, por ejemplo Perfil hacia Historial.
    public void irA(int seccion) { barra.setSelectedItemId(seccion); }
    @Override protected void onSaveInstanceState(Bundle out) {
        out.putInt("pestana", seleccion); super.onSaveInstanceState(out);
    }
    @Override protected void mostrar() { }
}
