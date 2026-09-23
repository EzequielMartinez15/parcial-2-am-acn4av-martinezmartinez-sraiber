package parcial1.am.acn4av.martinezmartinez.sraiber;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;
import java.util.Comparator;

import parcial1.am.acn4av.martinezmartinez.sraiber.Repository.RutinaRepository;
import parcial1.am.acn4av.martinezmartinez.sraiber.adapter.RutinaAdapter;
import parcial1.am.acn4av.martinezmartinez.sraiber.model.Ejercicio;
import parcial1.am.acn4av.martinezmartinez.sraiber.model.Rutina;

public class HomeActivity extends AppCompatActivity implements AgregarEjercicioDialog.Listener {
    private static final String ORDEN_SELECCIONADO = "orden_seleccionado";
    private int ordenSeleccionado = 0;

    private View home;

    private AutoCompleteTextView menuFiltros;

    private List<Rutina> rutinas;
    private RutinaAdapter rutinaAdapter;

    private RecyclerView rvRutinas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        home = findViewById(android.R.id.content);
        rutinas = RutinaRepository.getRutinas(this);

        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(getWindow(), home);

        getWindow().setStatusBarColor(
                ContextCompat.getColor(this, R.color.marber_background)
        );
        controller.setAppearanceLightStatusBars(false);
        controller.setAppearanceLightNavigationBars(false);

        //declaración de las variables de los botones de la activity
        MaterialButton logoutButton = findViewById(R.id.btn_logout);
        menuFiltros = findViewById(R.id.menu_drop_filtros);

        //Pongo datos en el card para probar
        rvRutinas = findViewById(R.id.rv_rutinas);

        rvRutinas.setLayoutManager(
                new LinearLayoutManager(this)
        );
        rutinaAdapter = new RutinaAdapter(rutinas, rutina -> {
            if (getSupportFragmentManager().findFragmentByTag("agregar_ejercicio") == null) {
                AgregarEjercicioDialog.crear(rutina.getNombre())
                        .show(getSupportFragmentManager(), "agregar_ejercicio");
            }
        });
        rvRutinas.setAdapter(rutinaAdapter);
        //Estos son los filtros que van a aparecer en el layout, osea el xml
        String[] filtros = getResources().getStringArray(R.array.orden_rutinas);

        //este objeto toma el contexto, un layout apra mostrar los datos, los datos
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                filtros
        );

        //acá estoy pasando los datos para el layout nuestro y que se agregue eso a nuestro filtro
        menuFiltros.setAdapter(adapter);
        menuFiltros.setOnClickListener(v -> menuFiltros.showDropDown());
        com.google.android.material.textfield.TextInputLayout campoOrden = findViewById(R.id.input_filtros);
        campoOrden.setEndIconOnClickListener(v -> menuFiltros.showDropDown());

        menuFiltros.setOnItemClickListener((parent, view, position, id) -> aplicarOrden(position));
        // Guardé la selección para mantener el orden cuando se recrea la pantalla.
        if (savedInstanceState != null) {
            ordenSeleccionado = savedInstanceState.getInt(ORDEN_SELECCIONADO, 0);
        }
        aplicarOrden(ordenSeleccionado);


        //Evento para simular logout, como no hay una autenticación simplemente vuelve a la activity de login.
        //Abajo del evento del logout puse un get que obtiene cuando el user toca el botón
        //de ir para atrás en el panel de navegación y que eso le de el alert de cerrar sesion.
        logoutButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            popUpCerrarSesion(intent);
        });
        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                        popUpCerrarSesion(intent);
                    }
                });
    }

    @Override
    public boolean onAgregarEjercicio(String nombreRutina, String nombre, int series, int repeticiones) {
        for (int i = 0; i < rutinas.size(); i++) {
            Rutina rutina = rutinas.get(i);
            if (rutina.getNombre().equals(nombreRutina)) {
                try {
                    RutinaRepository.agregarEjercicio(this, rutina,
                            new Ejercicio(nombre, series, repeticiones, R.drawable.blue));
                    rutinaAdapter.notifyItemChanged(i, RutinaAdapter.EJERCICIO_AGREGADO);
                    Snackbar.make(rvRutinas, R.string.ejercicio_agregado, Snackbar.LENGTH_SHORT).show();
                    return true;
                } catch (IllegalStateException e) {
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(ORDEN_SELECCIONADO, ordenSeleccionado);
        super.onSaveInstanceState(outState);
    }

    private void aplicarOrden(int opcion) {
        ordenSeleccionado = opcion;
        switch (opcion) {
            case 0:
                ordenarPorFechaReciente();
                break;
            case 1:
                ordenarPorFechaAntigua();
                break;
            case 2:
                ordenarPorMasRealizadas();
                break;
            case 3:
                ordenarPorMenosRealizadas();
                break;
            default:
                ordenSeleccionado = 0;
                ordenarPorFechaReciente();
        }
        // Usé false para que el texto elegido no filtre las opciones del desplegable.
        menuFiltros.setText(getResources().getStringArray(R.array.orden_rutinas)[ordenSeleccionado], false);
        rutinaAdapter.notifyDataSetChanged();
        rvRutinas.scrollToPosition(0);
    }

    private void ordenarPorFechaReciente() {
        rutinas.sort(Comparator.comparing(Rutina::getUltimaVezRealizada).reversed()
                .thenComparing(Rutina::getNombre));
    }

    private void ordenarPorFechaAntigua() {
        rutinas.sort(Comparator.comparing(Rutina::getUltimaVezRealizada)
                .thenComparing(Rutina::getNombre));
    }

    private void ordenarPorMasRealizadas() {
        rutinas.sort(Comparator.comparingInt(Rutina::getVecesRealizada).reversed()
                .thenComparing(Rutina::getNombre));
    }

    private void ordenarPorMenosRealizadas() {
        rutinas.sort(Comparator.comparingInt(Rutina::getVecesRealizada)
                .thenComparing(Rutina::getNombre));
    }
    protected void popUpCerrarSesion(Intent intent){
        new AlertDialog.Builder(HomeActivity.this)
                .setTitle("Cerrar sesión")
                .setMessage("¿Seguro que quieres salir de tu sesión?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
