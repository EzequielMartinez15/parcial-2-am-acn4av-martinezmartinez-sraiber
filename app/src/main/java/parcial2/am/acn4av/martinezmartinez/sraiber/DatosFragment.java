package parcial2.am.acn4av.martinezmartinez.sraiber;

import android.os.Bundle;
import android.view.*;
import android.widget.LinearLayout;
import androidx.fragment.app.Fragment;
import parcial2.am.acn4av.martinezmartinez.sraiber.Repository.DatosUsuario;

public abstract class DatosFragment extends Fragment {
    protected LinearLayout contenido;
    protected DatosUsuario datos;
    @Override public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle state) {
        androidx.core.widget.NestedScrollView scroll = new androidx.core.widget.NestedScrollView(requireContext());
        scroll.setId(R.id.seccion_scroll);
        scroll.setFillViewport(true);
        contenido = new LinearLayout(requireContext());
        contenido.setId(R.id.seccion_contenido);
        contenido.setOrientation(LinearLayout.VERTICAL);
        int margen = UiEntrenamiento.dp(requireContext(), 20);
        contenido.setPadding(margen, margen, margen, margen);
        scroll.addView(contenido, new android.widget.FrameLayout.LayoutParams(-1, -2));
        return scroll;
    }
    @Override public void onViewCreated(View view, Bundle state) {
        datos = DatosUsuario.obtener(requireContext());
        datos.cambios.observe(getViewLifecycleOwner(), cambio -> mostrar());
    }
    protected boolean esperando() {
        if (datos.listo) return false;
        UiEntrenamiento.texto(contenido, getString(R.string.cargando_datos), 20, true);
        UiEntrenamiento.boton(contenido, R.string.reintentar, false, v -> datos.reintentar(requireContext()));
        return true;
    }
    @Override public void onDestroyView() { super.onDestroyView(); contenido = null; }
    protected abstract void mostrar();
}
