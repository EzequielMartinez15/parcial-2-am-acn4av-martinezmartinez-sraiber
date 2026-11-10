package parcial2.am.acn4av.martinezmartinez.sraiber;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.*;
import parcial2.am.acn4av.martinezmartinez.sraiber.Repository.DatosUsuario;
import parcial2.am.acn4av.martinezmartinez.sraiber.model.*;
import static org.junit.Assert.*;
@RunWith(AndroidJUnit4.class)
public class RutinaRepositoryTest {
    @Test public void conservaIdFotoYDatosParaLeerSinCatalogo() {
        Rutina r = new Rutina("Piernas", Arrays.asList(new EjercicioRutina("sentadilla", "Sentadilla", 3, 10,
                "https://example.com/foto.jpg")), 0, new Date(0));
        Map<?, ?> ejercicio = (Map<?, ?>)((List<?>)DatosUsuario.datosRutina(r).get("ejercicios")).get(0);
        assertEquals("sentadilla", ejercicio.get("ejercicioId"));
        assertEquals("Sentadilla", ejercicio.get("nombre"));
        assertEquals("https://example.com/foto.jpg", ejercicio.get("nombreFoto"));
        assertEquals(3, ejercicio.get("series"));
    }
    @Test public void historialConservaFotoTrasEditarRutina() {
        EjercicioRutina ejercicio = new EjercicioRutina("sentadilla", "Sentadilla", 3, 10, "sentadilla");
        Entrenamiento e = Entrenamiento.iniciar(new Rutina("Piernas", Arrays.asList(ejercicio), 0, new Date(0)));
        ejercicio.setNombreImg("otra"); ejercicio.setNombre("Otro");
        Entrenamiento copia = Entrenamiento.leer(e.datos());
        assertEquals("sentadilla", copia.ejercicios.get(0).foto);
        assertEquals("sentadilla", copia.ejercicios.get(0).ejercicioId);
        assertEquals("Sentadilla", copia.ejercicios.get(0).nombre);
    }
    @Test public void validaImportacionAntesDeEscribirYConservaIds() throws Exception {
        org.json.JSONArray json = new org.json.JSONArray("[{\"nombre\":\"Sentadilla\",\"grupoMuscular\":\"Piernas\",\"nombreFoto\":\"sentadilla\"}]");
        assertEquals(ImportarEjerciciosDialog.leer(json).get(0).getId(), ImportarEjerciciosDialog.leer(json).get(0).getId());
        try { ImportarEjerciciosDialog.leer(new org.json.JSONArray("[{\"nombre\":\"\"}]")); fail(); }
        catch (org.json.JSONException expected) { }
        assertFalse(FotosEjercicio.valida("file:///secret"));
        assertFalse(FotosEjercicio.valida("https://"));
        assertTrue(FotosEjercicio.valida("https://example.com/a.jpg"));
    }
}
