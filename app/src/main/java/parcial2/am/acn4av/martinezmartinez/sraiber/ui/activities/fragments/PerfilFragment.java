package parcial2.am.acn4av.martinezmartinez.sraiber.ui.activities.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import parcial2.am.acn4av.martinezmartinez.sraiber.Navegacion;
import parcial2.am.acn4av.martinezmartinez.sraiber.R;
import parcial2.am.acn4av.martinezmartinez.sraiber.Repository.EjercicioRepository;
import parcial2.am.acn4av.martinezmartinez.sraiber.Sesion;
import parcial2.am.acn4av.martinezmartinez.sraiber.model.Entrenamiento;
import parcial2.am.acn4av.martinezmartinez.sraiber.ui.activities.HomeActivity;
import parcial2.am.acn4av.martinezmartinez.sraiber.ui.viewmodels.AuthViewModel;

public class PerfilFragment extends DatosFragment {
    private static final Locale ES = new Locale("es", "AR");
    private final ExecutorService copia = Executors.newSingleThreadExecutor();
    private final Handler principal = new Handler(Looper.getMainLooper());


    private final ActivityResultLauncher<PickVisualMediaRequest> elegirFoto =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), this::guardarFoto);

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle state) {
        return inflater.inflate(R.layout.fragment_perfil, parent, false);
    }

    @Override public void onViewCreated(View view, Bundle state) {
        super.onViewCreated(view, state);
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
        if (usuario != null) {
            ((TextView) view.findViewById(R.id.perfil_email)).setText(usuario.getEmail());
            if (usuario.getMetadata() != null) {
                String fecha = DateFormat.getDateInstance(DateFormat.LONG, ES)
                        .format(new Date(usuario.getMetadata().getCreationTimestamp()));
                ((TextView) view.findViewById(R.id.perfil_miembro)).setText(getString(R.string.perfil_miembro_desde, fecha));
            }
        }
        view.findViewById(R.id.perfil_btn_elegir_foto).setOnClickListener(v -> elegirFoto.launch(
                new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build()));
        view.findViewById(R.id.perfil_btn_quitar_foto).setOnClickListener(v -> quitarFoto());
        view.findViewById(R.id.perfil_btn_historial).setOnClickListener(v -> {
            if (requireActivity() instanceof HomeActivity) ((HomeActivity) requireActivity()).irA(Navegacion.HISTORIAL);
        });
        view.findViewById(R.id.perfil_btn_password).setOnClickListener(v -> cambiarPassword());
        view.findViewById(R.id.perfil_btn_salir).setOnClickListener(v ->
                new MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_Marber_Dialog)
                        .setTitle(R.string.cerrar_sesion).setMessage(R.string.confirmar_cerrar_sesion)
                        .setNegativeButton(R.string.ejercicio_cancelar, null)
                        .setPositiveButton(R.string.confirmar_si, (d, w) -> Sesion.cerrar(requireActivity())).show());
        mostrarFoto();
        mostrar();
    }

    @Override protected void mostrar() {
        View vista = getView();
        if (vista == null || datos == null) return;
        TextView estado = vista.findViewById(R.id.perfil_estado);
        String error = datos.error.getValue();

        if (error != null && !error.isEmpty()) estado.setText(error);
        else if (!datos.listo) estado.setText(R.string.cargando_datos);
        estado.setVisibility((error != null && !error.isEmpty()) || !datos.listo ? View.VISIBLE : View.GONE);
        estado.setOnClickListener(v -> datos.reintentar(requireContext()));

        Calendar inicioMes = Calendar.getInstance();
        inicioMes.set(Calendar.DAY_OF_MONTH, 1);
        inicioMes.set(Calendar.HOUR_OF_DAY, 0); inicioMes.set(Calendar.MINUTE, 0);
        inicioMes.set(Calendar.SECOND, 0); inicioMes.set(Calendar.MILLISECOND, 0);
        int esteMes = 0, series = 0; long minutos = 0;

        Map<String, Integer> veces = new HashMap<>();
        for (Entrenamiento e : datos.historial) {
            if (e.fin >= inicioMes.getTimeInMillis()) esteMes++;
            series += e.seriesHechas();
            minutos += e.minutos();
            veces.merge(e.nombre, 1, Integer::sum);
        }
        texto(vista, R.id.perfil_total, String.valueOf(datos.historial.size()));
        texto(vista, R.id.perfil_mes, String.valueOf(esteMes));
        texto(vista, R.id.perfil_cant_rutinas, String.valueOf(datos.rutinas.size()));
        texto(vista, R.id.perfil_series, String.valueOf(series));
        texto(vista, R.id.perfil_minutos, String.valueOf(minutos));
        texto(vista, R.id.perfil_propios, String.valueOf(EjercicioRepository.getCatalogoCreadosUserCache().size()));

        String favorita = null; int maximo = 0;
        for (Map.Entry<String, Integer> par : veces.entrySet())
            if (par.getValue() > maximo) { favorita = par.getKey(); maximo = par.getValue(); }
        texto(vista, R.id.perfil_favorita, favorita == null ? getString(R.string.perfil_sin_datos)
                : getResources().getQuantityString(R.plurals.perfil_favorita_detalle, maximo, favorita, maximo));
        if (datos.historial.isEmpty()) texto(vista, R.id.perfil_ultimo, getString(R.string.perfil_sin_datos));
        else {

            Entrenamiento ultimo = datos.historial.get(0);
            texto(vista, R.id.perfil_ultimo, ultimo.nombre + " · "
                    + DateFormat.getDateInstance(DateFormat.MEDIUM, ES).format(new Date(ultimo.fin)));
        }
    }

    private static void texto(View vista, int id, String valor) { ((TextView) vista.findViewById(id)).setText(valor); }


    private File archivoFoto() {
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
        String uid = usuario == null ? "sin_sesion" : usuario.getUid();
        return new File(new File(requireContext().getFilesDir(), "perfil"), uid + ".jpg");
    }

    private void mostrarFoto() {
        View vista = getView();
        if (vista == null) return;
        ImageView foto = vista.findViewById(R.id.perfil_foto);
        File archivo = archivoFoto();
        vista.findViewById(R.id.perfil_btn_quitar_foto).setVisibility(archivo.exists() ? View.VISIBLE : View.GONE);
        if (!archivo.exists()) { Glide.with(this).clear(foto); foto.setImageResource(R.drawable.ic_nav_perfil); return; }

        Glide.with(this).load(archivo).signature(new ObjectKey(archivo.lastModified()))
                .circleCrop().placeholder(R.drawable.ic_nav_perfil).into(foto);
    }


    public void guardarFoto(Uri uri) {
        if (uri == null) return;
        File destino = archivoFoto();
        android.content.ContentResolver resolver = requireContext().getContentResolver();

        copia.execute(() -> {
            boolean ok;
            File temporal = new File(destino.getParentFile(), destino.getName() + ".tmp");
            try {
                if (!destino.getParentFile().exists() && !destino.getParentFile().mkdirs()) throw new java.io.IOException("Sin carpeta");
                try (InputStream entrada = resolver.openInputStream(uri); OutputStream salida = new FileOutputStream(temporal)) {
                    if (entrada == null) throw new java.io.IOException("Sin datos");
                    byte[] buffer = new byte[8192]; int leidos;
                    while ((leidos = entrada.read(buffer)) != -1) salida.write(buffer, 0, leidos);
                }
                ok = temporal.renameTo(destino) || (destino.delete() && temporal.renameTo(destino));
            } catch (Exception e) { ok = false; }
            if (!ok) temporal.delete();
            boolean resultado = ok;
            principal.post(() -> {
                if (!isAdded() || getView() == null) return;
                mostrarFoto();
                Snackbar.make(requireView(), resultado ? R.string.perfil_foto_guardada : R.string.perfil_foto_error,
                        Snackbar.LENGTH_SHORT).show();
            });
        });
    }

    private void quitarFoto() {
        new MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_Marber_Dialog)
                .setTitle(R.string.perfil_quitar_foto)
                .setNegativeButton(R.string.ejercicio_cancelar, null)
                .setPositiveButton(R.string.confirmar_si, (d, w) -> {
                    if (archivoFoto().delete()) {
                        mostrarFoto();
                        Snackbar.make(requireView(), R.string.perfil_foto_quitada, Snackbar.LENGTH_SHORT).show();
                    }
                }).show();
    }

    private void cambiarPassword() {
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
        if (usuario == null || usuario.getEmail() == null) return;
        String email = usuario.getEmail();

        new MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_Marber_Dialog)
                .setTitle(R.string.perfil_cambiar_password)
                .setMessage(getString(R.string.auth_reset_confirmacion, email))
                .setNegativeButton(R.string.ejercicio_cancelar, null)
                .setPositiveButton(R.string.auth_enviar, (d, w) -> {
                    FirebaseAuth.getInstance().setLanguageCode("es");
                    FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnCompleteListener(tarea -> {
                        if (!isAdded() || getView() == null) return;
                        Snackbar.make(requireView(), tarea.isSuccessful() ? R.string.auth_reset_enviado
                                : AuthViewModel.error(tarea.getException()), Snackbar.LENGTH_LONG).show();
                    });
                }).show();
    }

    @Override public void onDestroy() { super.onDestroy(); copia.shutdown(); }
}
