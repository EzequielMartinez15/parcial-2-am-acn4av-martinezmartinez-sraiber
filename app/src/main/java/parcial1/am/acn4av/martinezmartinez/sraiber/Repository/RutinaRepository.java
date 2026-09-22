package parcial1.am.acn4av.martinezmartinez.sraiber.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.text.ParseException;
import java.text.SimpleDateFormat;


import parcial1.am.acn4av.martinezmartinez.sraiber.R;
import parcial1.am.acn4av.martinezmartinez.sraiber.model.Ejercicio;
import parcial1.am.acn4av.martinezmartinez.sraiber.model.Rutina;

public class RutinaRepository {
    public static List<Rutina> getRutinas() {

        List<Rutina> rutinas = new ArrayList<>();

        try{
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

            rutinas.add(
                    new Rutina(
                            "Push Day",
                            Arrays.asList(
                                    new Ejercicio("Press Banca", 4, 10, R.drawable.blue),
                                    new Ejercicio("Press Inclinado", 4, 12, R.drawable.blue),
                                    new Ejercicio("Aperturas", 3, 15, R.drawable.blue),
                                    new Ejercicio("Press Militar", 4, 10, R.drawable.blue),
                                    new Ejercicio("Elevaciones Laterales", 3, 15, R.drawable.blue),
                                    new Ejercicio("Fondos", 3, 12, R.drawable.blue)
                            ),
                            8,
                            formatter.parse("15/09/2026")
                    )
            );

            rutinas.add(
                    new Rutina(
                            "Pull Day",
                            Arrays.asList(
                                    new Ejercicio("Dominadas", 4, 8, R.drawable.blue),
                                    new Ejercicio("Jalon al Pecho", 4, 12, R.drawable.blue),
                                    new Ejercicio("Remo con Barra", 4, 10, R.drawable.blue),
                                    new Ejercicio("Face Pull", 3, 15, R.drawable.blue),
                                    new Ejercicio("Curl de Biceps", 4, 12, R.drawable.blue),
                                    new Ejercicio("Curl Martillo", 3, 12, R.drawable.blue)
                            ),
                            6,
                            formatter.parse("15/09/2026")
                    )
            );

            rutinas.add(
                    new Rutina(
                            "Leg Day",
                            Arrays.asList(
                                    new Ejercicio("Sentadilla", 4, 10, R.drawable.blue),
                                    new Ejercicio("Prensa", 4, 12, R.drawable.blue),
                                    new Ejercicio("Peso Muerto Rumano", 4, 10, R.drawable.blue),
                                    new Ejercicio("Extensiones", 3, 15, R.drawable.blue),
                                    new Ejercicio("Curl Femoral", 3, 15, R.drawable.blue),
                                    new Ejercicio("Gemelos", 4, 20, R.drawable.blue)
                            ),
                            5,
                            formatter.parse("15/09/2026")
                    )
            );
        }catch (ParseException e) {
            e.printStackTrace();
        }


        return rutinas;
    }
}
