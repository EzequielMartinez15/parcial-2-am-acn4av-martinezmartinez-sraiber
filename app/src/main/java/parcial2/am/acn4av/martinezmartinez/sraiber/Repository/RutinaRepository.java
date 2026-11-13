package parcial2.am.acn4av.martinezmartinez.sraiber.Repository;

import android.content.Context;
import java.util.List;
import parcial2.am.acn4av.martinezmartinez.sraiber.model.*;

public class RutinaRepository {
    public static List<Rutina> getRutinas(Context c) { return DatosUsuario.obtener(c).rutinasConEstadisticas(); }
    public static com.google.android.gms.tasks.Task<Void> agregarEjercicio(Context c, Rutina r, EjercicioRutina e) {
        r.getEjercicios().add(e); return DatosUsuario.obtener(c).guardarRutina(r);
    }
    public static com.google.android.gms.tasks.Task<Void> editarEjercicio(Context c, Rutina r, EjercicioRutina e, String idEnFirebase, String nombre, int series, int reps) {
        int i = r.getEjercicios().indexOf(e);
        if (i < 0) throw new IllegalStateException("El ejercicio ya no existe");
        r.getEjercicios().set(i, new EjercicioRutina(idEnFirebase, nombre, series, reps,
                EjercicioRepository.buscarPorId(idEnFirebase) == null ? e.getNombreImg() : EjercicioRepository.buscarPorId(idEnFirebase).getNombreFoto()));
        return DatosUsuario.obtener(c).guardarRutina(r);
    }
    public static com.google.android.gms.tasks.Task<Void> eliminarEjercicio(Context c, Rutina r, EjercicioRutina e) {
        if (!r.getEjercicios().remove(e)) throw new IllegalStateException("El ejercicio ya no existe");
        return DatosUsuario.obtener(c).guardarRutina(r);
    }
    public static void eliminarEjercicioDeTodasLasRutinas(Context c, String idEjercicio) {

        DatosUsuario datos = DatosUsuario.obtener(c);

        for (Rutina rutina : datos.rutinas) {
            boolean modificada = rutina.getEjercicios().removeIf(e -> idEjercicio.equals(e.getIdEnFirebase()));
            if (modificada) {
                datos.guardarRutina(rutina);
            }
        }
    }
    public static void eliminarRutina(Context c, Rutina r) { DatosUsuario.obtener(c).borrarRutina(r); }


    public static void actualizarEjercicioEnRutinas(Context c, String idEjercicio, String nombre, String nombreFoto) {

        DatosUsuario datos = DatosUsuario.obtener(c);

        for (Rutina rutina : datos.rutinas) {
            boolean modificada = false;
            for (EjercicioRutina ejercicio : rutina.getEjercicios()) {
                if (idEjercicio.equals(ejercicio.getIdEnFirebase())) {

                    ejercicio.setNombre(nombre);
                    ejercicio.setNombreImg(nombreFoto);

                    modificada = true;
                }
            }

            if (modificada) {
                datos.guardarRutina(rutina);
            }
        }

        datos.cambios.setValue(datos.cambios.getValue() + 1
        );
    }
}
