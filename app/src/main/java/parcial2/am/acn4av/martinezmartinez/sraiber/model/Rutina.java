package parcial2.am.acn4av.martinezmartinez.sraiber.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Rutina {

    private String nombre;
    private List<Ejercicio> ejercicios;
    private int vecesRealizada;
    private Date ultimaVezRealizada;

    public Rutina(
            String nombre,
            List<Ejercicio> ejercicios,
            int vecesRealizada,
            Date ultimaVezRealizada
    ) {
        this.nombre = nombre;
        this.ejercicios = new ArrayList<>(ejercicios);
        this.vecesRealizada = vecesRealizada;
        this.ultimaVezRealizada = ultimaVezRealizada;
    }

    public String getNombre() {
        return nombre;
    }

    public List<Ejercicio> getEjercicios() {
        return ejercicios;
    }

    public int getVecesRealizada() {
        return vecesRealizada;
    }

    public Date getUltimaVezRealizada() {
        return ultimaVezRealizada;
    }
}