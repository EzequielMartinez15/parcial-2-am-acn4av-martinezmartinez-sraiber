package parcial2.am.acn4av.martinezmartinez.sraiber;

import android.widget.ImageView;
import com.bumptech.glide.Glide;

public final class FotosEjercicio {
    private FotosEjercicio() { }
    public static boolean valida(String valor) {
        if (valor == null || valor.isEmpty()) return false;
        if (valor.matches("[a-z][a-z0-9_]*")) return true;
        try {
            java.net.URI uri = new java.net.URI(valor);
            return "https".equalsIgnoreCase(uri.getScheme()) && uri.getHost() != null
                    && uri.getUserInfo() == null;
        } catch (Exception e) { return false; }
    }
    public static void cargar(ImageView vista, String valor) {
        Object origen = R.drawable.ic_marber_dumbbell;
        if (valor != null && valida(valor)) {
            if (valor.toLowerCase(java.util.Locale.ROOT).startsWith("https://")) origen = valor;
            else {
                int id = vista.getResources().getIdentifier(valor, "drawable", vista.getContext().getPackageName());
                if (id != 0) origen = id;
            }
        }
        // Conserve las fotos locales y agregue descarga con cache para las URL.
        Glide.with(vista).load(origen).placeholder(R.drawable.ic_marber_dumbbell)
                .error(R.drawable.ic_marber_dumbbell).centerCrop().into(vista);
    }
}
