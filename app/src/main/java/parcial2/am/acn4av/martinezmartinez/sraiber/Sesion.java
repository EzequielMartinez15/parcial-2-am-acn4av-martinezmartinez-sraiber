package parcial2.am.acn4av.martinezmartinez.sraiber;

import android.app.Activity;
import android.content.Intent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public final class Sesion {
    public static final String EXTRA_UID = "usuario_uid";
    public static final String EXTRA_EMAIL = "usuario_email";
    private Sesion() { }

    public static void agregarUsuario(Intent intent) {
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
        if (usuario != null) {
            intent.putExtra(EXTRA_UID, usuario.getUid());
            intent.putExtra(EXTRA_EMAIL, usuario.getEmail());
        }
    }

    public static boolean exigir(Activity activity) {
        // Comprobé la sesión real; no usé los extras como prueba de autenticación.
        if (FirebaseAuth.getInstance().getCurrentUser() != null) return true;
        volverAlLogin(activity);
        return false;
    }

    public static void cerrar(Activity activity) {
        FirebaseAuth.getInstance().signOut();
        volverAlLogin(activity);
    }

    private static void volverAlLogin(Activity activity) {
        Intent intent = new Intent(activity, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }
}
