package parcial2.am.acn4av.martinezmartinez.sraiber.Repository;

import java.util.ArrayList;
import java.util.List;

import parcial2.am.acn4av.martinezmartinez.sraiber.R;
import parcial2.am.acn4av.martinezmartinez.sraiber.model.Ejercicio;

public class EjercicioRepository {

    public static List<Ejercicio> getEjercicios() {

        List<Ejercicio> ejercicios = new ArrayList<>();

        ejercicios.add(new Ejercicio("Press Banca", 4, 10, R.drawable.press_banca));
        ejercicios.add(new Ejercicio("Press Inclinado", 4, 12, R.drawable.press_inclinado));
        ejercicios.add(new Ejercicio("Aperturas", 3, 15, R.drawable.aperturas));
        ejercicios.add(new Ejercicio("Press Militar", 4, 10, R.drawable.press_militar));
        ejercicios.add(new Ejercicio("Elevaciones Laterales", 3, 15, R.drawable.elevaciones_laterales));
        ejercicios.add(new Ejercicio("Fondos", 3, 12, R.drawable.fondos));

        ejercicios.add(new Ejercicio("Dominadas", 4, 8, R.drawable.dominadas));
        ejercicios.add(new Ejercicio("Jalon al Pecho", 4, 12, R.drawable.jalon_al_pecho));
        ejercicios.add(new Ejercicio("Remo con Barra", 4, 10, R.drawable.remo_con_barra));
        ejercicios.add(new Ejercicio("Face Pull", 3, 15, R.drawable.face_pull));
        ejercicios.add(new Ejercicio("Curl de Biceps", 4, 12, R.drawable.curl_de_biceps));
        ejercicios.add(new Ejercicio("Curl Martillo", 3, 12, R.drawable.curl_martillo));

        ejercicios.add(new Ejercicio("Sentadilla", 4, 10, R.drawable.sentadilla));
        ejercicios.add(new Ejercicio("Prensa", 4, 12, R.drawable.prensa));
        ejercicios.add(new Ejercicio("Peso Muerto Rumano", 4, 10, R.drawable.peso_muerto_rumano));
        ejercicios.add(new Ejercicio("Extensiones", 3, 15, R.drawable.extensiones));
        ejercicios.add(new Ejercicio("Curl Femoral", 3, 15, R.drawable.curl_femoral));
        ejercicios.add(new Ejercicio("Gemelos", 4, 20, R.drawable.gemelos));

        return ejercicios;
    }

    public static int imagenPorNombre(String nombre) {
        Ejercicio ejercicio = buscarPorNombre(nombre);
        return ejercicio == null ? R.drawable.ic_marber_dumbbell : ejercicio.getImagen();
    }

    public static Ejercicio buscarPorNombre(String nombre) {

        for (Ejercicio ejercicio : getEjercicios()) {
            if (normalizar(ejercicio.getNombre()).equals(normalizar(nombre))) {
                return ejercicio;
            }
        }

        return null;
    }
    private static String normalizar(String nombre) {
        return java.text.Normalizer.normalize(nombre.trim(), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(java.util.Locale.ROOT);
    }
}
