package parcial1.am.acn4av.martinezmartinez.sraiber.Repository;

import java.util.ArrayList;
import java.util.List;

import parcial1.am.acn4av.martinezmartinez.sraiber.R;
import parcial1.am.acn4av.martinezmartinez.sraiber.model.Ejercicio;

public class EjercicioRepository {

    public static List<Ejercicio> getEjercicios() {

        List<Ejercicio> ejercicios = new ArrayList<>();

        ejercicios.add(new Ejercicio("Press Banca", 4, 10, R.drawable.blue));//press_banca
        ejercicios.add(new Ejercicio("Press Inclinado", 4, 12, R.drawable.blue));//press_inclinado
        ejercicios.add(new Ejercicio("Aperturas", 3, 15, R.drawable.blue));//aperturas
        ejercicios.add(new Ejercicio("Press Militar", 4, 10, R.drawable.blue));//press_militar
        ejercicios.add(new Ejercicio("Elevaciones Laterales", 3, 15, R.drawable.blue));//elevaciones_laterales
        ejercicios.add(new Ejercicio("Fondos", 3, 12, R.drawable.blue));//fondos

        ejercicios.add(new Ejercicio("Dominadas", 4, 8, R.drawable.blue));//dominadas
        ejercicios.add(new Ejercicio("Jalon al Pecho", 4, 12, R.drawable.blue));//jalon_al_pecho
        ejercicios.add(new Ejercicio("Remo con Barra", 4, 10, R.drawable.blue));//remo_con_barra
        ejercicios.add(new Ejercicio("Face Pull", 3, 15, R.drawable.blue));//face_pull
        ejercicios.add(new Ejercicio("Curl de Biceps", 4, 12, R.drawable.blue));//curl_de_biceps
        ejercicios.add(new Ejercicio("Curl Martillo", 3, 12, R.drawable.blue));//curl_martillo

        ejercicios.add(new Ejercicio("Sentadilla", 4, 10, R.drawable.blue));//sentadilla
        ejercicios.add(new Ejercicio("Prensa", 4, 12, R.drawable.blue));//prensa
        ejercicios.add(new Ejercicio("Peso Muerto Rumano", 4, 10, R.drawable.blue));//peso_muerto_rumano
        ejercicios.add(new Ejercicio("Extensiones", 3, 15, R.drawable.blue));//extensiones
        ejercicios.add(new Ejercicio("Curl Femoral", 3, 15, R.drawable.blue));//curl_femoral
        ejercicios.add(new Ejercicio("Gemelos", 4, 20, R.drawable.blue));//gemelos

        return ejercicios;
    }

    public static Ejercicio buscarPorNombre(String nombre) {

        for (Ejercicio ejercicio : getEjercicios()) {
            if (ejercicio.getNombre().equalsIgnoreCase(nombre)) {
                return ejercicio;
            }
        }

        return null;
    }
}