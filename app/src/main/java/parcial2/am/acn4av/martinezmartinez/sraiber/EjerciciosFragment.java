package parcial2.am.acn4av.martinezmartinez.sraiber;

import android.os.Bundle;
import android.widget.ArrayAdapter;

import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import parcial2.am.acn4av.martinezmartinez.sraiber.Repository.EjercicioRepository;
import parcial2.am.acn4av.martinezmartinez.sraiber.adapter.EjerciciosAdapterEnActivityEjercicios;
import parcial2.am.acn4av.martinezmartinez.sraiber.model.EjercicioCatalogo;

public class EjerciciosFragment extends Fragment implements AgregarModificarEliminarEjercicioEnCatalogoDialog.ListenerAgregarEjercicioCatalogo,
        AgregarModificarEliminarEjercicioEnCatalogoDialog.ListenerEditarEjercicioCatalogo,
        AgregarModificarEliminarEjercicioEnCatalogoDialog.ListenerBorrarEjercicioCatalogo{

    private RecyclerView rvEjercicios;
    private MaterialAutoCompleteTextView menuFiltros;
    private FloatingActionButton fabAgregarEjercicio;

    private EjerciciosAdapterEnActivityEjercicios adapter;

    private List<EjercicioCatalogo> ejercicios = new ArrayList<>();
    private List<EjercicioCatalogo> ejerciciosFiltrados = new ArrayList<>();

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_ejercicios,
                container,
                false);

        rvEjercicios =
                view.findViewById(R.id.rvEjercicios);

        menuFiltros =
                view.findViewById(R.id.menu_drop_filtros);

        fabAgregarEjercicio =
                view.findViewById(R.id.fabAgregarEjercicio);


        fabAgregarEjercicio.setOnClickListener(v -> {

            AgregarModificarEliminarEjercicioEnCatalogoDialog dialog =
                    AgregarModificarEliminarEjercicioEnCatalogoDialog
                            .crearAgregar();

            dialog.setListenerAgregar(this);

            dialog.show(
                    getParentFragmentManager(),
                    "agregarEjercicio");

        });

        cargarEjercicios();

        inicializarRecycler();
        inicializarFiltros();

        mostrarTodos();

        return view;
    }

    private void cargarEjercicios() {

        ejercicios.clear();

        ejercicios.addAll(
                EjercicioRepository.getCatalogoCache()
        );

        ejercicios.addAll(
                EjercicioRepository.getCatalogoCreadosUserCache()
        );
    }

    private void inicializarRecycler() {

        rvEjercicios.setLayoutManager(
                new GridLayoutManager(requireContext(), 1));


        adapter = new EjerciciosAdapterEnActivityEjercicios(
                ejerciciosFiltrados,
                requireContext(),
                EjercicioRepository.getIdsEjerciciosUsuario(),
                this, this
        );

        rvEjercicios.setAdapter(adapter);
    }

    private void inicializarFiltros() {

        String[] filtros = {
                "Todos",
                "Tus ejercicios",
                "Biceps",
                "Triceps",
                "Pecho",
                "Espalda",
                "Hombros",
                "Piernas",
                "Gluteos",
                "Abdominales",
                "Cardio"
        };

        ArrayAdapter<String> adapterFiltros =
                new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_list_item_1,
                        filtros
                );

        menuFiltros.setAdapter(adapterFiltros);

        menuFiltros.setText("Todos", false);

        menuFiltros.setOnItemClickListener(
                (parent, view, position, id) -> {

                    String seleccion =
                            parent.getItemAtPosition(position)
                                    .toString();

                    aplicarFiltro(seleccion);
                });
    }

    private void mostrarTodos() {

        ejerciciosFiltrados.clear();

        ejerciciosFiltrados.addAll(ejercicios);

        adapter.notifyDataSetChanged();
    }

    private void aplicarFiltro(String grupo) {

        ejerciciosFiltrados.clear();

        if(grupo.equalsIgnoreCase("Todos")){

            ejerciciosFiltrados.addAll(EjercicioRepository.getCatalogoCache());
            ejerciciosFiltrados.addAll(
                    EjercicioRepository.getCatalogoCreadosUserCache()
            );

        } else if(grupo.equalsIgnoreCase("Tus ejercicios")){

            ejerciciosFiltrados.addAll(
                    EjercicioRepository.getCatalogoCreadosUserCache()
            );
        }else{

            for(EjercicioCatalogo ejercicio : ejercicios){

                if(ejercicio.getGrupoMuscular()
                        .equalsIgnoreCase(grupo)){

                    ejerciciosFiltrados.add(ejercicio);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onAgregarEjercicioCatalogo(
            String nombre,
            String grupo,
            String nombreFoto) {
        if(FirebaseAuth.getInstance().getCurrentUser() == null){
            return false;
        }
        String uid =
                FirebaseAuth.getInstance()
                        .getCurrentUser()
                        .getUid();
        EjercicioRepository.agregarEjercicio(
                uid,
                nombre,
                grupo,
                nombreFoto,
                new EjercicioRepository.CallbackGuardado() {

                    @Override
                    public void onSuccess() {

                        cargarEjercicios();

                        adapter.actualizarIdsUsuario(
                                EjercicioRepository.getIdsEjerciciosUsuario()
                        );

                        mostrarTodos();
                    }

                    @Override
                    public void onError(Exception e) {

                        e.printStackTrace();

                    }
                });

        return true;
    }

    @Override
    public boolean onEditarEjercicioCatalogo(
            String id,
            String nombre,
            String grupo,
            String nombreFoto) {

        if(FirebaseAuth.getInstance().getCurrentUser() == null){
            return false;
        }
        String uid =
                FirebaseAuth.getInstance()
                        .getCurrentUser()
                        .getUid();
        EjercicioRepository.editarEjercicio(
                uid,
                id,
                nombre,
                grupo,
                nombreFoto,
                new EjercicioRepository.CallbackGuardado() {

                    @Override
                    public void onSuccess() {

                        cargarEjercicios();

                        adapter.actualizarIdsUsuario(
                                EjercicioRepository.getIdsEjerciciosUsuario()
                        );

                        mostrarTodos();
                    }

                    @Override
                    public void onError(Exception e) {

                        e.printStackTrace();
                    }
                });

        return true;
    }

    @Override
    public boolean onBorrarEjercicioCatalogo(
            String id) {

        if(FirebaseAuth.getInstance().getCurrentUser() == null){
            return false;
        }
        String uid =
                FirebaseAuth.getInstance()
                        .getCurrentUser()
                        .getUid();
        EjercicioRepository.borrarEjercicio(
                uid,
                id,
                new EjercicioRepository.CallbackGuardado() {

                    @Override
                    public void onSuccess() {

                        cargarEjercicios();

                        adapter.actualizarIdsUsuario(
                                EjercicioRepository.getIdsEjerciciosUsuario()
                        );

                        mostrarTodos();
                    }

                    @Override
                    public void onError(Exception e) {

                        e.printStackTrace();
                    }
                });

        return true;
    }
}