package parcial2.am.acn4av.martinezmartinez.sraiber;

public class PendienteFragment extends DatosFragment {
    @Override protected void mostrar() {
        contenido.removeAllViews();
        int seccion = requireArguments().getInt("seccion");
        UiEntrenamiento.texto(contenido, getString(seccion == Navegacion.PERFIL
                ? R.string.nav_perfil : R.string.nav_ejercicios), 30, true);
        UiEntrenamiento.texto(contenido, getString(R.string.seccion_proximamente), 16, false);
    }
}
