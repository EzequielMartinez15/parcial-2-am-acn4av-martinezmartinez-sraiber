package parcial2.am.acn4av.martinezmartinez.sraiber.model;

public class EjercicioRutina {
    //Esta clase refiere a los ejercicios que están en la clase de Rutina. Puse el idFirebase porque con esa luego vamos a buscar en el catálogo de ejercicios guardados la información de los ejercicios, como el nombre y vamos a agregar esa info a una instrancia de esta clase
    private String idEnFirebase;
    private String nombre;
    private int series;
    private int repeticiones;
    private int imagen;
    private String nombreImg;

    public EjercicioRutina(String idEnFirebase, String nombre, int series, int repeticiones, int imagen) {
        this.idEnFirebase = idEnFirebase;
        this.nombre = nombre;
        this.series = series;
        this.repeticiones = repeticiones;
        this.imagen = imagen;
    }
    public EjercicioRutina(String idEnFirebase, String nombre, int series, int repeticiones, String nombreImg) {
        this.idEnFirebase = idEnFirebase;
        this.nombre = nombre;
        this.series = series;
        this.repeticiones = repeticiones;
        this.nombreImg = nombreImg;
    }

    public EjercicioRutina(String nombre, int series, int repeticiones, String nombreImg) {
        this.nombre = nombre;
        this.series = series;
        this.repeticiones = repeticiones;
        this.nombreImg = nombreImg;
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

    public String getIdEnFirebase() {
        return idEnFirebase;
    }

    public void setIdEnFirebase(String idEnFirebase) {
        this.idEnFirebase = idEnFirebase;
    }

    public String getNombreImg() {
        return nombreImg;
    }

    public void setNombreImg(String nombreImg) {
        this.nombreImg = nombreImg;
    }
}