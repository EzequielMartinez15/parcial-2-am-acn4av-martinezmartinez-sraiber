package parcial2.am.acn4av.martinezmartinez.sraiber.Repository;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Separé la preparación de la cuenta para usarla tanto al registrarse como al volver a entrar.
// Si el registro se cortó antes de copiar las rutinas, la próxima vez que se abre la cuenta se completa.
public final class InicializacionCuenta {
    private InicializacionCuenta() { }

    // Usé este error cuando la colección de rutinas predeterminadas llega vacía, para no marcar la cuenta como lista.
    public static final class SinRutinasPredeterminadas extends IllegalStateException {
        SinRutinasPredeterminadas() { super("No hay rutinas predeterminadas"); }
    }

    public static Task<Void> asegurar(FirebaseFirestore db, String uid, long fechaCreacion) {
        DocumentReference usuario = db.collection("usuarios").document(uid);
        DocumentReference marca = usuario.collection("estado").document("migracion");
        // Miré primero la caché para poder abrir sin conexión una cuenta que ya estaba lista.
        return marca.get(Source.CACHE).continueWithTask(cache -> {
            if (cache.isSuccessful() && cache.getResult().exists()) return Tasks.forResult(null);
            return marca.get(Source.SERVER).continueWithTask(servidor -> {
                if (!servidor.isSuccessful()) return Tasks.forException(servidor.getException());
                // La marca indica que la copia ya se hizo: no vuelvo a crear rutinas que el usuario borró.
                if (servidor.getResult().exists()) return Tasks.forResult(null);
                return copiarPredeterminadas(db, usuario, marca, fechaCreacion);
            });
        });
    }

    private static Task<Void> copiarPredeterminadas(FirebaseFirestore db, DocumentReference usuario,
                                                    DocumentReference marca, long fechaCreacion) {
        return db.collection("rutinasPredeterminadas").get(Source.SERVER).continueWithTask(consulta -> {
            if (!consulta.isSuccessful()) return Tasks.forException(consulta.getException());
            List<DocumentSnapshot> predeterminadas = consulta.getResult().getDocuments();
            // Comprobé que haya rutinas antes de marcar la cuenta como inicializada.
            if (predeterminadas.isEmpty()) return Tasks.forException(new SinRutinasPredeterminadas());
            return db.runTransaction(tx -> {
                // Hice todas las lecturas primero porque la transacción no permite leer después de escribir.
                if (tx.get(marca).exists()) return null;
                boolean cuentaExiste = tx.get(usuario).exists();
                List<Boolean> yaExisten = new ArrayList<>();
                for (DocumentSnapshot rutina : predeterminadas) {
                    yaExisten.add(tx.get(usuario.collection("rutinas").document(rutina.getId())).exists());
                }
                if (!cuentaExiste) {
                    Map<String, Object> datosUsuario = new HashMap<>();
                    datosUsuario.put("uid", usuario.getId());
                    datosUsuario.put("fechaCreacion", fechaCreacion);
                    tx.set(usuario, datosUsuario);
                }
                for (int i = 0; i < predeterminadas.size(); i++) {
                    // Salteé las rutinas que el usuario ya tiene con ese ID para no pisarlas.
                    if (yaExisten.get(i)) continue;
                    DocumentSnapshot rutina = predeterminadas.get(i);
                    tx.set(usuario.collection("rutinas").document(rutina.getId()), rutina.getData());
                }
                tx.set(marca, Collections.singletonMap("version", 1));
                return null;
            });
        });
    }
}
