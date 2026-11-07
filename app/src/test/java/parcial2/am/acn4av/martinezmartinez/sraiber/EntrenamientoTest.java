package parcial2.am.acn4av.martinezmartinez.sraiber;
import org.junit.Test;
import java.util.*;
import parcial2.am.acn4av.martinezmartinez.sraiber.model.*;
import static org.junit.Assert.*;

public class EntrenamientoTest {
    private Rutina rutina() {
        return new Rutina("Prueba", Arrays.asList(new EjercicioRutina("Sentadilla", 2, 8, 0)), 0, new Date(0));
    }
    @Test public void capturaNoCambiaCuandoEditoLaRutina() {
        Rutina r = rutina(); Entrenamiento e = Entrenamiento.iniciar(r);
        r.getEjercicios().get(0).setNombre("Otro"); r.getEjercicios().clear();
        assertEquals("Sentadilla", e.ejercicios.get(0).nombre); assertEquals(2, e.totalSeries());
    }
    @Test public void exigeTodasLasSeriesYAdmitePesoCorporal() {
        Entrenamiento e = Entrenamiento.iniciar(rutina()); assertFalse(e.completo());
        for (Entrenamiento.Serie s : e.ejercicios.get(0).series) { s.peso = "0"; s.realizada = true; }
        assertTrue(e.completo()); e.ejercicios.get(0).series.get(1).repeticiones = "0"; assertFalse(e.completo());
    }
    @Test public void validaDecimalesYRechazaValoresImposibles() {
        Entrenamiento.Serie s = new Entrenamiento.Serie(); s.repeticiones = "12";
        for (String p : Arrays.asList("12,5", "0", "2000")) { s.peso = p; assertTrue(s.valida()); }
        for (String p : Arrays.asList("", "-1", "NaN", "Infinity", "2001")) { s.peso = p; assertFalse(s.valida()); }
        s.peso = "10"; s.repeticiones = "2.5"; assertFalse(s.valida());
    }
    @Test public void conservaBorradorYNotasEnLaSerializacion() {
        Entrenamiento e = Entrenamiento.iniciar(rutina()); e.notas = "Buen descanso";
        e.ejercicios.get(0).notas = "Sin dolor"; e.ejercicios.get(0).series.get(0).peso = "12,";
        Entrenamiento copia = Entrenamiento.leer(e.datos());
        assertEquals(e.datos(), copia.datos()); assertEquals(e.id, copia.id);
    }
    @Test public void distingueEjerciciosRepetidos() {
        Rutina r = rutina(); r.getEjercicios().add(new EjercicioRutina("Sentadilla", 1, 10, 0));
        Entrenamiento e = Entrenamiento.iniciar(r);
        assertNotEquals(e.ejercicios.get(0).clave, e.ejercicios.get(1).clave);
    }
    @Test public void semanaIncluyeDomingoYExcluyeLunesSiguiente() {
        Calendar c = Calendar.getInstance(); c.set(2026, Calendar.NOVEMBER, 3, 12, 0, 0);
        long lunes = FechasEntrenamiento.semana(c.getTimeInMillis());
        assertTrue(FechasEntrenamiento.enSemana(FechasEntrenamiento.sumarDias(lunes, 6), lunes));
        assertFalse(FechasEntrenamiento.enSemana(FechasEntrenamiento.sumarDias(lunes, 7), lunes));
        Calendar inicio = Calendar.getInstance(); inicio.setTimeInMillis(lunes);
        assertEquals(Calendar.MONDAY, inicio.get(Calendar.DAY_OF_WEEK));
    }
}
