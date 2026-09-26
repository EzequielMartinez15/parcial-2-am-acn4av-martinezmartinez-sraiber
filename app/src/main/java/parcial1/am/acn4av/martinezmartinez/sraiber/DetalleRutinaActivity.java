package parcial1.am.acn4av.martinezmartinez.sraiber;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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
    private TextView txtTituloRutina;
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
        txtTituloRutina = findViewById(R.id.txtTituloRutina);
    }

    private void configurarRecyclerView() {
        String nombreRutina = getIntent().getStringExtra("nombre_rutina");

        Rutina rutinaSeleccionada = null;

        for (Rutina rutina : RutinaRepository.getRutinas(this)) {

            if (rutina.getNombre().equals(nombreRutina)) {
                rutinaSeleccionada = rutina;
                rutinaActual = rutina;

                txtTituloRutina.setText(rutina.getNombre());
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
                ejercicios,
                ejercicio -> {

                    new MaterialAlertDialogBuilder(
                            this,
                            R.style.ThemeOverlay_Marber_Dialog
                    )
                            .setTitle("Eliminar ejercicio")
                            .setMessage("¿Seguro que querés borrar el ejercicio " + ejercicio.getNombre() + "?")
                            .setNegativeButton("Cancelar", null)
                            .setPositiveButton("Eliminar", (dialog, which) -> {

                                        RutinaRepository.eliminarEjercicio(
                                                this,
                                                rutinaActual,
                                                ejercicio
                                        );

                                        adapter.notifyDataSetChanged();

                                        Snackbar.make(
                                                main,
                                                "Ejercicio borrado correctamente",
                                                Snackbar.LENGTH_SHORT
                                        ).show();
                                    }
                            )
                            .show();
                });

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
