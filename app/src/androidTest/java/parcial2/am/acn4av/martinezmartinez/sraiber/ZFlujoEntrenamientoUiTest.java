package parcial2.am.acn4av.martinezmartinez.sraiber;

import android.content.*;
import android.graphics.Bitmap;
import android.view.*;
import android.widget.*;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.google.android.gms.tasks.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import org.junit.*;
import org.junit.runner.RunWith;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import parcial2.am.acn4av.martinezmartinez.sraiber.Repository.*;
import parcial2.am.acn4av.martinezmartinez.sraiber.model.*;
import static org.junit.Assert.*;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.action.ViewActions.click;

@RunWith(AndroidJUnit4.class)
public class ZFlujoEntrenamientoUiTest {
    private DatosUsuario datos;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private Context context;
    @Before public void preparar() throws Exception {
        Assume.assumeTrue("true".equals(InstrumentationRegistry.getArguments().getString("uiFirebaseEmulators")));
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        auth = FirebaseAuth.getInstance();
        Assume.assumeTrue(auth.getCurrentUser() == null);
        // Ejecuté este recorrido solo en emuladores y sin una sesión real abierta.
        auth.useEmulator("10.0.2.2", 9098); db = FirebaseFirestore.getInstance(); db.useEmulator("10.0.2.2", 8088);
        db.setFirestoreSettings(new FirebaseFirestoreSettings.Builder().setLocalCacheSettings(MemoryCacheSettings.newBuilder().build()).build());
        await(auth.createUserWithEmailAndPassword(UUID.randomUUID() + "@example.com", "Prueba123456"));
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> datos = DatosUsuario.obtener(context));
        hasta(() -> datos.listo && !datos.desdeCache);
    }
    @After public void limpiar() throws Exception {
        if (datos != null) {
            InstrumentationRegistry.getInstrumentation().runOnMainSync(DatosUsuario::soltar);
            auth.signOut(); await(db.terminate());
        }
    }
    @Test public void registroRotacionHistorialYComparacion() throws Exception {
        Rutina r = new Rutina("Rutina de prueba", Arrays.asList(new EjercicioRutina("Sentadilla", 2, 10, 0)), 0, new Date(0));
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> datos.guardarRutina(r));
        await(db.waitForPendingWrites()); hasta(() -> datos.buscarRutina(r.getId()) != null);
        try (ActivityScenario<HomeActivity> home = ActivityScenario.launch(HomeActivity.class)) { captura("home"); }
        AtomicReference<Task<Entrenamiento>> tarea = new AtomicReference<>();
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> tarea.set(datos.empezar(r)));
        Entrenamiento e = await(tarea.get()); hasta(() -> datos.activo != null);
        Intent intent = new Intent(context, EntrenamientoActivity.class).putExtra("entrenamiento_id", e.id);
        try (ActivityScenario<EntrenamientoActivity> entrenamiento = ActivityScenario.launch(intent)) {
            entrenamiento.onActivity(a -> {
                List<EditText> campos = buscar(a.findViewById(R.id.seccion_contenido), EditText.class);
                assertEquals(6, campos.size());
                campos.get(0).setText("20"); campos.get(1).setText("10");
                campos.get(2).setText("25"); campos.get(3).setText("8");
                campos.get(4).setText("Buena técnica"); campos.get(5).setText("Primera sesión");
                for (CheckBox check : buscar(a.findViewById(R.id.seccion_contenido), CheckBox.class)) check.setChecked(true);
            });
            entrenamiento.recreate();
            entrenamiento.onActivity(a -> {
                List<EditText> campos = buscar(a.findViewById(R.id.seccion_contenido), EditText.class);
                assertEquals("20", campos.get(0).getText().toString());
                for (CheckBox check : buscar(a.findViewById(R.id.seccion_contenido), CheckBox.class)) assertTrue(check.isChecked());
            });
            captura("entrenamiento");
            entrenamiento.onActivity(a -> {
                for (Button b : buscar(a.findViewById(R.id.seccion_contenido), Button.class))
                    if (b.getText().toString().equals(a.getString(R.string.finalizar_entrenamiento))) b.performClick();
            });
            onView(withText(R.string.ejercicio_guardar)).inRoot(androidx.test.espresso.matcher.RootMatchers.isDialog()).perform(click());
            hasta(() -> datos.historial.size() == 1 && datos.activo == null && !datos.pendientes);
        }
        Entrenamiento segundo = Entrenamiento.iniciar(r);
        segundo.ejercicios.get(0).series.get(0).peso = "22,5";
        segundo.ejercicios.get(0).series.get(1).peso = "20";
        for (Entrenamiento.Serie s : segundo.ejercicios.get(0).series) s.realizada = true;
        AtomicReference<Task<Void>> finalizar = new AtomicReference<>();
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> finalizar.set(datos.finalizar(segundo)));
        await(finalizar.get()); hasta(() -> datos.historial.size() == 2);
        try (ActivityScenario<HistorialActivity> historial = ActivityScenario.launch(HistorialActivity.class)) {
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            historial.onActivity(a -> {
                String dia = String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
                boolean seleccionado = false;
                for (TextView t : buscar(a.findViewById(R.id.seccion_contenido), TextView.class))
                    if (t.getText().toString().equals(dia) && t.isClickable()) { t.performClick(); seleccionado = true; break; }
                assertTrue(seleccionado);
                assertTrue(buscar(a.findViewById(R.id.seccion_contenido), TextView.class).stream()
                        .anyMatch(t -> t.getText().toString().startsWith("Semana del")));
            });
            captura("historial");
        }
        Intent detalle = new Intent(context, DetalleEntrenamientoActivity.class).putExtra("entrenamiento_id", segundo.id);
        try (ActivityScenario<DetalleEntrenamientoActivity> pantalla = ActivityScenario.launch(detalle)) {
            pantalla.onActivity(a -> {
                List<TextView> textos = buscar(a.findViewById(R.id.seccion_contenido), TextView.class);
                assertTrue(textos.stream().anyMatch(t -> t.getText().toString().startsWith("↑")));
                assertTrue(textos.stream().anyMatch(t -> t.getText().toString().startsWith("↓")));
            });
            captura("detalle");
        }
    }
    private static <T extends View> List<T> buscar(View v, Class<T> clase) {
        List<T> salida = new ArrayList<>(); if (clase.isInstance(v)) salida.add(clase.cast(v));
        if (v instanceof ViewGroup) for (int i = 0; i < ((ViewGroup)v).getChildCount(); i++)
            salida.addAll(buscar(((ViewGroup)v).getChildAt(i), clase));
        return salida;
    }
    private void captura(String nombre) throws Exception {
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        Thread.sleep(400);
        Bitmap b = InstrumentationRegistry.getInstrumentation().getUiAutomation().takeScreenshot();
        try (FileOutputStream out = new FileOutputStream(new File(context.getExternalFilesDir(null), "qa-" + nombre + ".png"))) {
            b.compress(Bitmap.CompressFormat.PNG, 100, out);
        }
        b.recycle();
    }
    private <T> T await(Task<T> t) throws Exception { return Tasks.await(t, 20, TimeUnit.SECONDS); }
    private void hasta(Callable<Boolean> condicion) throws Exception {
        long limite = System.currentTimeMillis() + 20000;
        while (System.currentTimeMillis() < limite) {
            AtomicReference<Boolean> hecho = new AtomicReference<>(false);
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> { try { hecho.set(condicion.call()); } catch (Exception ignored) { } });
            if (hecho.get()) return; Thread.sleep(50);
        }
        fail("Se agotó el tiempo de espera");
    }
}
