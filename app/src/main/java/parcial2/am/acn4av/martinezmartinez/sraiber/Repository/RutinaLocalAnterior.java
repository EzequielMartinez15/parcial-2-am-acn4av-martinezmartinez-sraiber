package parcial2.am.acn4av.martinezmartinez.sraiber.Repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import parcial2.am.acn4av.martinezmartinez.sraiber.model.Ejercicio;
import parcial2.am.acn4av.martinezmartinez.sraiber.model.Rutina;

public class RutinaLocalAnterior {
    private static final String DATOS = "rutinas_actualizadas";
    private static final String BORRADAS = "rutinas_borradas";

    // Guardé la lista completa para conservar cambios en ejercicios originales y agregados.
    public static List<Rutina> getRutinas(Context context) {
        List<Rutina> rutinas = getRutinas();
        SharedPreferences preferencias = context.getSharedPreferences(DATOS, Context.MODE_PRIVATE);
        Set<String> borradas = preferencias.getStringSet(BORRADAS, new HashSet<>());
        rutinas.removeIf(rutina -> borradas.contains(rutina.getNombre()));
        for (Rutina rutina : rutinas) {
            try {
                if (preferencias.contains(rutina.getNombre())) {
                    List<Ejercicio> recuperados = leerEjercicios(preferencias.getString(rutina.getNombre(), "[]"));
                    rutina.getEjercicios().clear();
                    rutina.getEjercicios().addAll(recuperados);
                } else {
                    // Recuperé lo guardado con la versión anterior sin modificar esos archivos.
                    String agregados = context.getSharedPreferences("ejercicios_agregados", Context.MODE_PRIVATE)
                            .getString(rutina.getNombre(), "[]");
                    rutina.getEjercicios().addAll(leerEjercicios(agregados));
                    JSONArray eliminados = new JSONArray(context.getSharedPreferences("ejercicios_borrados", Context.MODE_PRIVATE)
                            .getString(rutina.getNombre(), "[]"));
                    for (int i = 0; i < eliminados.length(); i++) {
                        String nombre = eliminados.getString(i);
                        rutina.getEjercicios().removeIf(e -> e.getNombre().equals(nombre));
                    }
                }
            } catch (JSONException e) {
                Log.e("RutinaRepository", "No se pudieron leer los ejercicios guardados", e);
            }
        }
        return rutinas;
    }

    private static List<Ejercicio> leerEjercicios(String datos) throws JSONException {
        List<Ejercicio> ejercicios = new ArrayList<>();
        JSONArray lista = new JSONArray(datos);
        for (int i = 0; i < lista.length(); i++) {
            JSONObject item = lista.getJSONObject(i);
            String nombre = item.getString("nombre");
            ejercicios.add(new Ejercicio(nombre, item.getInt("series"), item.getInt("repeticiones"),
                    EjercicioRepository.imagenPorNombre(nombre)));
        }
        return ejercicios;
    }

    private static void guardar(Context context, Rutina rutina, List<Ejercicio> ejercicios) {
        try {
            JSONArray lista = new JSONArray();
            for (Ejercicio ejercicio : ejercicios) {
                JSONObject item = new JSONObject();
                item.put("nombre", ejercicio.getNombre());
                item.put("series", ejercicio.getSeries());
                item.put("repeticiones", ejercicio.getRepeticiones());
                lista.put(item);
            }
            context.getSharedPreferences(DATOS, Context.MODE_PRIVATE).edit()
                    .putString(rutina.getNombre(), lista.toString()).apply();
            rutina.getEjercicios().clear();
            rutina.getEjercicios().addAll(ejercicios);
        } catch (JSONException e) {
            throw new IllegalStateException("No se pudo guardar la rutina", e);
        }
    }

    public static void agregarEjercicio(Context context, Rutina rutina, Ejercicio ejercicio) {
        List<Ejercicio> actualizados = new ArrayList<>(rutina.getEjercicios());
        actualizados.add(ejercicio);
        guardar(context, rutina, actualizados);
    }

    public static void editarEjercicio(Context context, Rutina rutina, Ejercicio ejercicio,
                                       String nombre, int series, int repeticiones) {
        List<Ejercicio> actualizados = new ArrayList<>(rutina.getEjercicios());
        int posicion = actualizados.indexOf(ejercicio);
        if (posicion < 0) throw new IllegalStateException("El ejercicio ya no existe");
        actualizados.set(posicion, new Ejercicio(nombre, series, repeticiones,
                EjercicioRepository.imagenPorNombre(nombre)));
        guardar(context, rutina, actualizados);
    }

    public static void eliminarEjercicio(Context context, Rutina rutina, Ejercicio ejercicio) {
        List<Ejercicio> actualizados = new ArrayList<>(rutina.getEjercicios());
        if (!actualizados.remove(ejercicio)) throw new IllegalStateException("El ejercicio ya no existe");
        guardar(context, rutina, actualizados);
    }

    public static void eliminarRutina(Context context, Rutina rutina) {
        SharedPreferences preferencias = context.getSharedPreferences(DATOS, Context.MODE_PRIVATE);
        Set<String> borradas = new HashSet<>(preferencias.getStringSet(BORRADAS, new HashSet<>()));
        borradas.add(rutina.getNombre());
        preferencias.edit().putStringSet(BORRADAS, borradas).apply();
    }

    public static List<Rutina> getRutinas() {

        List<Rutina> rutinas = new ArrayList<>();

        try{
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.ROOT);

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
