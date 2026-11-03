package parcial2.am.acn4av.martinezmartinez.sraiber;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;

public class AuthViewModel extends ViewModel {
    final MutableLiveData<Boolean> ocupado = new MutableLiveData<>(false);
    final MutableLiveData<Integer> mensaje = new MutableLiveData<>(0);
    final MutableLiveData<Boolean> autenticado = new MutableLiveData<>(false);

    boolean estaOcupado() { return Boolean.TRUE.equals(ocupado.getValue()); }

    void ingresar(String email, String password, boolean registro) {
        if (estaOcupado()) return;
        ocupado.setValue(true);
        mensaje.setValue(0);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        // Conservé la operación al girar la pantalla sin guardar la contraseña en el ViewModel.
        completar(registro ? auth.createUserWithEmailAndPassword(email, password)
                : auth.signInWithEmailAndPassword(email, password), false);
    }

    void recuperar(String email) {
        if (estaOcupado()) return;
        ocupado.setValue(true);
        mensaje.setValue(0);
        FirebaseAuth.getInstance().setLanguageCode("es");
        completar(FirebaseAuth.getInstance().sendPasswordResetEmail(email), true);
    }

    private void completar(Task<?> tarea, boolean recuperacion) {
        tarea.addOnCompleteListener(resultado -> {
            ocupado.setValue(false);
            if (resultado.isSuccessful()) {
                if (recuperacion) mensaje.setValue(R.string.auth_reset_enviado);
                else autenticado.setValue(true);
            } else {
                mensaje.setValue(error(resultado.getException()));
            }
        });
    }

    static int error(Exception exception) {
        if (exception instanceof FirebaseNetworkException) return R.string.auth_error_red;
        if (exception instanceof FirebaseTooManyRequestsException) return R.string.auth_error_intentos;
        if (exception instanceof FirebaseAuthException) {
            switch (((FirebaseAuthException) exception).getErrorCode()) {
                case "ERROR_EMAIL_ALREADY_IN_USE": return R.string.auth_error_email_usado;
                case "ERROR_WEAK_PASSWORD": return R.string.auth_error_password;
                case "ERROR_USER_DISABLED": return R.string.auth_error_deshabilitado;
                case "ERROR_INVALID_EMAIL": return R.string.error_email_invalid;
                case "ERROR_INVALID_CREDENTIAL":
                case "ERROR_INVALID_LOGIN_CREDENTIALS":
                case "ERROR_WRONG_PASSWORD":
                case "ERROR_USER_NOT_FOUND": return R.string.auth_error_credenciales;
                case "ERROR_OPERATION_NOT_ALLOWED": return R.string.auth_error_proveedor;
                default: break;
            }
        }
        return R.string.auth_error_general;
    }
}
