package parcial2.am.acn4av.martinezmartinez.sraiber.model;

public class EjercicioCatalogo {
    //Esta clase refiere a los ejercicios que se crean y se guardan como ejercicio normal sin que se asocie a una rutina, por ejemplo, Bici estática

    public void setId(String id) {
        this.id = id;
    }

    private String id;
    private String grupoMuscular;
    private String nombre;
    private String nombreFoto;

    public EjercicioCatalogo() {
    }

    public EjercicioCatalogo(String id, String grupoMuscular, String nombre, String nombreFoto) {
        this.id = id;
        this.grupoMuscular = grupoMuscular;
        this.nombre = nombre;
        this.nombreFoto = nombreFoto;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setGrupoMuscular(String grupoMuscular) {
        this.grupoMuscular = grupoMuscular;
    }

    public void setNombreFoto(String nombreFoto) {
        this.nombreFoto = nombreFoto;
    }

    public String getNombre() {
        return nombre;
    }

    public String getGrupoMuscular() {
        return grupoMuscular;
    }

    public String getNombreFoto() {
        return nombreFoto;
    }

    public String getId() {
        return id;
    }
}
