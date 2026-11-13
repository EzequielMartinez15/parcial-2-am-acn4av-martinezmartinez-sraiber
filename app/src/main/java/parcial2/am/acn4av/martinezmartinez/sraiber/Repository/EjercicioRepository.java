package parcial2.am.acn4av.martinezmartinez.sraiber.Repository;

import android.content.Context;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import parcial2.am.acn4av.martinezmartinez.sraiber.R;
import parcial2.am.acn4av.martinezmartinez.sraiber.model.EjercicioCatalogo;
import parcial2.am.acn4av.martinezmartinez.sraiber.model.EjercicioRutina;

public class EjercicioRepository {
    private static FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static String cuenta = "";
    public static void preparar(FirebaseFirestore firestore, String uid) {
        db = firestore;
        if (!cuenta.equals(uid)) { catalogo.clear(); catalogoCreadosUser.clear(); cuenta = uid; }
    }
    public static EjercicioCatalogo buscarPorNombre(String nombre) {
        for (EjercicioCatalogo e : todos()) if (e.getNombre() != null && e.getNombre().equalsIgnoreCase(nombre)) return e;
        return null;
    }
    public static List<EjercicioCatalogo> todos() {
        List<EjercicioCatalogo> lista = getCatalogoCache(); lista.addAll(getCatalogoCreadosUserCache());
        lista.sort(java.util.Comparator.comparing(EjercicioCatalogo::getNombre)); return lista;
    }

    public interface Callback {
        void onSuccess(List<EjercicioCatalogo> ejercicios);
        void onError(Exception e);
    }

    public interface CallbackGuardado {
        void onSuccess();
        void onError(Exception e);
    }
    private static final Map<String, EjercicioCatalogo> catalogo = new HashMap<>();
    private static final Map<String, EjercicioCatalogo> catalogoCreadosUser = new HashMap<>();
    public static void getCatalogo(Callback callback) {
        final String solicitudCuenta = cuenta;
        db.collection("ejerciciosPropiosDeLaApp")
                .get()
                .addOnSuccessListener(snapshot -> {

                    List<EjercicioCatalogo> ejercicios = new ArrayList<>();

                    if (!cuenta.equals(solicitudCuenta)) return;
                    catalogo.clear();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {

                        EjercicioCatalogo ejercicio = new EjercicioCatalogo();

                        ejercicio.setId(doc.getId());
                        ejercicio.setNombre(doc.getString("nombre"));
                        ejercicio.setGrupoMuscular(doc.getString("grupoMuscular"));
                        ejercicio.setNombreFoto(doc.getString("nombreFoto"));

                        ejercicios.add(ejercicio);

                        catalogo.put(
                                ejercicio.getId(),
                                ejercicio
                        );
                    }

                    callback.onSuccess(ejercicios);
                })
                .addOnFailureListener(callback::onError);
    }
    public static void getCatalogoCreadosUser(String uid, Callback callback) {
        final String solicitudUid = uid;
        db.collection("usuarios")
                .document(uid)
                .collection("ejerciciosCreados")
                .get()
                .addOnSuccessListener(snapshot -> {

                    List<EjercicioCatalogo> ejercicios = new ArrayList<>();

                    if (!cuenta.equals(solicitudUid)) return;
                    catalogoCreadosUser.clear();

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {

                        EjercicioCatalogo ejercicio = new EjercicioCatalogo();

                        ejercicio.setId(doc.getId());

                        ejercicio.setNombre(doc.getString("nombre"));

                        ejercicio.setGrupoMuscular(doc.getString("grupoMuscular"));

                        ejercicio.setNombreFoto(doc.getString("nombreFoto"));

                        ejercicios.add(ejercicio);

                        catalogoCreadosUser.put(ejercicio.getId(), ejercicio);
                    }

                    callback.onSuccess(ejercicios);

                })
                .addOnFailureListener(
                        callback::onError
                );
    }

    public static EjercicioCatalogo buscarPorId(String idFirebase) {
        EjercicioCatalogo retorno = catalogo.get(idFirebase);

        if (retorno == null){
            retorno = catalogoCreadosUser.get(idFirebase);
            return retorno;
        }
        return retorno;
    }
    public static List<EjercicioCatalogo> getCatalogoCache() {

        return new ArrayList<>(catalogo.values());
    }

    public static List<EjercicioCatalogo> getCatalogoCreadosUserCache() {

        return new ArrayList<>(catalogoCreadosUser.values());
    }

    public static void agregarEjercicio(String uidUsuario, String nombre, String grupoMuscular, String nombreFoto,
            CallbackGuardado callback) {

        Map<String, Object> ejercicio =
                new HashMap<>();

        ejercicio.put(
                "nombre",
                nombre);

        ejercicio.put(
                "grupoMuscular",
                grupoMuscular);

        ejercicio.put(
                "nombreFoto",
                nombreFoto);

        db.collection("usuarios")
                .document(uidUsuario)
                .collection("ejerciciosCreados")
                .add(ejercicio)
                .addOnSuccessListener(
                        documentReference -> {
                            String id =
                                    documentReference.getId();

                            EjercicioCatalogo ejercicioCreado =
                                    new EjercicioCatalogo();

                            ejercicioCreado.setId(id);
                            ejercicioCreado.setNombre(nombre);
                            ejercicioCreado.setGrupoMuscular(grupoMuscular);
                            ejercicioCreado.setNombreFoto(nombreFoto);

                            if (!cuenta.equals(uidUsuario)) return;
                            catalogoCreadosUser.put(
                                    id,
                                    ejercicioCreado
                            );

                            callback.onSuccess();

                        })
                .addOnFailureListener(
                        callback::onError);
    }


    public static void editarEjercicio(
            String uidUsuario,
            String idEjercicio,
            String nombre,
            String grupoMuscular,
            String nombreFoto,
            CallbackGuardado callback) {

        Map<String, Object> ejercicio =
                new HashMap<>();

        ejercicio.put(
                "nombre",
                nombre);

        ejercicio.put(
                "grupoMuscular",
                grupoMuscular);

        ejercicio.put(
                "nombreFoto",
                nombreFoto);

        db.collection("usuarios")
                .document(uidUsuario)
                .collection("ejerciciosCreados")
                .document(idEjercicio)
                .update(ejercicio)
                .addOnSuccessListener(unused -> {

                    if (!cuenta.equals(uidUsuario)) return;
                    EjercicioCatalogo ejercicioEditado =
                            catalogoCreadosUser.get(
                                    idEjercicio);

                    if (ejercicioEditado != null) {

                        ejercicioEditado.setNombre(
                                nombre);

                        ejercicioEditado.setGrupoMuscular(
                                grupoMuscular);

                        ejercicioEditado.setNombreFoto(
                                nombreFoto);

                        catalogoCreadosUser.put(
                                idEjercicio,
                                ejercicioEditado);
                    }

                    callback.onSuccess();

                })
                .addOnFailureListener(
                        callback::onError
                );
    }

    public static void borrarEjercicio(
            String uidUsuario,
            String idEjercicio,
            CallbackGuardado callback) {

        com.google.firebase.firestore.DocumentReference usuario = db.collection("usuarios").document(uidUsuario);
        usuario.collection("rutinas").get().addOnSuccessListener(snapshot -> {
            com.google.firebase.firestore.WriteBatch lote = db.batch();
            lote.delete(usuario.collection("ejerciciosCreados").document(idEjercicio));
            for (DocumentSnapshot rutina : snapshot) {
                Object contenido = rutina.get("ejercicios");
                if (!(contenido instanceof List)) continue;
                List<Object> restantes = new ArrayList<>();
                for (Object item : (List<?>) contenido) {
                    if (!(item instanceof Map) || !idEjercicio.equals(((Map<?, ?>) item).get("ejercicioId"))) restantes.add(item);
                }
                if (restantes.size() != ((List<?>) contenido).size()) lote.update(rutina.getReference(), "ejercicios", restantes);
            }
            lote.commit().addOnSuccessListener(v -> {
                if (cuenta.equals(uidUsuario)) catalogoCreadosUser.remove(idEjercicio); callback.onSuccess();
            }).addOnFailureListener(callback::onError);
        }).addOnFailureListener(callback::onError);
    }
    public static com.google.android.gms.tasks.Task<Void> importar(String uid, List<EjercicioCatalogo> lista) {
        com.google.firebase.firestore.WriteBatch lote = db.batch();
        for (EjercicioCatalogo e : lista) {
            Map<String, Object> campos = new HashMap<>();
            campos.put("nombre", e.getNombre()); campos.put("grupoMuscular", e.getGrupoMuscular());
            campos.put("nombreFoto", e.getNombreFoto());
            lote.set(db.collection("usuarios").document(uid).collection("ejerciciosCreados").document(e.getId()), campos);
        }
        return lote.commit().addOnSuccessListener(v -> {
            if (cuenta.equals(uid)) for (EjercicioCatalogo e : lista) catalogoCreadosUser.put(e.getId(), e);
        });
    }
    public static int imagenPorNombre(
            Context context,
            String nombre) {
        for (EjercicioCatalogo ejercicio : catalogo.values()) {

            if (ejercicio.getNombre().equalsIgnoreCase(nombre)) {

                int id = context.getResources()
                        .getIdentifier(
                                ejercicio.getNombreFoto(),
                                "drawable",
                                context.getPackageName()
                        );

                return id != 0
                        ? id
                        : R.drawable.ic_marber_dumbbell;
            }
        }

        return R.drawable.ic_marber_dumbbell;
    }

    public static Set<String> getIdsEjerciciosUsuario() {

        Set<String> ids = new HashSet<>();

        for (EjercicioCatalogo ejercicio :
                catalogoCreadosUser.values()) {

            ids.add(
                    ejercicio.getId()
            );
        }

        return ids;
    }
}
