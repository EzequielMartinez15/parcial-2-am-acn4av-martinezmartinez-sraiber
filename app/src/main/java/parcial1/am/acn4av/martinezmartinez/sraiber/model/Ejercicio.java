package parcial1.am.acn4av.martinezmartinez.sraiber.model;

public class Ejercicio {

    private String nombre;
    private int series;
    private int repeticiones;
    private int imagen;

    public Ejercicio(String nombre, int series, int repeticiones, int imagen) {
        this.nombre = nombre;
        this.series = series;
        this.repeticiones = repeticiones;
        this.imagen = imagen;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setSeries(int series) {
        this.series = series;
    }

    public void setRepeticiones(int repeticiones) {
        this.repeticiones = repeticiones;
    }

    public String getNombre() {
        return nombre;
    }

    public int getSeries() {
        return series;
    }

    public int getRepeticiones() {
        return repeticiones;
    }

    public int getImagen() {
        return imagen;
    }
}