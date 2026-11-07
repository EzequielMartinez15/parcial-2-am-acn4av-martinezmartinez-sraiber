package parcial2.am.acn4av.martinezmartinez.sraiber.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Rutina {

    private String nombre;
    private String id;
    private List<EjercicioRutina> ejercicios;
    private int vecesRealizada;
    private Date ultimaVezRealizada;

    public Rutina(
            String nombre,
            List<EjercicioRutina> ejercicios,
            int vecesRealizada,
            Date ultimaVezRealizada
    ) {
        this.nombre = nombre;
        this.id = java.util.UUID.nameUUIDFromBytes(nombre.getBytes(java.nio.charset.StandardCharsets.UTF_8)).toString();
        this.ejercicios = new ArrayList<>(ejercicios);
        this.vecesRealizada = vecesRealizada;
        this.ultimaVezRealizada = ultimaVezRealizada;
    }

    public String getNombre() {
        return nombre;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public List<EjercicioRutina> getEjercicios() {
        return ejercicios;
    }

    public int getVecesRealizada() {
        return vecesRealizada;
    }

    public Date getUltimaVezRealizada() {
        return ultimaVezRealizada;
    }
}
