package parcial2.am.acn4av.martinezmartinez.sraiber;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.widget.LinearLayout;
import androidx.core.content.ContextCompat;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public final class Navegacion {
    public static final int EJERCICIOS = 101, RUTINAS = 102, HOME = 103, HISTORIAL = 104, PERFIL = 105;
    private Navegacion() { }
    public static BottomNavigationView instalar(Activity activity, LinearLayout root, int seleccion) {
        BottomNavigationView nav = new BottomNavigationView(activity);
        nav.setLabelVisibilityMode(com.google.android.material.navigation.NavigationBarView.LABEL_VISIBILITY_LABELED);
        nav.setItemHorizontalTranslationEnabled(false);
        nav.setBackgroundColor(ContextCompat.getColor(activity, R.color.marber_surface));
        ColorStateList colores = new ColorStateList(new int[][]{new int[]{android.R.attr.state_checked}, new int[]{}},
                new int[]{ContextCompat.getColor(activity, R.color.marber_primary_light),
                        ContextCompat.getColor(activity, R.color.marber_text_medium)});
        nav.setItemTextColor(colores); nav.setItemIconTintList(colores);
        nav.setItemActiveIndicatorColor(ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.marber_primary_dark)));
        nav.getMenu().add(0, EJERCICIOS, 0, R.string.nav_ejercicios).setIcon(R.drawable.ic_marber_dumbbell);
        nav.getMenu().add(0, RUTINAS, 1, R.string.nav_rutinas).setIcon(R.drawable.ic_nav_rutinas);
        nav.getMenu().add(0, HOME, 2, R.string.nav_home).setIcon(R.drawable.ic_nav_home);
        nav.getMenu().add(0, HISTORIAL, 3, R.string.nav_historial).setIcon(R.drawable.ic_nav_historial);
        nav.getMenu().add(0, PERFIL, 4, R.string.nav_perfil).setIcon(R.drawable.ic_nav_perfil);
        nav.setSelectedItemId(seleccion);
        root.addView(nav, new LinearLayout.LayoutParams(-1, -2));
        return nav;
    }
}
