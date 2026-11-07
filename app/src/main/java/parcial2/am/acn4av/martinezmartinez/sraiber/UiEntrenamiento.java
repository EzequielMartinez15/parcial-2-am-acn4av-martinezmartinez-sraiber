package parcial2.am.acn4av.martinezmartinez.sraiber;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.widget.*;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.*;

public final class UiEntrenamiento {
    private UiEntrenamiento() { }
    public static int dp(Context c, int n) { return Math.round(n * c.getResources().getDisplayMetrics().density); }
    public static LinearLayout columna(Context c) {
        LinearLayout l = new LinearLayout(c); l.setOrientation(LinearLayout.VERTICAL); return l;
    }
    public static TextView texto(LinearLayout padre, String texto, int size, boolean fuerte) {
        Context c = padre.getContext(); TextView t = new TextView(c); t.setText(texto); t.setTextSize(size);
        t.setTextColor(ContextCompat.getColor(c, fuerte ? R.color.marber_text_high : R.color.marber_text_medium));
        if (fuerte) t.setTypeface(t.getTypeface(), Typeface.BOLD);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2); p.bottomMargin = dp(c, 12);
        padre.addView(t, p); return t;
    }
    public static LinearLayout tarjeta(LinearLayout padre) {
        Context c = padre.getContext(); MaterialCardView card = new MaterialCardView(c);
        card.setCardBackgroundColor(ContextCompat.getColor(c, R.color.marber_surface));
        card.setRadius(dp(c, 20)); card.setStrokeWidth(dp(c, 1));
        card.setStrokeColor(ContextCompat.getColor(c, R.color.marber_border)); card.setCardElevation(0);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2); p.bottomMargin = dp(c, 16);
        padre.addView(card, p); LinearLayout contenido = columna(c);
        int padding = dp(c, 18); contenido.setPadding(padding, padding, padding, padding);
        card.addView(contenido); return contenido;
    }
    public static MaterialButton boton(LinearLayout padre, int texto, boolean primario, View.OnClickListener accion) {
        Context c = padre.getContext();
        MaterialButton b = new MaterialButton(new androidx.appcompat.view.ContextThemeWrapper(c,
                primario ? R.style.Widget_Marber_Button_Primary : R.style.Widget_Marber_Button_Quiet));
        b.setText(texto); b.setAllCaps(false); b.setMinHeight(dp(c, 48)); b.setCornerRadius(dp(c, 14));
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(primario
                ? ContextCompat.getColor(c, R.color.marber_primary) : android.graphics.Color.TRANSPARENT));
        b.setTextColor(ContextCompat.getColor(c, primario ? R.color.marber_text_high : R.color.marber_primary_light));
        padre.addView(b, new LinearLayout.LayoutParams(-1, -2)); b.setOnClickListener(accion); return b;
    }
    public static TextInputEditText campo(LinearLayout padre, int hint, String valor, int tipo) {
        Context c = padre.getContext();
        TextInputLayout layout = new TextInputLayout(new androidx.appcompat.view.ContextThemeWrapper(c,
                R.style.Widget_Marber_TextInputLayout));
        layout.setHint(hint); layout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        layout.setHintAnimationEnabled(false);
        layout.setExpandedHintEnabled(false);
        TextInputEditText input = new TextInputEditText(layout.getContext()); input.setInputType(tipo);
        input.setTextColor(ContextCompat.getColor(c, R.color.marber_text_high)); input.setText(valor);
        input.setMinHeight(dp(c, 56)); input.setSaveEnabled(false);
        layout.addView(input, new LinearLayout.LayoutParams(-1, -2));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2); p.bottomMargin = dp(c, 12);
        padre.addView(layout, p); return input;
    }
    public static void foto(LinearLayout padre, String nombre) {
        ImageView foto = new ImageView(padre.getContext());
        foto.setImageResource(parcial2.am.acn4av.martinezmartinez.sraiber.Repository.EjercicioRepository.imagenPorNombre(padre.getContext(), nombre));
        foto.setScaleType(ImageView.ScaleType.CENTER_CROP);
        foto.setContentDescription(padre.getContext().getString(R.string.foto_de_ejercicio, nombre));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(padre.getContext(), 150));
        p.bottomMargin = dp(padre.getContext(), 14); padre.addView(foto, p);
    }
}
