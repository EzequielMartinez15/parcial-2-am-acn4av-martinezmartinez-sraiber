package parcial1.am.acn4av.martinezmartinez.sraiber;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;
import parcial1.am.acn4av.martinezmartinez.sraiber.Repository.EjercicioRepository;
import parcial1.am.acn4av.martinezmartinez.sraiber.Repository.RutinaRepository;
import parcial1.am.acn4av.martinezmartinez.sraiber.model.Ejercicio;
import parcial1.am.acn4av.martinezmartinez.sraiber.model.Rutina;

@RunWith(AndroidJUnit4.class)
public class RutinaRepositoryTest {
    private Context context;

    @Before
    public void preparar() {
        // Separé las preferencias de prueba para no tocar los datos del usuario.
        context = new ContextWrapper(InstrumentationRegistry.getInstrumentation().getTargetContext()) {
            @Override public SharedPreferences getSharedPreferences(String name, int mode) {
                return super.getSharedPreferences("prueba_detalle_" + name, mode);
            }
        };
        limpiar();
    }

    @After
    public void limpiar() {
        for (String archivo : new String[]{"rutinas_actualizadas", "ejercicios_agregados", "ejercicios_borrados"}) {
            context.getSharedPreferences(archivo, Context.MODE_PRIVATE).edit().clear().commit();
        }
    }

    @Test
    public void editarEjercicioOriginalPersisteAlRecargar() {
        Rutina rutina = RutinaRepository.getRutinas(context).get(0);
        RutinaRepository.editarEjercicio(context, rutina, rutina.getEjercicios().get(0), "Press Banca", 7, 9);
        Ejercicio recuperado = RutinaRepository.getRutinas(context).get(0).getEjercicios().get(0);
        assertEquals(7, recuperado.getSeries());
        assertEquals(9, recuperado.getRepeticiones());
        assertEquals(R.drawable.press_banca, recuperado.getImagen());
    }

    @Test
    public void borrarUnDuplicadoNoBorraLosDemasYPermiteVolverAAgregar() {
        Rutina rutina = RutinaRepository.getRutinas(context).get(0);
        Ejercicio original = rutina.getEjercicios().get(0);
        int cantidad = rutina.getEjercicios().size();
        RutinaRepository.agregarEjercicio(context, rutina, new Ejercicio(original.getNombre(), 2, 5, original.getImagen()));
        RutinaRepository.eliminarEjercicio(context, rutina, original);
        Rutina recuperada = RutinaRepository.getRutinas(context).get(0);
        assertEquals(cantidad, recuperada.getEjercicios().size());
        assertEquals(2, recuperada.getEjercicios().get(cantidad - 1).getSeries());
        RutinaRepository.agregarEjercicio(context, recuperada, original);
        assertEquals(cantidad + 1, RutinaRepository.getRutinas(context).get(0).getEjercicios().size());
        assertEquals(6, RutinaRepository.getRutinas(context).get(1).getEjercicios().size());
    }

    @Test
    public void conservaDatosAnterioresAlGuardarListaCompleta() {
        context.getSharedPreferences("ejercicios_agregados", Context.MODE_PRIVATE).edit()
                .putString("Push Day", "[{\"nombre\":\"Curl Martillo\",\"series\":3,\"repeticiones\":12}]").commit();
        context.getSharedPreferences("ejercicios_borrados", Context.MODE_PRIVATE).edit()
                .putString("Push Day", "[\"Press Banca\"]").commit();
        Rutina rutina = RutinaRepository.getRutinas(context).get(0);
        assertEquals(6, rutina.getEjercicios().size());
        RutinaRepository.editarEjercicio(context, rutina, rutina.getEjercicios().get(0), "Aperturas", 5, 8);
        Rutina recuperada = RutinaRepository.getRutinas(context).get(0);
        assertEquals(6, recuperada.getEjercicios().size());
        assertEquals("Curl Martillo", recuperada.getEjercicios().get(5).getNombre());
        assertEquals(5, recuperada.getEjercicios().get(0).getSeries());
    }

    @Test
    public void rutinaVaciaYRutinaEliminadaPersisten() {
        Rutina rutina = RutinaRepository.getRutinas(context).get(0);
        while (!rutina.getEjercicios().isEmpty()) {
            RutinaRepository.eliminarEjercicio(context, rutina, rutina.getEjercicios().get(0));
        }
        assertTrue(RutinaRepository.getRutinas(context).get(0).getEjercicios().isEmpty());
        RutinaRepository.eliminarRutina(context, rutina);
        assertEquals(2, RutinaRepository.getRutinas(context).size());
        assertEquals("Pull Day", RutinaRepository.getRutinas(context).get(0).getNombre());
    }

    @Test
    public void catalogoTieneFotosYReconoceAcentos() {
        assertEquals(18, EjercicioRepository.getEjercicios().size());
        for (Ejercicio ejercicio : EjercicioRepository.getEjercicios()) {
            assertNotEquals(R.drawable.blue, ejercicio.getImagen());
            assertNotNull(context.getDrawable(ejercicio.getImagen()));
        }
        assertEquals(R.drawable.curl_de_biceps, EjercicioRepository.imagenPorNombre("Curl de Bíceps"));
        assertEquals(R.drawable.ic_marber_dumbbell, EjercicioRepository.imagenPorNombre("Personalizado"));
    }
}
