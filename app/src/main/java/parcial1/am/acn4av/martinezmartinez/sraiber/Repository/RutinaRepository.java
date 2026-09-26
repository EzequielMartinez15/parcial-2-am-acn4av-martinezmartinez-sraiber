package parcial1.am.acn4av.martinezmartinez.sraiber.Repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.text.ParseException;
import java.text.SimpleDateFormat;


import parcial1.am.acn4av.martinezmartinez.sraiber.R;
import parcial1.am.acn4av.martinezmartinez.sraiber.model.Ejercicio;
import parcial1.am.acn4av.martinezmartinez.sraiber.model.Rutina;

public class RutinaRepository {
    private static final String PREFERENCIAS = "ejercicios_agregados";

    // Dejé los datos de prueba y sumé los ejercicios que el usuario guardó en este dispositivo.
    public static List<Rutina> getRutinas(Context context) {
        List<Rutina> rutinas = getRutinas();
        SharedPreferences preferencias = context.getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE);
        for (Rutina rutina : rutinas) {
            try {
                JSONArray guardados = new JSONArray(preferencias.getString(rutina.getNombre(), "[]"));
                for (int i = 0; i < guardados.length(); i++) {
                    JSONObject ejercicio = guardados.getJSONObject(i);
                    rutina.getEjercicios().add(new Ejercicio(
                            ejercicio.getString("nombre"), ejercicio.getInt("series"),
                            ejercicio.getInt("repeticiones"), R.drawable.blue));
                }
            } catch (JSONException e) {
                Log.e("RutinaRepository", "No se pudieron leer los ejercicios guardados", e);
            }
        }
        return rutinas;
    }

    public static void agregarEjercicio(Context context, Rutina rutina, Ejercicio ejercicio) {
        SharedPreferences preferencias = context.getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE);
        try {
            JSONArray guardados = new JSONArray(preferencias.getString(rutina.getNombre(), "[]"));
            JSONObject nuevo = new JSONObject();
            nuevo.put("nombre", ejercicio.getNombre());
            nuevo.put("series", ejercicio.getSeries());
            nuevo.put("repeticiones", ejercicio.getRepeticiones());
            guardados.put(nuevo);
            preferencias.edit().putString(rutina.getNombre(), guardados.toString()).apply();
            rutina.getEjercicios().add(ejercicio);
        } catch (JSONException e) {
            throw new IllegalStateException("No se pudo guardar el ejercicio", e);
        }
    }

    public static List<Rutina> getRutinas() {

        List<Rutina> rutinas = new ArrayList<>();

        try{
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

            rutinas.add(
                    new Rutina(
                            "Push Day",
                            Arrays.asList(
                                    EjercicioRepository.buscarPorNombre("Press Banca"),
                                    EjercicioRepository.buscarPorNombre("Press Inclinado"),
                                    EjercicioRepository.buscarPorNombre("Aperturas"),
                                    EjercicioRepository.buscarPorNombre("Press Militar"),
                                    EjercicioRepository.buscarPorNombre("Elevaciones Laterales"),
                                    EjercicioRepository.buscarPorNombre("Fondos")
                            ),
                            8,
                            formatter.parse("15/09/2026")
                    )
            );

            rutinas.add(
                    new Rutina(
                            "Pull Day",
                            Arrays.asList(
                                    EjercicioRepository.buscarPorNombre("Dominadas"),
                                    EjercicioRepository.buscarPorNombre("Jalon al Pecho"),
                                    EjercicioRepository.buscarPorNombre("Remo con Barra"),
                                    EjercicioRepository.buscarPorNombre("Face Pull"),
                                    EjercicioRepository.buscarPorNombre("Curl de Biceps"),
                                    EjercicioRepository.buscarPorNombre("Curl Martillo")
                            ),
                            6,
                            formatter.parse("20/09/2026")
                    )
            );

            rutinas.add(
                    new Rutina(
                            "Leg Day",
                            Arrays.asList(
                                    EjercicioRepository.buscarPorNombre("Sentadilla"),
                                    EjercicioRepository.buscarPorNombre("Prensa"),
                                    EjercicioRepository.buscarPorNombre("Peso Muerto Rumano"),
                                    EjercicioRepository.buscarPorNombre("Extensiones"),
                                    EjercicioRepository.buscarPorNombre("Curl Femoral"),
                                    EjercicioRepository.buscarPorNombre("Gemelos")
                            ),
                            5,
                            formatter.parse("18/09/2026")
                    )
            );
        }catch (ParseException e) {
            e.printStackTrace();
        }


        return rutinas;
    }
}
