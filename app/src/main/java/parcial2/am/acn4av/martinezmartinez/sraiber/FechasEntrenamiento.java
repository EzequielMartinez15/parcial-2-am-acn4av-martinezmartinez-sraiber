package parcial2.am.acn4av.martinezmartinez.sraiber;

import java.util.Calendar;

public final class FechasEntrenamiento {
    private FechasEntrenamiento() { }
    public static long dia(long tiempo) {
        Calendar c = Calendar.getInstance(); c.setTimeInMillis(tiempo);
        c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0); c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }
    public static boolean mismoDia(long a, long b) { return dia(a) == dia(b); }
    public static long semana(long tiempo) {
        Calendar c = Calendar.getInstance(); c.setTimeInMillis(dia(tiempo));
        int desplazamiento = (c.get(Calendar.DAY_OF_WEEK) + 5) % 7;
        c.add(Calendar.DAY_OF_MONTH, -desplazamiento); return c.getTimeInMillis();
    }
    public static long sumarDias(long tiempo, int dias) {
        Calendar c = Calendar.getInstance(); c.setTimeInMillis(tiempo); c.add(Calendar.DAY_OF_MONTH, dias); return c.getTimeInMillis();
    }
    public static boolean enSemana(long tiempo, long seleccion) {
        long inicio = semana(seleccion); return tiempo >= inicio && tiempo < sumarDias(inicio, 7);
    }
}
