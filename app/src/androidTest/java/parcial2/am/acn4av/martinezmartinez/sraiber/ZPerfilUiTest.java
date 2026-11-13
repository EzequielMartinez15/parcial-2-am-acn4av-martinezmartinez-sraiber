package parcial2.am.acn4av.martinezmartinez.sraiber;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.widget.TextView;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import parcial2.am.acn4av.martinezmartinez.sraiber.Repository.DatosUsuario;
import parcial2.am.acn4av.martinezmartinez.sraiber.model.Entrenamiento;
import parcial2.am.acn4av.martinezmartinez.sraiber.model.Rutina;
import parcial2.am.acn4av.martinezmartinez.sraiber.ui.activities.DetalleEntrenamientoActivity;
import parcial2.am.acn4av.martinezmartinez.sraiber.ui.activities.DetalleRutinaActivity;
import parcial2.am.acn4av.martinezmartinez.sraiber.ui.activities.EntrenamientoActivity;
import parcial2.am.acn4av.martinezmartinez.sraiber.ui.activities.HomeActivity;
import parcial2.am.acn4av.martinezmartinez.sraiber.ui.activities.LoginActivity;
import parcial2.am.acn4av.martinezmartinez.sraiber.ui.activities.fragments.PerfilFragment;
import parcial2.am.acn4av.martinezmartinez.sraiber.ui.dialogs.AgregarModificarEliminarEjercicioEnCatalogoDialog;
import parcial2.am.acn4av.martinezmartinez.sraiber.ui.dialogs.CrearRutinaDialog;
import parcial2.am.acn4av.martinezmartinez.sraiber.ui.dialogs.ImportarEjerciciosDialog;

import static org.junit.Assert.*;

// Probé el perfil con datos conocidos contra los emuladores. Con -e capturas true también guardo
// capturas de cada pantalla para armar el informe.
@RunWith(AndroidJUnit4.class)
public class ZPerfilUiTest {
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private Context context;
    private DatosUsuario datos;
    private boolean capturas;

    @Before public void preparar() throws Exception {
        Assume.assumeTrue("true".equals(InstrumentationRegistry.getArguments().getString("uiFirebaseEmulators")));
        capturas = "true".equals(InstrumentationRegistry.getArguments().getString("capturas"));
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        auth = FirebaseAuth.getInstance();
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

    @Test public void perfilMuestraEstadisticasFotoYNavega() throws Exception {
        if (capturas) try (ActivityScenario<LoginActivity> login = ActivityScenario.launch(LoginActivity.class)) {
            captura("01-login");
            login.onActivity(a -> a.findViewById(R.id.create_account_button).performClick());
            captura("02-registro");
        }
        // Usé un email corto para las capturas; sin capturas uso uno aleatorio para no chocar con otras corridas.
        String email = capturas ? "atleta@marber.app" : "prueba-" + UUID.randomUUID().toString().substring(0, 6) + "@marber.app";
        Tasks.await(auth.createUserWithEmailAndPassword(email, "Prueba123456"), 20, TimeUnit.SECONDS);
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> datos = DatosUsuario.obtener(context));
        hasta(() -> datos.listo && datos.rutinas.size() >= 2);

        // Cargué tres entrenamientos terminados en días distintos: dos de la misma rutina.
        Rutina favorita = datos.rutinas.get(0), otra = datos.rutinas.get(1);
        long dia = 24L * 60 * 60 * 1000, ahora = System.currentTimeMillis();
        terminar(favorita, ahora - 4 * dia, "40", "10");
        terminar(otra, ahora - 2 * dia, "60", "8");
        terminar(favorita, ahora - dia, "42,5", "10");
        hasta(() -> datos.historial.size() == 3 && !datos.pendientes);

        Intent perfil = new Intent(context, HomeActivity.class).putExtra("seccion", Navegacion.PERFIL);
        try (ActivityScenario<HomeActivity> home = ActivityScenario.launch(perfil)) {
            hasta(() -> leer(home, R.id.perfil_total).equals("3"));
            assertEquals(String.valueOf(datos.rutinas.size()), leer(home, R.id.perfil_cant_rutinas));
            assertTrue(leer(home, R.id.perfil_favorita).startsWith(favorita.getNombre()));
            assertTrue(leer(home, R.id.perfil_favorita).endsWith("2 veces"));
            assertTrue(leer(home, R.id.perfil_ultimo).startsWith(favorita.getNombre()));
            assertTrue(leer(home, R.id.perfil_miembro).length() > 0);

            // Saqué las capturas de las pestañas antes de cambiar la foto para que no aparezca su aviso.
            if (capturas) {
                capturarPestana(home, Navegacion.HOME, "03-inicio");
                capturarPestana(home, Navegacion.RUTINAS, "04-rutinas");
                abrirDialogo(home, Navegacion.RUTINAS, new CrearRutinaDialog(), "08-crear-rutina");
                capturarPestana(home, Navegacion.EJERCICIOS, "05-ejercicios");
                abrirDialogo(home, Navegacion.EJERCICIOS, AgregarModificarEliminarEjercicioEnCatalogoDialog.crearAgregar(), "09-nuevo-ejercicio");
                abrirDialogo(home, Navegacion.EJERCICIOS, new ImportarEjerciciosDialog(), "10-importar-json");
                capturarPestana(home, Navegacion.HISTORIAL, "06-historial");
                home.onActivity(a -> a.irA(Navegacion.PERFIL));
                Thread.sleep(800);
            }

            // Simulé la elección de la galería con una foto del proyecto para probar la copia y la vista.
            Uri foto = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.drawable.sentadilla);
            home.onActivity(a -> ((PerfilFragment) a.getSupportFragmentManager()
                    .findFragmentByTag("pestana_" + Navegacion.PERFIL)).guardarFoto(foto));
            File archivo = new File(new File(context.getFilesDir(), "perfil"), auth.getCurrentUser().getUid() + ".jpg");
            hasta(() -> archivo.exists() && archivo.length() > 0);
            hasta(() -> {
                AtomicReference<Integer> visible = new AtomicReference<>();
                home.onActivity(a -> visible.set(a.findViewById(R.id.perfil_btn_quitar_foto).getVisibility()));
                return visible.get() == android.view.View.VISIBLE;
            });
            // Esperé a que se vaya el aviso de la foto antes de capturar el perfil.
            if (capturas) Thread.sleep(3500);
            captura("07-perfil");

            home.onActivity(a -> a.findViewById(R.id.perfil_btn_historial).performClick());
            hasta(() -> {
                AtomicReference<Integer> elegida = new AtomicReference<>();
                home.onActivity(a -> elegida.set(((BottomNavigationView) a.findViewById(R.id.barra_principal)).getSelectedItemId()));
                return elegida.get() == Navegacion.HISTORIAL;
            });
        }

        if (capturas) {
            Intent detalle = new Intent(context, DetalleRutinaActivity.class)
                    .putExtra("rutina_id", favorita.getId()).putExtra("nombre_rutina", favorita.getNombre());
            try (ActivityScenario<DetalleRutinaActivity> pantalla = ActivityScenario.launch(detalle)) { captura("11-detalle-rutina"); }
            Intent hecho = new Intent(context, DetalleEntrenamientoActivity.class)
                    .putExtra("entrenamiento_id", datos.historial.get(0).id);
            try (ActivityScenario<DetalleEntrenamientoActivity> pantalla = ActivityScenario.launch(hecho)) { captura("13-detalle-entrenamiento"); }
            AtomicReference<Task<Entrenamiento>> activo = new AtomicReference<>();
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> activo.set(datos.empezar(otra)));
            Entrenamiento nuevo = Tasks.await(activo.get(), 20, TimeUnit.SECONDS);
            hasta(() -> datos.activo != null);
            Intent entrenar = new Intent(context, EntrenamientoActivity.class).putExtra("entrenamiento_id", nuevo.id);
            try (ActivityScenario<EntrenamientoActivity> pantalla = ActivityScenario.launch(entrenar)) { captura("12-entrenamiento"); }
        }
    }

    private void terminar(Rutina rutina, long fin, String peso, String reps) throws Exception {
        Entrenamiento e = Entrenamiento.iniciar(rutina);
        e.inicio = fin - 55 * 60 * 1000; e.fin = fin;
        for (Entrenamiento.Movimiento m : e.ejercicios)
            for (Entrenamiento.Serie s : m.series) { s.peso = peso; s.repeticiones = reps; s.realizada = true; }
        AtomicReference<Task<Void>> tarea = new AtomicReference<>();
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> tarea.set(datos.finalizar(e)));
        Tasks.await(tarea.get(), 20, TimeUnit.SECONDS);
    }

    private void capturarPestana(ActivityScenario<HomeActivity> home, int pestana, String nombre) throws Exception {
        home.onActivity(a -> a.irA(pestana));
        Thread.sleep(1200);
        captura(nombre);
    }

    private void abrirDialogo(ActivityScenario<HomeActivity> home, int pestana, DialogFragment dialogo, String nombre) throws Exception {
        home.onActivity(a -> {
            a.irA(pestana);
            // El diálogo del catálogo busca sus listeners entre los fragments de la Activity, igual que en la app.
            Fragment pestanaActual = a.getSupportFragmentManager().findFragmentByTag("pestana_" + pestana);
            dialogo.show(dialogo instanceof AgregarModificarEliminarEjercicioEnCatalogoDialog
                    ? a.getSupportFragmentManager() : pestanaActual.getChildFragmentManager(), "captura");
        });
        Thread.sleep(1500);
        captura(nombre);
        home.onActivity(a -> dialogo.dismissAllowingStateLoss());
        Thread.sleep(500);
    }

    private static String leer(ActivityScenario<HomeActivity> home, int id) {
        AtomicReference<String> texto = new AtomicReference<>("");
        home.onActivity(a -> { TextView vista = a.findViewById(id); if (vista != null) texto.set(vista.getText().toString()); });
        return texto.get();
    }

    private void captura(String nombre) throws Exception {
        if (!capturas) return;
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        Thread.sleep(700);
        Bitmap b = InstrumentationRegistry.getInstrumentation().getUiAutomation().takeScreenshot();
        File carpeta = new File(context.getFilesDir(), "capturas");
        if (!carpeta.exists()) assertTrue(carpeta.mkdirs());
        try (FileOutputStream out = new FileOutputStream(new File(carpeta, nombre + ".png"))) {
            b.compress(Bitmap.CompressFormat.PNG, 100, out);
        }
        b.recycle();
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
