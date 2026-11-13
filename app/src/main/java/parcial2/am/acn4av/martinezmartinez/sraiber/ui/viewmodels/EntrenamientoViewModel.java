package parcial2.am.acn4av.martinezmartinez.sraiber.ui.viewmodels;

import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import parcial2.am.acn4av.martinezmartinez.sraiber.R;
import parcial2.am.acn4av.martinezmartinez.sraiber.Repository.DatosUsuario;
import parcial2.am.acn4av.martinezmartinez.sraiber.model.Entrenamiento;

public class EntrenamientoViewModel extends ViewModel {
    public Entrenamiento entrenamiento;
    public boolean ocupado, terminado;
    public final MutableLiveData<Integer> cambios = new MutableLiveData<>(0);
    public int mensaje;
    private DatosUsuario datos;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean modificado;
    private final Runnable guardar = this::persistir;

    public void iniciar(DatosUsuario datos, Entrenamiento e) {
        if (entrenamiento != null) return;
        this.datos = datos; entrenamiento = Entrenamiento.leer(e.datos());
    }
    public void modificar() {
        if (ocupado || terminado) return;
        modificado = true; handler.removeCallbacks(guardar); handler.postDelayed(guardar, 700);
    }
    public void persistir() {
        handler.removeCallbacks(guardar);
        if (!modificado || ocupado || terminado || entrenamiento == null) return;
        modificado = false;
        try { datos.guardarBorrador(entrenamiento); }
        catch (IllegalStateException e) { mensaje = R.string.datos_error_red; notificar(); }
    }
    public void finalizar() {
        if (ocupado || terminado) return;
        if (!entrenamiento.completo()) { mensaje = R.string.entrenamiento_incompleto; notificar(); return; }
        handler.removeCallbacks(guardar); ocupado = true; modificado = false; notificar();

        datos.finalizar(entrenamiento).addOnCompleteListener(tarea -> {
            ocupado = false;
            if (tarea.isSuccessful()) { terminado = true; mensaje = R.string.entrenamiento_guardado; }
            else { entrenamiento.fin = 0; mensaje = R.string.datos_error_red; }
            notificar();
        });
    }
    public void descartar() {
        if (ocupado || terminado) return;
        handler.removeCallbacks(guardar); ocupado = true; modificado = false; notificar();
        datos.descartar().addOnCompleteListener(tarea -> {
            ocupado = false; terminado = tarea.isSuccessful();
            if (!terminado) mensaje = R.string.datos_error_red;
            notificar();
        });
    }
    private void notificar() { cambios.setValue(cambios.getValue() + 1); }
    @Override protected void onCleared() { persistir(); handler.removeCallbacks(guardar); }
}
