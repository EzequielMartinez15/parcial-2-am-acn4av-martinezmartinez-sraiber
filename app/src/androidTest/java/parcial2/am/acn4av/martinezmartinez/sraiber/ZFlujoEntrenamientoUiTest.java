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
        Rutina r = new Rutina("Rutina de prueba", Arrays.asList(new EjercicioRutina("Sentadilla", 2, 10, "sentadilla")), 0, new Date(0));
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
    @Test public void creaRutinaYConfirmaBorradoDelCatalogo() throws Exception {
        try (ActivityScenario<HomeActivity> escenario = ActivityScenario.launch(HomeActivity.class)) {
            escenario.onActivity(a -> {
                ((com.google.android.material.bottomnavigation.BottomNavigationView)a.findViewById(R.id.barra_principal))
                        .setSelectedItemId(Navegacion.EJERCICIOS);
                AgregarModificarEliminarEjercicioEnCatalogoDialog.crearAgregar().showNow(a.getSupportFragmentManager(), "catalogo_prueba");
            });
            captura("crear-rutina");
            escenario.recreate();
            escenario.onActivity(a -> {
                androidx.fragment.app.DialogFragment fragment = (androidx.fragment.app.DialogFragment)a.getSupportFragmentManager().findFragmentByTag("catalogo_prueba");
                androidx.appcompat.app.AlertDialog dialog = (androidx.appcompat.app.AlertDialog) fragment.requireDialog();
                ((EditText)dialog.findViewById(R.id.input_nombre_ejercicio)).setText("Ejercicio QA");
                ((com.google.android.material.textfield.MaterialAutoCompleteTextView)dialog.findViewById(R.id.input_grupo_muscular)).setText("Piernas", false);
                ((EditText)dialog.findViewById(R.id.input_nombre_foto)).setText("https://example.com/foto.jpg");
                dialog.getButton(-1).performClick();
            });
            hasta(() -> EjercicioRepository.getCatalogoCreadosUserCache().size() == 1);
            escenario.onActivity(a -> {
                ((com.google.android.material.bottomnavigation.BottomNavigationView)a.findViewById(R.id.barra_principal)).setSelectedItemId(Navegacion.RUTINAS);
                new CrearRutinaDialog().showNow(a.getSupportFragmentManager(), "rutina_prueba");
                androidx.fragment.app.DialogFragment f = (androidx.fragment.app.DialogFragment)a.getSupportFragmentManager().findFragmentByTag("rutina_prueba");
                List<EditText> campos = buscar(f.requireDialog().getWindow().getDecorView(), EditText.class);
                campos.get(0).setText("Rutina QA");
                // Marqué el ejercicio creado en la prueba y no el primero del catálogo general.
                for (CheckBox check : buscar(f.requireDialog().getWindow().getDecorView(), CheckBox.class))
                    if (check.getText().toString().contains("Ejercicio QA")) check.setChecked(true);
            });
            captura("crear-rutina");
            escenario.recreate();
            escenario.onActivity(a -> {
                androidx.fragment.app.DialogFragment f = (androidx.fragment.app.DialogFragment)a.getSupportFragmentManager().findFragmentByTag("rutina_prueba");
                assertEquals("Rutina QA", buscar(f.requireDialog().getWindow().getDecorView(), EditText.class).get(0).getText().toString());
                boolean marcado = false;
                for (CheckBox check : buscar(f.requireDialog().getWindow().getDecorView(), CheckBox.class))
                    if (check.getText().toString().contains("Ejercicio QA")) marcado = check.isChecked();
                assertTrue(marcado);
                ((androidx.appcompat.app.AlertDialog)f.requireDialog()).getButton(-1).performClick();
            });
            // Busqué la rutina por nombre porque las cuentas nuevas ya traen las rutinas predeterminadas.
            hasta(() -> datos.buscarRutina("Rutina QA") != null && !datos.pendientes);
            assertEquals(3, datos.buscarRutina("Rutina QA").getEjercicios().get(0).getSeries());
            escenario.onActivity(a -> {
                ((com.google.android.material.bottomnavigation.BottomNavigationView)a.findViewById(R.id.barra_principal)).setSelectedItemId(Navegacion.EJERCICIOS);
                AgregarModificarEliminarEjercicioEnCatalogoDialog.crearEditar(EjercicioRepository.getCatalogoCreadosUserCache().get(0))
                        .showNow(a.getSupportFragmentManager(), "editar_qa");
            });
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            escenario.onActivity(a -> {
                androidx.fragment.app.DialogFragment f = (androidx.fragment.app.DialogFragment)a.getSupportFragmentManager().findFragmentByTag("editar_qa");
                ((androidx.appcompat.app.AlertDialog)f.requireDialog()).getButton(-3).performClick();
            });
            onView(withText(R.string.ejercicio_cancelar)).inRoot(androidx.test.espresso.matcher.RootMatchers.isDialog())
                    .perform(click());
            assertEquals(1, EjercicioRepository.getCatalogoCreadosUserCache().size());
            escenario.onActivity(a -> {
                androidx.fragment.app.DialogFragment f = (androidx.fragment.app.DialogFragment)a.getSupportFragmentManager().findFragmentByTag("editar_qa");
                ((androidx.appcompat.app.AlertDialog)f.requireDialog()).getButton(-3).performClick();
            });
            onView(withText(R.string.confirmar_si)).inRoot(androidx.test.espresso.matcher.RootMatchers.isDialog()).perform(click());
            hasta(() -> EjercicioRepository.getCatalogoCreadosUserCache().isEmpty() && datos.buscarRutina("Rutina QA").getEjercicios().isEmpty());
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
        try (FileOutputStream out = new FileOutputStream(new File(context.getFilesDir(), "qa-" + nombre + ".png"))) {
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
