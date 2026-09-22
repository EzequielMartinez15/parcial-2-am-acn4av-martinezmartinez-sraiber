package parcial1.am.acn4av.martinezmartinez.sraiber;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.material.button.MaterialButton;

public class HomeActivity extends AppCompatActivity {

    private View home;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(getWindow(), home);

        getWindow().setStatusBarColor(
                ContextCompat.getColor(this, R.color.marber_background)
        );
        controller.setAppearanceLightStatusBars(false);
        controller.setAppearanceLightNavigationBars(false);

        //declaración de las variables de los botones de la activity
        MaterialButton logoutButton = findViewById(R.id.btn_logout);




        //Evento para simular logout, como no hay una autenticación simplemente vuelve a la activity de login.
        //Abajo del evento del logout puse un get que obtiene cuando el user toca el botón
        //de ir para atrás en el panel de navegación y que eso le de el alert de cerrar sesion.
        logoutButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            popUpCerrarSesion(intent);
        });
        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                        popUpCerrarSesion(intent);
                    }
                });
    }

    protected void popUpCerrarSesion(Intent intent){
        new AlertDialog.Builder(HomeActivity.this)
                .setTitle("Cerrar sesión")
                .setMessage("¿Seguro que quieres salir de tu sesión?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
