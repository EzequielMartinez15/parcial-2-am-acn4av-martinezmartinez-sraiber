package parcial1.am.acn4av.martinezmartinez.sraiber;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private static final int MIN_PASSWORD_LENGTH = 6;

    private View main;
    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Acá agarro el padding de la barra de estado/navegación y le sumo el alto del
        // teclado cuando está abierto, para que no me tape los campos de abajo.
        main = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets keyboard = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right,
                    Math.max(systemBars.bottom, keyboard.bottom));
            return insets;
        });
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(getWindow(), main);
        getWindow().setStatusBarColor(
                ContextCompat.getColor(this, R.color.marber_background)
        );
        controller.setAppearanceLightStatusBars(false);
        controller.setAppearanceLightNavigationBars(false);
        ViewCompat.requestApplyInsets(main);

        // Enlazo acá todas las vistas del formulario. Como todavía es una sola pantalla,
        // dejotodo dentro de este mismo onCreate; si sumamos más pantallas lo separamos.
        emailLayout = findViewById(R.id.email_layout);
        passwordLayout = findViewById(R.id.password_layout);
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        MaterialButton loginButton = findViewById(R.id.login_button);
        MaterialButton forgotPasswordButton = findViewById(R.id.forgot_password_button);
        MaterialButton createAccountButton = findViewById(R.id.create_account_button);

        configureField(emailLayout, emailInput);
        configureField(passwordLayout, passwordInput);

        loginButton.setOnClickListener(view -> submitLogin());
        forgotPasswordButton.setOnClickListener(view -> Snackbar.make(main,
                R.string.forgot_password_coming_soon, Snackbar.LENGTH_SHORT).show());
        createAccountButton.setOnClickListener(view -> Snackbar.make(main,
                R.string.create_account_coming_soon, Snackbar.LENGTH_SHORT).show());

        // Si el usuario aprieta "Listo" en el teclado (en el campo de contraseña), quiero
        // que dispare el mismo submitLogin() que el botón, así no lo obligo a tocar la pantalla.
        passwordInput.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submitLogin();
                return true;
            }
            return false;
        });
    }

    private void configureField(TextInputLayout layout, TextInputEditText input) {
        // Le pinto el borde a mano en vez de dejar el color que trae Material por defecto,
        // porque si no, en algunos celulares me toma el tema claro del sistema y rompe la paleta.
        int restingColor = ContextCompat.getColor(this, R.color.marber_surface_alt);
        int focusedColor = ContextCompat.getColor(this, R.color.marber_primary);
        layout.setBoxStrokeColorStateList(new ColorStateList(
                new int[][] {
                        new int[] {-android.R.attr.state_enabled},
                        new int[] {android.R.attr.state_focused},
                        new int[] {android.R.attr.state_hovered},
                        new int[] {}
                },
                new int[] {restingColor, focusedColor, restingColor, restingColor}));

        // Le agrego este listener para que, apenas el usuario vuelve a escribir en el campo,
        // le saque el mensaje de error a ESE campo (el otro campo no se toca).
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence text, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
                layout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable text) {
            }
        });
    }

    private void submitLogin() {
        WindowCompat.getInsetsController(getWindow(), main)
                .hide(WindowInsetsCompat.Type.ime());

        Editable emailText = emailInput.getText();
        Editable passwordText = passwordInput.getText();
        emailLayout.setError(null);
        passwordLayout.setError(null);
        boolean valid = true;

        if (emailText == null || emailText.toString().trim().isEmpty()) {
            emailLayout.setError(getString(R.string.error_email_required));
            valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailText.toString().trim()).matches()) {
            emailLayout.setError(getString(R.string.error_email_invalid));
            valid = false;
        }

        if (passwordText == null || passwordText.length() == 0) {
            passwordLayout.setError(getString(R.string.error_password_required));
            valid = false;
        } else if (passwordText.length() < MIN_PASSWORD_LENGTH) {
            passwordLayout.setError(getString(R.string.error_password_short));
            valid = false;
        }

        if (valid) {
            Snackbar.make(main, R.string.login_welcome, Snackbar.LENGTH_SHORT).show();
            String email = emailText.toString().trim();
            String password = passwordText.toString();
            //Cree el intent para que si es valido el loghin se vaya a la home_activity
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);

        }
    }
}
