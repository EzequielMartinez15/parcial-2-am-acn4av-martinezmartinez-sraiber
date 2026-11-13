package parcial2.am.acn4av.martinezmartinez.sraiber;

import android.view.View;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

import parcial2.am.acn4av.martinezmartinez.sraiber.ui.activities.LoginActivity;

@RunWith(AndroidJUnit4.class)
public class LoginFormularioTest {
    @Before public void sinSesionExistente() {
        // Evité cerrar una sesión del usuario y no creé cuentas reales durante las pruebas.
        assumeTrue(FirebaseAuth.getInstance().getCurrentUser() == null);
    }

    @Test public void camposVaciosNoPermitenIngresar() {
        try (ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class)) {
            scenario.onActivity(activity -> {
                activity.findViewById(R.id.login_button).performClick();
                assertNotNull(((TextInputLayout) activity.findViewById(R.id.email_layout)).getError());
                assertNotNull(((TextInputLayout) activity.findViewById(R.id.password_layout)).getError());
                assertNull(FirebaseAuth.getInstance().getCurrentUser());
            });
        }
    }

    @Test public void registroRechazaConfirmacionDistintaYConservaModo() {
        try (ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class)) {
            scenario.onActivity(activity -> {
                activity.findViewById(R.id.create_account_button).performClick();
                ((TextInputEditText) activity.findViewById(R.id.email_input)).setText("prueba@example.com");
                ((TextInputEditText) activity.findViewById(R.id.password_input)).setText("prueba123");
                ((TextInputEditText) activity.findViewById(R.id.confirm_password_input)).setText("distinta123");
                activity.findViewById(R.id.login_button).performClick();
                assertNotNull(((TextInputLayout) activity.findViewById(R.id.confirm_password_layout)).getError());
                assertNull(FirebaseAuth.getInstance().getCurrentUser());
            });
            scenario.recreate();
            scenario.onActivity(activity -> {
                assertEquals(View.VISIBLE, activity.findViewById(R.id.confirm_password_layout).getVisibility());
                assertEquals(View.GONE, activity.findViewById(R.id.forgot_password_button).getVisibility());
                activity.findViewById(R.id.create_account_button).performClick();
                assertEquals(View.GONE, activity.findViewById(R.id.confirm_password_layout).getVisibility());
                assertEquals("", ((TextInputEditText) activity.findViewById(R.id.password_input)).getText().toString());
            });
        }
    }

    @Test public void recuperarRequiereEmailValido() {
        try (ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class)) {
            scenario.onActivity(activity -> {
                ((TextInputEditText) activity.findViewById(R.id.email_input)).setText("invalido");
                activity.findViewById(R.id.forgot_password_button).performClick();
                assertNotNull(((TextInputLayout) activity.findViewById(R.id.email_layout)).getError());
                assertEquals(View.GONE, activity.findViewById(R.id.auth_progress).getVisibility());
            });
        }
    }
}
