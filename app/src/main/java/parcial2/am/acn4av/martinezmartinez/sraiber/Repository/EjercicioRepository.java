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
    //Cambie toda la clase para migrar todos los datos a la BD y que obtenga los datos de ahí y no de manera local como estaba antes.
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

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
    //este getCatalogo se produce solo 1 vez al iniciar la app, cuando está iniciada toda la aplicación se maneja con el méthod de getCatalogoCache (está más abajo)
        db.collection("ejerciciosPropiosDeLaApp")
                .get()
                .addOnSuccessListener(snapshot -> {

                    List<EjercicioCatalogo> ejercicios = new ArrayList<>();

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
        db.collection("usuarios")
                .document(uid)
                .collection("ejerciciosCreados")
                .get()
                .addOnSuccessListener(snapshot -> {

                    List<EjercicioCatalogo> ejercicios = new ArrayList<>();

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

                            catalogoCreadosUser.put(
                                    id,
                                    ejercicioCreado
                            );

                            callback.onSuccess();
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

        db.collection("usuarios")
                .document(uidUsuario)
                .collection("ejerciciosCreados")
                .document(idEjercicio)
                .delete()
                .addOnSuccessListener(unused -> {

                    catalogoCreadosUser.remove(idEjercicio);

                    callback.onSuccess();
                })
                .addOnFailureListener(callback::onError);
    }
    public static int imagenPorNombre(
            Context context,
            String nombre) {
        //cambié el méthod para que dependiendo del nombre del ejercicio y el contexto busque la foto, si no la encuentra pone el ícono de la app
        //TODO dsps hay que adaptarlo para que tome los valores de una URL capaz, o una foto de la galeria del user.
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
