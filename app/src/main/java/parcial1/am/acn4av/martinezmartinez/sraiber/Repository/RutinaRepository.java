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
    private static final String PREFERENCIASBORRADOS = "ejercicios_borrados";

    // Dejé los datos de prueba y sumé los ejercicios que el usuario guardó en este dispositivo.
    public static List<Rutina> getRutinas(Context context) {
        List<Rutina> rutinas = getRutinas();
        SharedPreferences preferencias = context.getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE);
        SharedPreferences preferenciasBorrados = context.getSharedPreferences(PREFERENCIASBORRADOS, Context.MODE_PRIVATE);
        for (Rutina rutina : rutinas) {
            try {
                JSONArray guardados = new JSONArray(preferencias.getString(rutina.getNombre(), "[]"));
                for (int i = 0; i < guardados.length(); i++) {
                    JSONObject ejercicio = guardados.getJSONObject(i);
                    rutina.getEjercicios().add(new Ejercicio(
                            ejercicio.getString("nombre"), ejercicio.getInt("series"),
                            ejercicio.getInt("repeticiones"), R.drawable.blue));
                }

                //Acá estoy sacando de la lista de la rutina los ejercicios borrados, esto sirve para que no aparezcan los ejercicios hardcodeados en la rutina hardcodeada si es que se borraron
                JSONArray eliminados = new JSONArray(preferenciasBorrados.getString(rutina.getNombre(), "[]"));
                for (int i = 0; i < eliminados.length(); i++) {
                    String nombreEjercicio = eliminados.getString(i);
                    rutina.getEjercicios().removeIf(ejercicio ->
                                    ejercicio.getNombre().equals(nombreEjercicio)
                    );
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

    public static void editarEjercicio(Context context, Rutina rutina, Ejercicio ejercicio,  String nombreNuevo, int seriesNuevo, int repeticionesNuevo) {
        SharedPreferences preferencias = context.getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE);
        try {
            JSONArray guardados = new JSONArray(preferencias.getString(rutina.getNombre(), "[]"));
            for (int i = 0; i < guardados.length(); i++) {

                JSONObject jsonEjercicio = guardados.getJSONObject(i);

                boolean esElMismo =
                        jsonEjercicio.getString("nombre")
                                .equals(ejercicio.getNombre())
                                &&
                                jsonEjercicio.getInt("series")
                                        == ejercicio.getSeries()
                                &&
                                jsonEjercicio.getInt("repeticiones")
                                        == ejercicio.getRepeticiones();
                if (esElMismo) {
                    jsonEjercicio.put("nombre", nombreNuevo);
                    jsonEjercicio.put("series", seriesNuevo);
                    jsonEjercicio.put("repeticiones", repeticionesNuevo);

                    break;
                }
            }

            preferencias.edit().putString(rutina.getNombre(), guardados.toString()).apply();

            ejercicio.setNombre(nombreNuevo);
            ejercicio.setSeries(seriesNuevo);
            ejercicio.setRepeticiones(repeticionesNuevo);
        } catch (JSONException e) {
            throw new IllegalStateException("No se pudo editar el ejercicio", e);
        }
    }
    public static void eliminarEjercicio(Context context, Rutina rutina, Ejercicio ejercicioAEliminar) {

        SharedPreferences preferenciasBorrados = context.getSharedPreferences(PREFERENCIASBORRADOS, Context.MODE_PRIVATE);
        try {

            JSONArray eliminados = new JSONArray(preferenciasBorrados.getString(rutina.getNombre(), "[]"));
            eliminados.put(ejercicioAEliminar.getNombre());

            preferenciasBorrados.edit().putString(rutina.getNombre(), eliminados.toString()).apply();

            rutina.getEjercicios().remove(ejercicioAEliminar);

        } catch (JSONException e) {
            e.printStackTrace();
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
