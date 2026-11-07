package parcial2.am.acn4av.martinezmartinez.sraiber;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
                : auth.signInWithEmailAndPassword(email, password), false, registro);
    }

    void recuperar(String email) {
        if (estaOcupado()) return;
        ocupado.setValue(true);
        mensaje.setValue(0);
        FirebaseAuth.getInstance().setLanguageCode("es");
        completar(FirebaseAuth.getInstance().sendPasswordResetEmail(email), true, false);
    }

    private void completar(Task<?> tarea, boolean recuperacion, boolean registro) {
        tarea.addOnCompleteListener(resultado -> {
            ocupado.setValue(false);
            if (resultado.isSuccessful()) {

                if (recuperacion) {

                    mensaje.setValue(
                            R.string.auth_reset_enviado
                    );

                } else if (registro) {

                    inicializarUsuarioNuevo(
                            FirebaseAuth.getInstance()
                                    .getCurrentUser()
                                    .getUid()
                    );

                } else {

                    autenticado.setValue(true);
                }
            }else {
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

    //Acá inicializa el usuarioNuevo, poniendolo en la BD, junto con las rutinas predeterminadas de la app. Además de guardar la fecha en la que creó la cuenta
    private void inicializarUsuarioNuevo(String uid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("rutinasPredeterminadas")
                .get()
                .addOnSuccessListener(snapshot -> {

                    WriteBatch batch = db.batch();

                    Map<String, Object> datosUsuario = new HashMap<>();

                    datosUsuario.put("uid", uid);
                    datosUsuario.put("fechaCreacion", System.currentTimeMillis());

                    batch.set(db.collection("usuarios").document(uid), datosUsuario);

                    batch.set(
                            db.collection("usuarios")
                                    .document(uid)
                                    .collection("estado")
                                    .document("migracion"),
                            Collections.singletonMap("version",1)
                    );

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {

                        batch.set(db.collection("usuarios")
                                        .document(uid)
                                        .collection("rutinas")
                                        .document(doc.getId()),
                                doc.getData()
                        );
                    }

                    batch.commit()
                            .addOnSuccessListener(v -> {autenticado.setValue(true);})
                            .addOnFailureListener(e -> {});
                })
                .addOnFailureListener(e -> mensaje.setValue(R.string.auth_error_general)
                );
    }
}
