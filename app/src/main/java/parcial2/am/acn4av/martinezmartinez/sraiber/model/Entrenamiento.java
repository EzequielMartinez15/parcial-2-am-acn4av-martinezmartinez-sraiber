package parcial2.am.acn4av.martinezmartinez.sraiber.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Entrenamiento {
    public String id = "", rutinaId = "", nombre = "", notas = "";
    public long inicio, fin;
    public List<Movimiento> ejercicios = new ArrayList<>();

    public static class Serie {
        public String peso = "", repeticiones = "";
        public boolean realizada;
        public Map<String, Object> datos() {
            Map<String, Object> d = new HashMap<>();
            d.put("peso", peso); d.put("repeticiones", repeticiones); d.put("realizada", realizada);
            return d;
        }
        public boolean valida() {
            try {
                double kg = Double.parseDouble(peso.replace(',', '.'));
                int reps = Integer.parseInt(repeticiones);
                return Double.isFinite(kg) && kg >= 0 && kg <= 2000 && reps > 0 && reps <= 10000;
            } catch (NumberFormatException e) { return false; }
        }
        public double kilos() { return Double.parseDouble(peso.replace(',', '.')); }
        public int reps() { return Integer.parseInt(repeticiones); }
    }

    public static class Movimiento {
        public String clave = "", nombre = "", notas = "";
        public List<Serie> series = new ArrayList<>();
        public Map<String, Object> datos() {
            Map<String, Object> d = new HashMap<>();
            d.put("clave", clave); d.put("nombre", nombre); d.put("notas", notas);
            List<Map<String, Object>> lista = new ArrayList<>();
            for (Serie s : series) lista.add(s.datos());
            d.put("series", lista);
            return d;
        }
    }

    public static Entrenamiento iniciar(Rutina rutina) {
        Entrenamiento e = new Entrenamiento();
        e.id = UUID.randomUUID().toString(); e.rutinaId = rutina.getId();
        e.nombre = rutina.getNombre(); e.inicio = System.currentTimeMillis();
        Map<String, Integer> repetidos = new HashMap<>();
        for (EjercicioRutina ejercicio : rutina.getEjercicios()) {
            Movimiento m = new Movimiento(); m.nombre = ejercicio.getNombre();
            String base = m.nombre.trim().toLowerCase(java.util.Locale.ROOT);
            int numero = repetidos.getOrDefault(base, 0);
            repetidos.put(base, numero + 1); m.clave = base + "#" + numero;
            for (int i = 0; i < ejercicio.getSeries(); i++) {
                Serie s = new Serie(); s.repeticiones = String.valueOf(ejercicio.getRepeticiones());
                m.series.add(s);
            }
            e.ejercicios.add(m);
        }
        return e;
    }

    public boolean completo() {
        if (ejercicios.isEmpty()) return false;
        for (Movimiento m : ejercicios) {
            if (m.series.isEmpty()) return false;
            for (Serie s : m.series) if (!s.realizada || !s.valida()) return false;
        }
        return true;
    }
    public int seriesHechas() {
        int cantidad = 0;
        for (Movimiento m : ejercicios) for (Serie s : m.series) if (s.realizada) cantidad++;
        return cantidad;
    }
    public int totalSeries() {
        int cantidad = 0;
        for (Movimiento m : ejercicios) cantidad += m.series.size();
        return cantidad;
    }
    public long minutos() { return Math.max(1, (fin - inicio) / 60000); }

    public Map<String, Object> datos() {
        Map<String, Object> d = new HashMap<>();
        d.put("id", id); d.put("rutinaId", rutinaId); d.put("nombre", nombre);
        d.put("notas", notas); d.put("inicio", inicio); d.put("fin", fin);
        List<Map<String, Object>> lista = new ArrayList<>();
        for (Movimiento m : ejercicios) lista.add(m.datos());
        d.put("ejercicios", lista);
        return d;
    }

    @SuppressWarnings("unchecked")
    public static Entrenamiento leer(Map<String, Object> d) {
        Entrenamiento e = new Entrenamiento();
        e.id = texto(d, "id"); e.rutinaId = texto(d, "rutinaId"); e.nombre = texto(d, "nombre");
        e.notas = texto(d, "notas"); e.inicio = numero(d, "inicio"); e.fin = numero(d, "fin");
        Object ejercicios = d.get("ejercicios");
        if (ejercicios instanceof List) for (Object obj : (List<?>) ejercicios) {
            if (!(obj instanceof Map)) continue;
            Map<String, Object> md = (Map<String, Object>) obj;
            Movimiento m = new Movimiento(); m.clave = texto(md, "clave");
            m.nombre = texto(md, "nombre"); m.notas = texto(md, "notas");
            Object series = md.get("series");
            if (series instanceof List) for (Object so : (List<?>) series) {
                if (!(so instanceof Map)) continue;
                Map<String, Object> sd = (Map<String, Object>) so;
                Serie s = new Serie(); s.peso = texto(sd, "peso"); s.repeticiones = texto(sd, "repeticiones");
                s.realizada = Boolean.TRUE.equals(sd.get("realizada")); m.series.add(s);
            }
            e.ejercicios.add(m);
        }
        return e;
    }
    public static String texto(Map<String, Object> d, String key) {
        Object valor = d.get(key); return valor == null ? "" : valor.toString();
    }
    public static long numero(Map<String, Object> d, String key) {
        Object valor = d.get(key); return valor instanceof Number ? ((Number) valor).longValue() : 0;
    }
}
