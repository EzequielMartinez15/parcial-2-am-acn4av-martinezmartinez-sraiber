package parcial2.am.acn4av.martinezmartinez.sraiber.ui.activities.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;
import java.util.Comparator;

import parcial2.am.acn4av.martinezmartinez.sraiber.R;
import parcial2.am.acn4av.martinezmartinez.sraiber.Repository.RutinaRepository;
import parcial2.am.acn4av.martinezmartinez.sraiber.Sesion;
import parcial2.am.acn4av.martinezmartinez.sraiber.ui.activities.UiEntrenamiento;
import parcial2.am.acn4av.martinezmartinez.sraiber.ui.adapterts.RutinaAdapter;
import parcial2.am.acn4av.martinezmartinez.sraiber.model.EjercicioCatalogo;
import parcial2.am.acn4av.martinezmartinez.sraiber.model.EjercicioRutina;
import parcial2.am.acn4av.martinezmartinez.sraiber.model.Rutina;
import parcial2.am.acn4av.martinezmartinez.sraiber.ui.activities.DetalleRutinaActivity;
import parcial2.am.acn4av.martinezmartinez.sraiber.ui.dialogs.AgregarModificarEjercicioEnRutinaDialog;
import parcial2.am.acn4av.martinezmartinez.sraiber.ui.dialogs.CrearRutinaDialog;

public class RutinasFragment extends androidx.fragment.app.Fragment implements AgregarModificarEjercicioEnRutinaDialog.ListenerAgregarEjercicio {
    private static final String ORDEN_SELECCIONADO = "orden_seleccionado";
    private int ordenSeleccionado = 0;

    private View home;

    private AutoCompleteTextView menuFiltros;

    private List<Rutina> rutinas;
    private RutinaAdapter rutinaAdapter;

    private RecyclerView rvRutinas;

    @Override public android.view.View onCreateView(android.view.LayoutInflater inflater,
            android.view.ViewGroup container, Bundle state) {
        return inflater.inflate(R.layout.activity_rutinas, container, false);
    }
    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        home = view;
        ((android.widget.TextView)home.findViewById(R.id.titulo_home)).setText(R.string.nav_rutinas);
        home.findViewById(R.id.btn_logout).setVisibility(View.GONE);
        ((View)home.findViewById(R.id.btn_logout).getParent()).setVisibility(View.GONE);
        rutinas = RutinaRepository.getRutinas(requireContext());
        android.widget.LinearLayout root = (android.widget.LinearLayout) home;
        com.google.android.material.button.MaterialButton crear = UiEntrenamiento.boton(root, R.string.crear_rutina, true, v -> {
            if (getChildFragmentManager().findFragmentByTag("crear_rutina") == null)
                new CrearRutinaDialog().show(getChildFragmentManager(), "crear_rutina");
        });
        root.removeView(crear);
        android.widget.LinearLayout.LayoutParams espacio = new android.widget.LinearLayout.LayoutParams(-1, -2);
        espacio.setMargins(UiEntrenamiento.dp(requireContext(), 20), UiEntrenamiento.dp(requireContext(), 12),
                UiEntrenamiento.dp(requireContext(), 20), 0);
        root.addView(crear, 3, espacio);

        //declaración de las variables de los botones de la activity
        menuFiltros = home.findViewById(R.id.menu_drop_filtros);

        //Pongo datos en el card para probar
        rvRutinas = home.findViewById(R.id.rv_rutinas);

        rvRutinas.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );
        rutinaAdapter = new RutinaAdapter(
                rutinas,
                rutina -> {
                    if (getChildFragmentManager().findFragmentByTag("agregar_ejercicio") == null) {
                        AgregarModificarEjercicioEnRutinaDialog.crearAgregar(rutina.getNombre())
                                .show(getChildFragmentManager(), "agregar_ejercicio");
                    }
                },
                rutina -> {
                    Intent intent =
                            new Intent(
                                    requireContext(),
                                    DetalleRutinaActivity.class
                            );
                    intent.putExtra(
                            "nombre_rutina",
                            rutina.getNombre()
                    );
                    Sesion.agregarUsuario(intent);
                    intent.putExtra("rutina_id", rutina.getId());
                    startActivity(intent);
                }
        );
        rvRutinas.setAdapter(rutinaAdapter);
        //Estos son los filtros que van a aparecer en el layout, osea el xml
        String[] filtros = getResources().getStringArray(R.array.orden_rutinas);

        //este objeto toma el contexto, un layout apra mostrar los datos, los datos
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.item_dropdown_marber,
                filtros
        );

        //acá estoy pasando los datos para el layout nuestro y que se agregue eso a nuestro filtro
        menuFiltros.setAdapter(adapter);
        menuFiltros.setOnClickListener(v -> menuFiltros.showDropDown());
        com.google.android.material.textfield.TextInputLayout campoOrden = home.findViewById(R.id.input_filtros);
        campoOrden.setEndIconOnClickListener(v -> menuFiltros.showDropDown());

        menuFiltros.setOnItemClickListener((parent, itemView, position, id) -> aplicarOrden(position));
        // Guardé la selección para mantener el orden cuando se recrea la pantalla.
        if (savedInstanceState != null) {
            ordenSeleccionado = savedInstanceState.getInt(ORDEN_SELECCIONADO, 0);
        }
        aplicarOrden(ordenSeleccionado);


        parcial2.am.acn4av.martinezmartinez.sraiber.Repository.DatosUsuario datos =
                parcial2.am.acn4av.martinezmartinez.sraiber.Repository.DatosUsuario.obtener(requireContext());
        datos.cambios.observe(getViewLifecycleOwner(), cambio -> {
            rutinas.clear(); rutinas.addAll(RutinaRepository.getRutinas(requireContext()));
            aplicarOrden(ordenSeleccionado);
            home.findViewById(R.id.home_vacio).setVisibility(rutinas.isEmpty() ? View.VISIBLE : View.GONE);
        });
        datos.error.observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) Snackbar.make(home, error, Snackbar.LENGTH_LONG).show();
        });
    }

    @Override
    public boolean onAgregarEjercicio(String nombreRutina, EjercicioCatalogo ejercicioCatalogo, int series, int repeticiones) {
        for (int i = 0; i < rutinas.size(); i++) {
            Rutina rutina = rutinas.get(i);
            if (rutina.getNombre().equals(nombreRutina)) {
                try {
                    com.google.android.gms.tasks.Task<Void> guardado = RutinaRepository.agregarEjercicio(requireContext(), rutina, new EjercicioRutina(ejercicioCatalogo.getId(), ejercicioCatalogo.getNombre(), series,
                                    repeticiones, ejercicioCatalogo.getNombreFoto()));
                    rutinaAdapter.notifyItemChanged(i, RutinaAdapter.EJERCICIO_AGREGADO);
                    Snackbar.make(rvRutinas, R.string.operacion_guardando, Snackbar.LENGTH_SHORT).show();
                    guardado.addOnSuccessListener(v -> { if (getView() != null)
                        Snackbar.make(rvRutinas, R.string.ejercicio_agregado, Snackbar.LENGTH_SHORT).show(); });
                    return true;
                } catch (IllegalStateException e) {
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
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
}
