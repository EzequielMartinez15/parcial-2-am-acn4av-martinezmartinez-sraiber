package parcial2.am.acn4av.martinezmartinez.sraiber;

import android.app.Activity;
import android.content.Context;
import android.widget.EditText;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import parcial2.am.acn4av.martinezmartinez.sraiber.Repository.DatosUsuario;
import parcial2.am.acn4av.martinezmartinez.sraiber.model.Rutina;
import parcial2.am.acn4av.martinezmartinez.sraiber.ui.activities.HomeActivity;
import parcial2.am.acn4av.martinezmartinez.sraiber.ui.activities.LoginActivity;

import static org.junit.Assert.*;

// Probé el registro y la recuperación de cuentas a medio inicializar solo contra los emuladores.
// Necesita que los emuladores tengan cargadas 3 rutinas en rutinasPredeterminadas.
@RunWith(AndroidJUnit4.class)
public class ZRegistroRutinasUiTest {
    private static final int PREDETERMINADAS = 3;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private Context context;

    @Before public void preparar() {
        Assume.assumeTrue("true".equals(InstrumentationRegistry.getArguments().getString("uiFirebaseEmulators")));
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        auth = FirebaseAuth.getInstance();
        // Me aseguré de no correr esto en una instalación con una sesión real abierta.
        Assume.assumeTrue(auth.getCurrentUser() == null);
        auth.useEmulator("10.0.2.2", 9098);
        db = FirebaseFirestore.getInstance();
        db.useEmulator("10.0.2.2", 8088);
    }

    @After public void limpiar() throws Exception {
        if (db == null) return;
        InstrumentationRegistry.getInstrumentation().runOnMainSync(DatosUsuario::soltar);
        auth.signOut();
        Tasks.await(db.terminate(), 15, TimeUnit.SECONDS);
    }

    @Test public void registroDeDosCuentasSeguidasCopiaLasRutinas() throws Exception {
        try (ActivityScenario<LoginActivity> login = ActivityScenario.launch(LoginActivity.class)) {
            String primera = registrarDesdeLogin();
            comprobarInicializada(primera);
            // Cerré la sesión desde Home igual que lo haría el usuario y registré otra cuenta.
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> Sesion.cerrar(actividadActual()));
            hasta(() -> actividadActual() instanceof LoginActivity);
            String segunda = registrarDesdeLogin();
            assertNotEquals(primera, segunda);
            comprobarInicializada(segunda);
        }
    }

    @Test public void recuperaInicializacionIncompletaSinPisarNiRecrear() throws Exception {
        // Simulé una cuenta cuyo registro se cortó antes de copiar las rutinas.
        Tasks.await(auth.createUserWithEmailAndPassword(UUID.randomUUID() + "@example.com", "Prueba123456"), 20, TimeUnit.SECONDS);
        String uid = auth.getCurrentUser().getUid();
        DocumentReference usuario = db.collection("usuarios").document(uid);
        String idPredeterminada = Tasks.await(db.collection("rutinasPredeterminadas").get(Source.SERVER)).getDocuments().get(0).getId();
        // Guardé una rutina propia con el mismo ID que una predeterminada para comprobar que no se pisa.
        Tasks.await(usuario.collection("rutinas").document(idPredeterminada)
                .set(Collections.singletonMap("nombre", "Mi rutina")), 20, TimeUnit.SECONDS);

        AtomicReference<DatosUsuario> datos = new AtomicReference<>();
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> datos.set(DatosUsuario.obtener(context)));
        hasta(() -> datos.get().listo && datos.get().rutinas.size() == PREDETERMINADAS);
        assertTrue(Tasks.await(usuario.collection("estado").document("migracion").get(Source.SERVER)).exists());
        assertEquals("Mi rutina", datos.get().buscarRutina(idPredeterminada).getNombre());

        // Borré una rutina a propósito y volví a abrir la cuenta: no tiene que reaparecer.
        Rutina borrada = null;
        for (Rutina r : datos.get().rutinas) if (!r.getId().equals(idPredeterminada)) borrada = r;
        Rutina aBorrar = borrada;
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> datos.get().borrarRutina(aBorrar));
        Tasks.await(db.waitForPendingWrites(), 20, TimeUnit.SECONDS);
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            DatosUsuario.soltar(); datos.set(DatosUsuario.obtener(context));
        });
        hasta(() -> datos.get().listo && !datos.get().desdeCache);
        Thread.sleep(1500);
        assertEquals(PREDETERMINADAS - 1, datos.get().rutinas.size());
        assertNull(datos.get().buscarRutina(aBorrar.getId()));
    }

    private String registrarDesdeLogin() throws Exception {
        String email = UUID.randomUUID() + "@example.com";
        hasta(() -> actividadActual() instanceof LoginActivity);
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            Activity a = actividadActual();
            a.findViewById(R.id.create_account_button).performClick();
            ((EditText) a.findViewById(R.id.email_input)).setText(email);
            ((EditText) a.findViewById(R.id.password_input)).setText("Prueba123456");
            ((EditText) a.findViewById(R.id.confirm_password_input)).setText("Prueba123456");
            a.findViewById(R.id.login_button).performClick();
        });
        hasta(() -> actividadActual() instanceof HomeActivity);
        return auth.getCurrentUser().getUid();
    }

    private void comprobarInicializada(String uid) throws Exception {
        DocumentReference usuario = db.collection("usuarios").document(uid);
        hasta(() -> Tasks.await(usuario.collection("rutinas").get(Source.SERVER)).size() == PREDETERMINADAS
                && Tasks.await(usuario.collection("estado").document("migracion").get(Source.SERVER)).exists());
        AtomicReference<Integer> enPantalla = new AtomicReference<>(0);
        hasta(() -> {
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                DatosUsuario d = DatosUsuario.obtener(context);
                enPantalla.set(d.listo ? d.rutinas.size() : -1);
            });
            return enPantalla.get() == PREDETERMINADAS;
        });
    }

    private static Activity actividadActual() {
        AtomicReference<Activity> actual = new AtomicReference<>();
        Runnable buscar = () -> {
            Collection<Activity> activas = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED);
            if (!activas.isEmpty()) actual.set(activas.iterator().next());
        };
        if (android.os.Looper.myLooper() == android.os.Looper.getMainLooper()) buscar.run();
        else InstrumentationRegistry.getInstrumentation().runOnMainSync(buscar);
        return actual.get();
    }

    private static void hasta(Callable<Boolean> condicion) throws Exception {
        long limite = System.currentTimeMillis() + 30000;
        while (System.currentTimeMillis() < limite) {
            try { if (Boolean.TRUE.equals(condicion.call())) return; } catch (Exception ignorada) { }
            Thread.sleep(250);
        }
        assertTrue("La condición no se cumplió a tiempo", condicion.call());
    }
}
