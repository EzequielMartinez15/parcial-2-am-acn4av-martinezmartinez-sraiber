package parcial2.am.acn4av.martinezmartinez.sraiber;

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
import androidx.lifecycle.ViewModelProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

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
    private TextInputEditText confirmInput;
    private TextInputLayout confirmLayout;
    private MaterialButton loginButton, forgotPasswordButton, createAccountButton;
    private AuthViewModel auth;
    private boolean registro;
    private boolean navegando;

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

        // Enlacé el formulario con la misma operación de Firebase aunque gire la pantalla.
        emailLayout = findViewById(R.id.email_layout);
        passwordLayout = findViewById(R.id.password_layout);
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        loginButton = findViewById(R.id.login_button);
        forgotPasswordButton = findViewById(R.id.forgot_password_button);
        createAccountButton = findViewById(R.id.create_account_button);
        confirmInput = findViewById(R.id.confirm_password_input);
        confirmLayout = findViewById(R.id.confirm_password_layout);
        auth = new ViewModelProvider(this).get(AuthViewModel.class);
        registro = savedInstanceState != null && savedInstanceState.getBoolean("registro");

        configureField(emailLayout, emailInput);
        configureField(passwordLayout, passwordInput);
        configureField(confirmLayout, confirmInput);

        loginButton.setOnClickListener(view -> submitLogin());
        forgotPasswordButton.setOnClickListener(view -> recuperarPassword());
        createAccountButton.setOnClickListener(view -> {
            registro = !registro;
            passwordInput.setText("");
            confirmInput.setText("");
            emailLayout.setError(null);
            passwordLayout.setError(null);
            confirmLayout.setError(null);
            actualizarFormulario();
        });
        auth.ocupado.observe(this, ocupado -> actualizarFormulario());
        auth.mensaje.observe(this, mensaje -> {
            if (mensaje != null && mensaje != 0) {
                Snackbar.make(main, mensaje, Snackbar.LENGTH_LONG).show();
                auth.mensaje.setValue(0);
            }
        });
        auth.autenticado.observe(this, autenticado -> {
            if (Boolean.TRUE.equals(autenticado)) abrirHome();
        });
        actualizarFormulario();

        // Si el usuario aprieta "Listo" en el teclado (en el campo de contraseña), quiero
        // que dispare el mismo submitLogin() que el botón, así no lo obligo a tocar la pantalla.
        passwordInput.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submitLogin();
                return true;
            }
            return false;
        });
        confirmInput.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submitLogin();
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) abrirHome();
    }

    @Override
    protected void onSaveInstanceState(Bundle estado) {
        estado.putBoolean("registro", registro);
        super.onSaveInstanceState(estado);
    }

    private void actualizarFormulario() {
        boolean ocupado = auth.estaOcupado();
        loginButton.setEnabled(!ocupado);
        forgotPasswordButton.setEnabled(!ocupado);
        createAccountButton.setEnabled(!ocupado);
        emailInput.setEnabled(!ocupado);
        passwordInput.setEnabled(!ocupado);
        confirmInput.setEnabled(!ocupado);
        confirmLayout.setVisibility(registro ? View.VISIBLE : View.GONE);
        findViewById(R.id.auth_registro_ayuda).setVisibility(registro ? View.VISIBLE : View.GONE);
        findViewById(R.id.auth_progress).setVisibility(ocupado ? View.VISIBLE : View.GONE);
        forgotPasswordButton.setVisibility(registro ? View.GONE : View.VISIBLE);
        loginButton.setText(ocupado ? R.string.auth_esperando
                : registro ? R.string.txt_login_create_account : R.string.txt_login_submit);
        createAccountButton.setText(registro ? R.string.auth_volver_login : R.string.txt_login_create_account);
    }

    private void abrirHome() {
        if (navegando || FirebaseAuth.getInstance().getCurrentUser() == null) return;
        navegando = true;
        passwordInput.setText("");
        confirmInput.setText("");
        Intent intent = new Intent(this, HomeActivity.class);
        Sesion.agregarUsuario(intent);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private boolean validarEmail() {
        String email = emailInput.getText() == null ? "" : emailInput.getText().toString().trim();
        emailLayout.setError(null);
        if (email.isEmpty()) emailLayout.setError(getString(R.string.error_email_required));
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
            emailLayout.setError(getString(R.string.error_email_invalid));
        else return true;
        emailInput.requestFocus();
        return false;
    }

    private void recuperarPassword() {
        if (auth.estaOcupado() || !validarEmail()) return;
        String email = emailInput.getText().toString().trim();
        // Pedí confirmar el destinatario antes de enviar el enlace de recuperación.
        new MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_Marber_Dialog)
                .setTitle(R.string.auth_reset_titulo)
                .setMessage(getString(R.string.auth_reset_confirmacion, email))
                .setNegativeButton(R.string.ejercicio_cancelar, null)
                .setPositiveButton(R.string.auth_enviar, (dialog, which) -> auth.recuperar(email))
                .show();
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
        if (auth.estaOcupado() || navegando) return;
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

        confirmLayout.setError(null);
        if (registro && (confirmInput.getText() == null || passwordText == null
                || !passwordText.toString().equals(confirmInput.getText().toString()))) {
            confirmLayout.setError(getString(R.string.auth_password_distinta));
            valid = false;
        }
        // Abrí la Home únicamente después de que Firebase confirmó el ingreso o el registro.
        if (valid) auth.ingresar(emailText.toString().trim(), passwordText.toString(), registro);
    }
}
