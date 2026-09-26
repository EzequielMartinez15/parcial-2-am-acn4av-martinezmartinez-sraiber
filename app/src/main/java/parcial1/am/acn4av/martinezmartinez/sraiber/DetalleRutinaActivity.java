package parcial1.am.acn4av.martinezmartinez.sraiber;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import parcial1.am.acn4av.martinezmartinez.sraiber.Repository.RutinaRepository;
import parcial1.am.acn4av.martinezmartinez.sraiber.adapter.EjercicioDetalleRutinaAdapter;
import parcial1.am.acn4av.martinezmartinez.sraiber.model.Ejercicio;
import parcial1.am.acn4av.martinezmartinez.sraiber.model.Rutina;

public class DetalleRutinaActivity extends AppCompatActivity {

    private View main;
    private RecyclerView rvEjercicios;
    private MaterialButton btnAgregarEjercicio;
    private MaterialButton btnBorrarRutina;

    private EjercicioDetalleRutinaAdapter adapter;
    private Rutina rutinaActual;
    private List<Ejercicio> ejercicios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detalle_rutina);

        main = findViewById(android.R.id.content);

        ViewCompat.setOnApplyWindowInsetsListener(main, (v, insets) -> {
            Insets systemBars =
                    insets.getInsets(WindowInsetsCompat.Type.systemBars());

            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
            );

            return insets;
        });

        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(getWindow(), main);

        getWindow().setStatusBarColor(
                ContextCompat.getColor(this, R.color.marber_background)
        );

        controller.setAppearanceLightStatusBars(false);
        controller.setAppearanceLightNavigationBars(false);

        inicializarVistas();
        configurarRecyclerView();
        configurarBotones();
    }

    private void inicializarVistas() {

        rvEjercicios = findViewById(R.id.rvEjercicios);
        btnAgregarEjercicio = findViewById(R.id.btnAgregarEjercicio);
        btnBorrarRutina = findViewById(R.id.btnBorrarRutina);
    }

    private void configurarRecyclerView() {
        String nombreRutina = getIntent().getStringExtra("nombre_rutina");

        Rutina rutinaSeleccionada = null;

        for (Rutina rutina : RutinaRepository.getRutinas(this)) {

            if (rutina.getNombre().equals(nombreRutina)) {
                rutinaSeleccionada = rutina;
                rutinaActual = rutina;
                break;
            }
        }
        if (rutinaSeleccionada == null) {
            finish();
            return;
        }
        ejercicios = rutinaSeleccionada.getEjercicios();
        adapter = new EjercicioDetalleRutinaAdapter(
                this,
                ejercicios
        );

        rvEjercicios.setLayoutManager(
                new LinearLayoutManager(this)
        );

        rvEjercicios.setAdapter(adapter);
    }

    private void configurarBotones() {

        btnAgregarEjercicio.setOnClickListener(v -> {

            Snackbar.make(
                    main,
                    "Agregar ejercicio",
                    Snackbar.LENGTH_SHORT
            ).show();

        });

        btnBorrarRutina.setOnClickListener(v -> {

            Snackbar.make(
                    main,
                    "Borrar rutina",
                    Snackbar.LENGTH_SHORT
            ).show();

        });
    }
}
