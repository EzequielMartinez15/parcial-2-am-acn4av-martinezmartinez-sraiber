package parcial2.am.acn4av.martinezmartinez.sraiber;

public class HistorialActivity extends HomeActivity {
    @Override protected void onCreate(android.os.Bundle state) {
        if (!getIntent().hasExtra("seccion")) getIntent().putExtra("seccion", Navegacion.HISTORIAL);
        super.onCreate(state);
    }
}
