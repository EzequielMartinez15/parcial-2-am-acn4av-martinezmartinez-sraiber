package parcial2.am.acn4av.martinezmartinez.sraiber.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.text.InputType;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.*;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import java.util.*;
import org.json.*;

import parcial2.am.acn4av.martinezmartinez.sraiber.FotosEjercicio;
import parcial2.am.acn4av.martinezmartinez.sraiber.R;
import parcial2.am.acn4av.martinezmartinez.sraiber.Repository.*;
import parcial2.am.acn4av.martinezmartinez.sraiber.ui.activities.UiEntrenamiento;
import parcial2.am.acn4av.martinezmartinez.sraiber.model.EjercicioCatalogo;

public class ImportarEjerciciosDialog extends DialogFragment {
    private RequestQueue cola;
    private TextInputEditText url;
    private TextView resumen;
    private List<EjercicioCatalogo> lista;
    private String revisada = "";
    @NonNull @Override public Dialog onCreateDialog(Bundle state) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_Marber_Dialog);
        LinearLayout contenido = UiEntrenamiento.columna(builder.getContext());
        int margen = UiEntrenamiento.dp(requireContext(), 20); contenido.setPadding(margen, margen, margen, margen);
        ScrollView scroll = new ScrollView(builder.getContext()); scroll.addView(contenido);
        UiEntrenamiento.texto(contenido, getString(R.string.json_ayuda), 14, false);
        url = UiEntrenamiento.campo(contenido, R.string.json_url,
                state == null ? getString(R.string.json_url_ejemplo) : state.getString("url", ""),
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        resumen = UiEntrenamiento.texto(contenido, "", 14, false);
        AlertDialog dialog = builder.setTitle(R.string.importar_json).setView(scroll)
                .setNegativeButton(R.string.ejercicio_cancelar, null).setPositiveButton(R.string.json_revisar, null).create();
        dialog.setOnShowListener(v -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(b -> {
            String enlace = url.getText().toString().trim();
            if (!enlace.startsWith("https://") || !FotosEjercicio.valida(enlace)) { url.setError(getString(R.string.json_error)); return; }
            if (lista != null && enlace.equals(revisada)) {
                if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                b.setEnabled(false); setCancelable(false); dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);
                EjercicioRepository.importar(uid, lista).addOnCompleteListener(t -> {
                    if (!isAdded() || getDialog() != dialog) return;
                    b.setEnabled(true); setCancelable(true); dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(true);
                    if (t.isSuccessful()) {
                        DatosUsuario datos = DatosUsuario.obtener(requireContext());
                        datos.cambios.setValue(datos.cambios.getValue() + 1);
                        Toast.makeText(requireContext(), R.string.json_importado, Toast.LENGTH_LONG).show(); dismiss();
                    } else url.setError(getString(R.string.operacion_error));
                });
                return;
            }
            b.setEnabled(false);
            cola = Volley.newRequestQueue(requireContext().getApplicationContext());
            cola.add(new JsonArrayRequest(enlace, respuesta -> {
                if (!isAdded() || getDialog() != dialog) return;
                b.setEnabled(true);
                try {
                    lista = leer(respuesta); revisada = enlace;
                    StringBuilder nombres = new StringBuilder();
                    for (EjercicioCatalogo e : lista) nombres.append(e.getNombre()).append(" - ").append(e.getGrupoMuscular()).append("\n");
                    resumen.setText(nombres);
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setText(getString(R.string.json_confirmar, lista.size()));
                } catch (Exception e) { lista = null; url.setError(getString(R.string.json_error)); }
            }, error -> {
                if (!isAdded() || getDialog() != dialog) return;
                b.setEnabled(true); url.setError(getString(R.string.json_error));
            }));
        }));
        return dialog;
    }
    public static List<EjercicioCatalogo> leer(JSONArray json) throws JSONException {
        if (json.length() == 0 || json.length() > 50) throw new JSONException("Cantidad invalida");
        List<EjercicioCatalogo> salida = new ArrayList<>();
        for (int i = 0; i < json.length(); i++) {
            JSONObject dato = json.getJSONObject(i);
            String nombre = dato.getString("nombre").trim(), grupo = dato.getString("grupoMuscular").trim();
            String foto = dato.getString("nombreFoto").trim();
            if (nombre.isEmpty() || nombre.length() > 100 || grupo.isEmpty() || grupo.length() > 40 || !FotosEjercicio.valida(foto))
                throw new JSONException("Ejercicio invalido");
            EjercicioCatalogo e = new EjercicioCatalogo();
            e.setId("importado_" + UUID.nameUUIDFromBytes((nombre + "|" + grupo + "|" + foto).getBytes(java.nio.charset.StandardCharsets.UTF_8)));
            e.setNombre(nombre); e.setGrupoMuscular(grupo); e.setNombreFoto(foto); salida.add(e);
        }
        return salida;
    }
    @Override public void onSaveInstanceState(@NonNull Bundle out) { out.putString("url", url.getText().toString()); super.onSaveInstanceState(out); }
    @Override public void onDestroyView() { if (cola != null) { cola.cancelAll(r -> true); cola.stop(); } super.onDestroyView(); }
}
