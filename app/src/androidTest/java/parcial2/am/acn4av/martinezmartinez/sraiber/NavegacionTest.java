package parcial2.am.acn4av.martinezmartinez.sraiber;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import org.junit.*;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class NavegacionTest {
    @Test public void conservaBarraPestanasYRotacion() {
        Assume.assumeNotNull(FirebaseAuth.getInstance().getCurrentUser());
        try (ActivityScenario<HomeActivity> scenario = ActivityScenario.launch(HomeActivity.class)) {
            scenario.onActivity(a -> {
                BottomNavigationView barra = a.findViewById(R.id.barra_principal);
                Fragment inicio = a.getSupportFragmentManager().getPrimaryNavigationFragment();
                assertTrue(inicio instanceof InicioFragment);
                for (int id : new int[]{Navegacion.RUTINAS, Navegacion.HISTORIAL,
                        Navegacion.EJERCICIOS, Navegacion.PERFIL, Navegacion.HOME}) {
                    barra.setSelectedItemId(id);
                    assertSame(barra, a.findViewById(R.id.barra_principal));
                    assertFalse(a.isFinishing());
                    long visibles = a.getSupportFragmentManager().getFragments().stream()
                            .filter(f -> !f.isHidden()).count();
                    assertEquals(1, visibles);
                }
                assertSame(inicio, a.getSupportFragmentManager().getPrimaryNavigationFragment());
                barra.setSelectedItemId(Navegacion.HISTORIAL);
            });
            scenario.recreate();
            scenario.onActivity(a -> {
                BottomNavigationView barra = a.findViewById(R.id.barra_principal);
                assertEquals(Navegacion.HISTORIAL, barra.getSelectedItemId());
                assertTrue(a.getSupportFragmentManager().getPrimaryNavigationFragment() instanceof HistorialFragment);
                a.getOnBackPressedDispatcher().onBackPressed();
                assertEquals(Navegacion.HOME, barra.getSelectedItemId());
            });
        }
    }
}
