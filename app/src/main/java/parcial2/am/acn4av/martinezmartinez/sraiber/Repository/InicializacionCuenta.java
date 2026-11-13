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


public final class InicializacionCuenta {
    private InicializacionCuenta() { }


    public static final class SinRutinasPredeterminadas extends IllegalStateException {
        SinRutinasPredeterminadas() { super("No hay rutinas predeterminadas"); }
    }

    public static Task<Void> asegurar(FirebaseFirestore db, String uid, long fechaCreacion) {
        DocumentReference usuario = db.collection("usuarios").document(uid);
        DocumentReference marca = usuario.collection("estado").document("migracion");

        return marca.get(Source.CACHE).continueWithTask(cache -> {
            if (cache.isSuccessful() && cache.getResult().exists()) return Tasks.forResult(null);
            return marca.get(Source.SERVER).continueWithTask(servidor -> {
                if (!servidor.isSuccessful()) return Tasks.forException(servidor.getException());

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

            if (predeterminadas.isEmpty()) return Tasks.forException(new SinRutinasPredeterminadas());
            return db.runTransaction(tx -> {

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
