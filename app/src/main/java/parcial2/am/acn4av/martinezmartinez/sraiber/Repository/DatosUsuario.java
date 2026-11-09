package parcial2.am.acn4av.martinezmartinez.sraiber.Repository;

import android.content.Context;
import androidx.lifecycle.MutableLiveData;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.*;
import parcial2.am.acn4av.martinezmartinez.sraiber.model.*;

public final class DatosUsuario {
    private static DatosUsuario actual;
    public final String uid;
    public final MutableLiveData<Integer> cambios = new MutableLiveData<>(0);
    public final MutableLiveData<String> error = new MutableLiveData<>("");
    public boolean listo, desdeCache, pendientes;
    public Entrenamiento activo;
    public final List<Rutina> rutinas = new ArrayList<>();
    public final List<Entrenamiento> historial = new ArrayList<>();
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private final Context contexto;
    private final DocumentReference usuario;
    private final List<ListenerRegistration> escuchas = new ArrayList<>();
    private boolean iniciando;
    private boolean cerrado;
    private int cargados;
    private final Map<String, SnapshotMetadata> metadatos = new HashMap<>();

    private DatosUsuario(Context context, FirebaseAuth auth, FirebaseFirestore db) {
        this.contexto = context; this.auth = auth; this.db = db;
        this.uid = auth.getCurrentUser().getUid(); usuario = db.collection("usuarios").document(uid);
    }

    public static DatosUsuario conectar(Context context, FirebaseAuth auth, FirebaseFirestore db) {
        if (auth.getCurrentUser() == null) throw new IllegalStateException("Sin sesión");
        DatosUsuario datos = new DatosUsuario(context, auth, db); datos.iniciar(context); return datos;
    }

    public static synchronized DatosUsuario obtener(Context context) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) throw new IllegalStateException("Sin sesión");
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (actual == null || !actual.uid.equals(uid)) {
            if (actual != null) actual.cerrar();
            actual = conectar(context.getApplicationContext(), FirebaseAuth.getInstance(), FirebaseFirestore.getInstance());
        }
        return actual;
    }

    public static synchronized void soltar() {
        if (actual != null) actual.cerrar();
        actual = null;
    }

    public void cerrar() { cerrado = true; for (ListenerRegistration e : escuchas) e.remove(); escuchas.clear(); }
    private void notificar() { cambios.setValue(cambios.getValue() + 1); }

    public void reintentar(Context context) {
        if (cerrado) return;
        if (error.getValue() != null && !error.getValue().isEmpty()) {
            for (ListenerRegistration e : escuchas) e.remove(); escuchas.clear();
            metadatos.clear(); listo = false; iniciando = false;
        }
        if (!iniciando && !listo) iniciar(context.getApplicationContext());
    }

    private void iniciar(Context context) {
        if (iniciando) return;
        iniciando = true; error.setValue("");
        //Crea el catálogo de los ejercicios creados por la app y los deja en un hashmap para que se use este en la app y no vaya siempre a la bd para ver los ejercicios
        EjercicioRepository.getCatalogo(new EjercicioRepository.Callback() {
                @Override
                public void onSuccess(List<EjercicioCatalogo> ejerciciosCatalogoApp) {
                    escuchar();
                }
                @Override
                public void onError(Exception e) {
                    fallo(e);
                }
            }
        );
        EjercicioRepository.getCatalogoCreadosUser(uid, new EjercicioRepository.Callback() {
                @Override
                public void onSuccess(List<EjercicioCatalogo> ejerciciosCatalogoUser) {
                    escuchar();
                }
                @Override
                public void onError(Exception e) {
                    fallo(e);
                }
            }
        );
    }
    //Borré el méthod que migraba todos los datos del almacenamiento local. En el authViewModel se cargan las rutinas predefinidas de un nuevo user y más abajo se cargan las rutinas de un user logeado
    private void escuchar() {
        if (cerrado) return;
        if (!escuchas.isEmpty()) return;
        escuchas.add(usuario.collection("rutinas").addSnapshotListener(MetadataChanges.INCLUDE, (snapshot, e) -> {
            if (cerrado) return;
            if (e != null) { fallo(e); return; }
            if (snapshot == null) return;
            rutinas.clear();
            for (DocumentSnapshot d : snapshot) rutinas.add(leerRutina(d));
            actualizarMeta("rutinas", snapshot.getMetadata());
        }));
        escuchas.add(usuario.collection("entrenamientos").addSnapshotListener(MetadataChanges.INCLUDE, (snapshot, e) -> {
            if (cerrado) return;
            if (e != null) { fallo(e); return; }
            if (snapshot == null) return;
            historial.clear();
            for (DocumentSnapshot d : snapshot) {
                Entrenamiento entrenamiento = Entrenamiento.leer(d.getData());
                if (entrenamiento.fin > 0) historial.add(entrenamiento);
            }
            historial.sort(Comparator.comparingLong((Entrenamiento t) -> t.fin).reversed());
            actualizarMeta("historial", snapshot.getMetadata());
        }));
        escuchas.add(usuario.collection("estado").document("activo")
                .addSnapshotListener(MetadataChanges.INCLUDE, (snapshot, e) -> {
                    if (cerrado) return;
                    if (e != null) { fallo(e); return; }
                    if (snapshot == null) return;
                    activo = snapshot.exists() ? Entrenamiento.leer(snapshot.getData()) : null;
                    actualizarMeta("activo", snapshot.getMetadata());
                }));
    }

    private void actualizarMeta(String fuente, SnapshotMetadata metadata) {
        metadatos.put(fuente, metadata); cargados = metadatos.size(); listo = cargados == 3;
        desdeCache = false; pendientes = false;
        for (SnapshotMetadata m : metadatos.values()) {
            desdeCache |= m.isFromCache(); pendientes |= m.hasPendingWrites();
        }
        error.setValue(""); notificar();
    }

    private void fallo(Exception e) {
        if (cerrado) return;
        if (e instanceof FirebaseFirestoreException
                && ((FirebaseFirestoreException)e).getCode() == FirebaseFirestoreException.Code.PERMISSION_DENIED)
            error.setValue(contexto.getString(parcial2.am.acn4av.martinezmartinez.sraiber.R.string.datos_error_permiso));
        else error.setValue(contexto.getString(parcial2.am.acn4av.martinezmartinez.sraiber.R.string.datos_error_red));
        notificar();
    }

    private void comprobar() {
        if (cerrado || !listo || auth.getCurrentUser() == null
                || !uid.equals(auth.getCurrentUser().getUid()))
            throw new IllegalStateException("La cuenta no está lista");
    }

    public void guardarRutina(Rutina r) {
        comprobar(); usuario.collection("rutinas").document(r.getId()).set(datosRutina(r)).addOnFailureListener(this::fallo);
    }
    public void borrarRutina(Rutina r) {
        comprobar(); usuario.collection("rutinas").document(r.getId()).delete().addOnFailureListener(this::fallo);
    }
    public Rutina buscarRutina(String idONombre) {
        for (Rutina r : rutinas) if (r.getId().equals(idONombre) || r.getNombre().equals(idONombre)) return r;
        return null;
    }
    public Entrenamiento buscarEntrenamiento(String id) {
        for (Entrenamiento e : historial) if (e.id.equals(id)) return e;
        return null;
    }
    public Task<Entrenamiento> empezar(Rutina r) {
        comprobar(); Entrenamiento nuevo = Entrenamiento.iniciar(r);
        DocumentReference ref = usuario.collection("estado").document("activo");
        // Comprobé en una transacción que no haya otro entrenamiento activo en otro dispositivo.
        return db.runTransaction(tx -> {
            DocumentSnapshot existente = tx.get(ref);
            if (existente.exists()) return Entrenamiento.leer(existente.getData());
            tx.set(ref, nuevo.datos()); return nuevo;
        });
    }
    public Task<Void> guardarBorrador(Entrenamiento e) {
        comprobar(); return usuario.collection("estado").document("activo").set(e.datos()).addOnFailureListener(this::fallo);
    }
    public Task<Void> descartar() {
        comprobar(); return usuario.collection("estado").document("activo").delete().addOnFailureListener(this::fallo);
    }
    public Task<Void> finalizar(Entrenamiento e) {
        comprobar();
        if (!e.completo()) return Tasks.forException(new IllegalArgumentException("Hay series incompletas"));
        if (e.fin == 0) e.fin = System.currentTimeMillis();
        // Usé el mismo ID al reintentar para no duplicar el historial ni contar dos veces una sesión.
        WriteBatch lote = db.batch();
        lote.set(usuario.collection("entrenamientos").document(e.id), e.datos());
        lote.delete(usuario.collection("estado").document("activo"));
        return lote.commit().addOnFailureListener(this::fallo);
    }

    public List<Rutina> rutinasConEstadisticas() {
        List<Rutina> salida = new ArrayList<>();
        for (Rutina r : rutinas) {
            int cantidad = 0; long fecha = 0;
            for (Entrenamiento e : historial) if (e.rutinaId.equals(r.getId())) { cantidad++; fecha = Math.max(fecha, e.fin); }
            Rutina copia = new Rutina(r.getNombre(), r.getEjercicios(), cantidad, new Date(fecha));
            copia.setId(r.getId()); salida.add(copia);
        }
        return salida;
    }
    public static Map<String, Object> datosRutina(Rutina r) {
        Map<String, Object> d = new HashMap<>(); d.put("nombre", r.getNombre());
        List<Map<String, Object>> lista = new ArrayList<>();
        for (EjercicioRutina e : r.getEjercicios()) {
            Map<String, Object> item = new HashMap<>();
            //Hice un cambio en el For por el refractor de ejercicioRutina y puse la información guardad en la db con el nuevo modelado
            item.put("ejercicioId", e.getIdFirebase());
            item.put("series", e.getSeries());
            item.put("repeticiones", e.getRepeticiones());

            lista.add(item);
        }
        d.put("ejercicios", lista); return d;
    }
    @SuppressWarnings("unchecked")
    private Rutina leerRutina(DocumentSnapshot d) {
        List<EjercicioRutina> ejercicios = new ArrayList<>();
        Object datos = d.get("ejercicios");
        if (datos instanceof List) for (Object item : (List<?>) datos) {
            Map<String, Object> e = (Map<String, Object>) item;

            String ejercicioId  = Entrenamiento.texto(e, "ejercicioId");
            int series = (int) Entrenamiento.numero(e, "series");
            int repeticiones = (int) Entrenamiento.numero(e, "repeticiones");

            EjercicioCatalogo catalogo = EjercicioRepository.buscarPorId(ejercicioId);
            //cuando lee la rutina busca el ejercicio específico por la id de firestore y lo agrega a la lista de ejercicios de la rutina
            ejercicios.add(
                    new EjercicioRutina(
                            ejercicioId,
                            catalogo.getNombre(),
                            series,
                            repeticiones,
                            catalogo.getNombreFoto()
                    )
            );
        }
        Rutina r = new Rutina(d.getString("nombre"), ejercicios, 0, new Date(0)); r.setId(d.getId()); return r;
    }
}
