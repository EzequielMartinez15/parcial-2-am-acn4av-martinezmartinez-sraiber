package parcial2.am.acn4av.martinezmartinez.sraiber;

public class RutinasActivity extends HomeActivity {
    @Override protected void onCreate(android.os.Bundle state) {
        if (!getIntent().hasExtra("seccion")) getIntent().putExtra("seccion", Navegacion.RUTINAS);
        super.onCreate(state);
    }
}
