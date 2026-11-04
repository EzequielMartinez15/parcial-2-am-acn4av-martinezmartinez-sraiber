package parcial2.am.acn4av.martinezmartinez.sraiber;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.google.android.gms.tasks.*;
import com.google.firebase.*;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.*;
import org.junit.*;
import org.junit.runner.RunWith;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import parcial2.am.acn4av.martinezmartinez.sraiber.Repository.*;
import parcial2.am.acn4av.martinezmartinez.sraiber.model.*;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class FirestoreEntrenamientoTest {
    private static final List<FirebaseApp> apps = new ArrayList<>();
    private FirebaseApp app;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private DatosUsuario datos;
    private Context contexto;
    private String prefijo;
    @Before public void preparar() throws Exception {
        Assume.assumeTrue("true".equals(InstrumentationRegistry.getArguments().getString("firebaseEmulators")));
        prefijo = "prueba_nube_" + UUID.randomUUID();
        Context original = InstrumentationRegistry.getInstrumentation().getTargetContext();
        contexto = new ContextWrapper(original) {
            @Override public SharedPreferences getSharedPreferences(String nombre, int modo) {
                return super.getSharedPreferences(prefijo + nombre, modo);
            }
        };
        // Usé una app secundaria y emuladores para no tocar la sesión ni los datos reales del usuario.
        app = FirebaseApp.initializeApp(original, new FirebaseOptions.Builder().setProjectId("demo-marber")
                .setApplicationId("1:123456789:android:pruebas").setApiKey("fake-api-key").build(), prefijo);
        apps.add(app);
        auth = FirebaseAuth.getInstance(app); auth.useEmulator("10.0.2.2", 9098);
        db = FirebaseFirestore.getInstance(app); db.useEmulator("10.0.2.2", 8088);
        db.setFirestoreSettings(new FirebaseFirestoreSettings.Builder().setLocalCacheSettings(MemoryCacheSettings.newBuilder().build()).build());
        esperar(auth.createUserWithEmailAndPassword(UUID.randomUUID() + "@example.com", "Prueba123456"));
        conectar();
    }
    private void conectar() throws Exception {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> datos = DatosUsuario.conectar(contexto, auth, db));
        hasta(() -> datos.listo && !datos.desdeCache);
    }
    @After public void limpiar() throws Exception {
        if (datos != null) InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> datos.cerrar());
        if (db != null) esperar(db.terminate());
    }
    @AfterClass public static void liberarApps() { for (FirebaseApp app : apps) app.delete(); apps.clear(); }
    private <T> T esperar(Task<T> task) throws Exception { return Tasks.await(task, 20, TimeUnit.SECONDS); }
    private <T> Task<T> principal(Callable<Task<T>> accion) {
        AtomicReference<Task<T>> resultado = new AtomicReference<>();
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            try { resultado.set(accion.call()); } catch (Exception e) { resultado.set(Tasks.forException(e)); }
        });
        return resultado.get();
    }
    private void hasta(Callable<Boolean> condicion) throws Exception {
        long limite = System.currentTimeMillis() + 20000;
        while (System.currentTimeMillis() < limite) {
            AtomicReference<Boolean> listo = new AtomicReference<>(false);
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                try { listo.set(condicion.call()); } catch (Exception ignored) { }
            });
            if (listo.get()) return;
            Thread.sleep(50);
        }
        fail("No se cumplió la condición dentro del tiempo esperado");
    }
    @Test public void finalizaSinDuplicarYConservaDetalleTrasBorrarRutina() throws Exception {
        assertEquals(3, datos.rutinas.size()); Rutina r = datos.rutinas.get(0);
        Entrenamiento e = esperar(principal(() -> datos.empezar(r)));
        Entrenamiento mismo = esperar(principal(() -> datos.empezar(r))); assertEquals(e.id, mismo.id);
        for (Entrenamiento.Movimiento m : e.ejercicios) for (Entrenamiento.Serie s : m.series) { s.peso = "12,5"; s.realizada = true; }
        e.notas = "Sesión de prueba";
        esperar(principal(() -> datos.finalizar(e))); esperar(principal(() -> datos.finalizar(e)));
        hasta(() -> datos.historial.size() == 1 && datos.activo == null);
        assertEquals(1, datos.rutinasConEstadisticas().stream().filter(x -> x.getId().equals(r.getId())).findFirst().get().getVecesRealizada());
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> datos.borrarRutina(r));
        esperar(db.waitForPendingWrites()); hasta(() -> datos.rutinas.size() == 2);
        assertEquals("Sesión de prueba", datos.historial.get(0).notas);
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> datos.cerrar()); conectar();
        assertEquals(2, datos.rutinas.size()); assertEquals(1, datos.historial.size());
    }
    @Test public void borradorSinConexionSeRecuperaYSeSincroniza() throws Exception {
        Entrenamiento e = esperar(principal(() -> datos.empezar(datos.rutinas.get(0))));
        esperar(db.disableNetwork());
        e.ejercicios.get(0).series.get(0).peso = "22,5"; e.notas = "Sin conexión";
        Task<Void> escritura = principal(() -> datos.guardarBorrador(e));
        hasta(() -> datos.activo != null && datos.activo.notas.equals("Sin conexión") && datos.pendientes);
        assertFalse(escritura.isComplete());
        esperar(db.enableNetwork()); esperar(escritura);
        DocumentSnapshot remoto = esperar(db.collection("usuarios").document(datos.uid).collection("estado").document("activo").get(Source.SERVER));
        assertEquals("Sin conexión", remoto.getString("notas"));
        esperar(principal(() -> datos.descartar())); hasta(() -> datos.activo == null);
        assertTrue(datos.historial.isEmpty());
    }
    @Test public void reglasImpidenLeerYEscribirOtraCuenta() throws Exception {
        DocumentReference ajeno = db.collection("usuarios").document("otro-usuario").collection("estado").document("activo");
        try { esperar(ajeno.set(Collections.singletonMap("dato", true))); fail("La escritura ajena no debe estar permitida"); }
        catch (ExecutionException e) { assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, ((FirebaseFirestoreException)e.getCause()).getCode()); }
        try { esperar(ajeno.get(Source.SERVER)); fail("La lectura ajena no debe estar permitida"); }
        catch (ExecutionException e) { assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, ((FirebaseFirestoreException)e.getCause()).getCode()); }
    }
}
