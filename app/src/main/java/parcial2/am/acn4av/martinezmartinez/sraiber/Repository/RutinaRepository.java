package parcial2.am.acn4av.martinezmartinez.sraiber.Repository;

import android.content.Context;
import java.util.List;
import parcial2.am.acn4av.martinezmartinez.sraiber.model.*;

public class RutinaRepository {
    // Conservé estos métodos para conectar los formularios existentes con Firestore.
    public static List<Rutina> getRutinas(Context c) { return DatosUsuario.obtener(c).rutinasConEstadisticas(); }
    public static List<Rutina> getRutinas() { return RutinaLocalAnterior.getRutinas(); }
    public static void agregarEjercicio(Context c, Rutina r, Ejercicio e) {
        r.getEjercicios().add(e); DatosUsuario.obtener(c).guardarRutina(r);
    }
    public static void editarEjercicio(Context c, Rutina r, Ejercicio e, String nombre, int series, int reps) {
        int i = r.getEjercicios().indexOf(e);
        if (i < 0) throw new IllegalStateException("El ejercicio ya no existe");
        r.getEjercicios().set(i, new Ejercicio(nombre, series, reps, EjercicioRepository.imagenPorNombre(nombre)));
        DatosUsuario.obtener(c).guardarRutina(r);
    }
    public static void eliminarEjercicio(Context c, Rutina r, Ejercicio e) {
        if (!r.getEjercicios().remove(e)) throw new IllegalStateException("El ejercicio ya no existe");
        DatosUsuario.obtener(c).guardarRutina(r);
    }
    public static void eliminarRutina(Context c, Rutina r) { DatosUsuario.obtener(c).borrarRutina(r); }
}
