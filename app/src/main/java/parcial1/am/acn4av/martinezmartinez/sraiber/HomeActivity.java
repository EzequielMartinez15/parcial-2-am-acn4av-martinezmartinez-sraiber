package parcial1.am.acn4av.martinezmartinez.sraiber;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

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
    }
}
