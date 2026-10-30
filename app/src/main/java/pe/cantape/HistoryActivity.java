package pe.cantape;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class HistoryActivity extends Activity {
    private final Locale locale = new Locale("es", "PE");
    private final SimpleDateFormat dayKey = new SimpleDateFormat("yyyyMMdd", locale);
    private final Map<String, long[]> days = new HashMap<>();
    private final Calendar month = Calendar.getInstance();
    private boolean expanded;
    private Button toggle;
    private PaymentStore payments;
    private List<PaymentStore.Payment> rows;
    private String selected;
    private LinearLayout grid;
    private TextView monthTitle;
    private TextView emptyDay;
    private ListView list;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        payments = new PaymentStore(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int spacing = Math.round(20 * getResources().getDisplayMetrics().density);
        layout.setPadding(spacing, spacing, spacing, spacing);
        layout.setOnApplyWindowInsetsListener((view, insets) -> {
            view.setPadding(spacing, spacing + insets.getSystemWindowInsetTop(),
                    spacing, spacing + insets.getSystemWindowInsetBottom());
            return insets;
        });
        layout.setBackgroundColor(Palette.BACKGROUND);
        setContentView(layout);

        layout.addView(label("Yapes detectados", 27, true));
        layout.addView(label(Money.format(payments.totalCents()) + " en " + payments.count() + " avisos", 20, true));
        layout.addView(label("Este historial se guarda solo en este teléfono. No sustituye los movimientos oficiales de Yape.", 14, false));

        Button clear = new Button(this);
        clear.setText("Borrar historial");
        clear.setAllCaps(false);
        clear.setTextColor(Palette.SLATE);
        clear.setEnabled(payments.count() > 0);
        clear.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Borrar historial")
                .setMessage("Se eliminarán todos los Yapes detectados y el total volverá a S/ 0.00. No se puede deshacer. Los movimientos en Yape no se ven afectados.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Borrar todo", (dialog, which) -> {
                    payments.clear();
                    recreate();
                })
                .show());
        layout.addView(clear);

        rows = payments.all();
        if (rows.isEmpty()) {
            layout.addView(label("Aún no hay Yapes detectados.", 17, false));
            return;
        }
        for (PaymentStore.Payment payment : rows) {
            long[] day = days.get(dayKey.format(new Date(payment.at)));
            if (day == null) days.put(dayKey.format(new Date(payment.at)), day = new long[2]);
            day[0] += payment.cents;
            day[1]++;
        }
        selected = dayKey.format(new Date());
        month.setTimeInMillis(System.currentTimeMillis());

        layout.addView(calendarHeader());
        layout.addView(weekdayRow());
        grid = new LinearLayout(this);
        grid.setOrientation(LinearLayout.VERTICAL);
        layout.addView(grid);
        toggle = new Button(this);
        toggle.setAllCaps(false);
        toggle.setOnClickListener(v -> { expanded = !expanded; renderCalendar(); });
        layout.addView(toggle);
        emptyDay = label("Sin yapes este día.", 16, false);
        emptyDay.setPadding(0, 18, 0, 0);
        layout.addView(emptyDay);
        list = new ListView(this);
        layout.addView(list, new LinearLayout.LayoutParams(-1, 0, 1));
        renderCalendar();
        renderList();
    }

    private View calendarHeader() {
        LinearLayout bar = new LinearLayout(this);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        Button previous = new Button(this);
        previous.setText("‹");
        previous.setOnClickListener(v -> { step(-1); renderCalendar(); });
        Button next = new Button(this);
        next.setText("›");
        next.setOnClickListener(v -> { step(1); renderCalendar(); });
        monthTitle = label("", 19, true);
        monthTitle.setGravity(Gravity.CENTER);
        monthTitle.setPadding(0, 0, 0, 0);
        bar.addView(previous, new LinearLayout.LayoutParams(-2, -2));
        bar.addView(monthTitle, new LinearLayout.LayoutParams(0, -2, 1));
        bar.addView(next, new LinearLayout.LayoutParams(-2, -2));
        return bar;
    }

    private View weekdayRow() {
        LinearLayout row = new LinearLayout(this);
        for (String name : new String[]{"L", "M", "M", "J", "V", "S", "D"}) {
            TextView cell = label(name, 13, true);
            cell.setGravity(Gravity.CENTER);
            cell.setPadding(0, 0, 0, 4);
            row.addView(cell, new LinearLayout.LayoutParams(0, -2, 1));
        }
        return row;
    }

    private void step(int direction) {
        month.add(expanded ? Calendar.MONTH : Calendar.WEEK_OF_YEAR, direction);
    }

    private void renderCalendar() {
        Calendar first = (Calendar) month.clone();
        int offset;
        int length;
        if (expanded) {
            first.set(Calendar.DAY_OF_MONTH, 1);
            String title = new SimpleDateFormat("MMMM yyyy", locale).format(first.getTime());
            monthTitle.setText(Character.toUpperCase(title.charAt(0)) + title.substring(1));
            offset = (first.get(Calendar.DAY_OF_WEEK) + 5) % 7;
            length = first.getActualMaximum(Calendar.DAY_OF_MONTH);
        } else {
            int back = (first.get(Calendar.DAY_OF_WEEK) + 5) % 7;
            first.add(Calendar.DAY_OF_MONTH, -back);
            Calendar end = (Calendar) first.clone();
            end.add(Calendar.DAY_OF_MONTH, 6);
            SimpleDateFormat shortDay = new SimpleDateFormat("d MMM", locale);
            monthTitle.setText(shortDay.format(first.getTime()) + " – " + shortDay.format(end.getTime()));
            offset = 0;
            length = 7;
        }
        toggle.setText(expanded ? "Ver solo esta semana" : "Ver calendario completo");
        grid.removeAllViews();

        int cellHeight = Math.round(40 * getResources().getDisplayMetrics().density);
        int weeks = (offset + length + 6) / 7;
        for (int week = 0; week < weeks; week++) {
            LinearLayout row = new LinearLayout(this);
            for (int col = 0; col < 7; col++) {
                int index = week * 7 + col - offset;
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, cellHeight, 1);
                if (index < 0 || index >= length) {
                    row.addView(new View(this), params);
                    continue;
                }
                Calendar date = (Calendar) first.clone();
                date.add(Calendar.DAY_OF_MONTH, index);
                String key = dayKey.format(date.getTime());
                boolean hasYapes = days.containsKey(key);
                boolean isSelected = key.equals(selected);

                TextView cell = new TextView(this);
                cell.setGravity(Gravity.CENTER);
                cell.setTextSize(16);
                cell.setText(Integer.toString(date.get(Calendar.DAY_OF_MONTH)));
                cell.setTextColor(isSelected ? Color.WHITE : Palette.SLATE);
                if (isSelected) cell.setBackgroundColor(Palette.TEAL_DARK);
                else if (hasYapes) cell.setBackgroundColor(Palette.TINT);
                cell.setOnClickListener(v -> { selected = key; renderCalendar(); renderList(); });
                params.setMargins(1, 1, 1, 1);
                row.addView(cell, params);
            }
            grid.addView(row);
        }
    }

    private void renderList() {
        List<PaymentStore.Payment> day = new ArrayList<>();
        for (PaymentStore.Payment payment : rows) {
            if (dayKey.format(new Date(payment.at)).equals(selected)) day.add(payment);
        }
        emptyDay.setVisibility(day.isEmpty() ? View.VISIBLE : View.GONE);
        list.setAdapter(new DayAdapter(groupByDay(day)));
    }

    private List<Object> groupByDay(List<PaymentStore.Payment> rows) {
        Locale locale = new Locale("es", "PE");
        SimpleDateFormat dayKey = new SimpleDateFormat("yyyyMMdd", locale);
        SimpleDateFormat dayName = new SimpleDateFormat("EEEE d 'de' MMMM", locale);
        SimpleDateFormat time = new SimpleDateFormat("HH:mm", locale);
        String today = dayKey.format(new Date());
        String yesterday = dayKey.format(new Date(System.currentTimeMillis() - 24L * 60 * 60 * 1000));

        List<Object> items = new ArrayList<>();
        int i = 0;
        while (i < rows.size()) {
            String key = dayKey.format(new Date(rows.get(i).at));
            int end = i;
            long total = 0;
            while (end < rows.size() && dayKey.format(new Date(rows.get(end).at)).equals(key)) {
                total += rows.get(end).cents;
                end++;
            }
            String name = dayName.format(new Date(rows.get(i).at));
            if (key.equals(today)) name = "Hoy · " + name;
            else if (key.equals(yesterday)) name = "Ayer · " + name;
            else name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
            int count = end - i;
            items.add(new String[]{name, Money.format(total) + " en " + count + (count == 1 ? " yape" : " yapes")});
            for (; i < end; i++) {
                PaymentStore.Payment payment = rows.get(i);
                String sender = payment.sender.isEmpty() ? "Remitente no indicado" : payment.sender;
                items.add(sender + "\n" + Money.format(payment.cents) + "    ·    " + time.format(new Date(payment.at)));
            }
        }
        return items;
    }

    /** Rows are either a String[]{day, daily total} header or a String payment line. */
    private final class DayAdapter extends BaseAdapter {
        private final List<Object> items;

        DayAdapter(List<Object> items) { this.items = items; }

        @Override public int getCount() { return items.size(); }
        @Override public Object getItem(int position) { return items.get(position); }
        @Override public long getItemId(int position) { return position; }
        @Override public boolean isEnabled(int position) { return false; }
        @Override public int getViewTypeCount() { return 2; }
        @Override public int getItemViewType(int position) { return items.get(position) instanceof String[] ? 0 : 1; }

        @Override
        public View getView(int position, View convert, ViewGroup parent) {
            Object item = items.get(position);
            int pad = Math.round(12 * getResources().getDisplayMetrics().density);
            if (item instanceof String[]) {
                String[] header = (String[]) item;
                LinearLayout box = convert instanceof LinearLayout ? (LinearLayout) convert : new LinearLayout(HistoryActivity.this);
                box.removeAllViews();
                box.setOrientation(LinearLayout.VERTICAL);
                box.setBackgroundColor(Palette.TINT);
                box.setPadding(pad, pad, pad, pad);
                TextView day = label(header[0], 17, true);
                day.setPadding(0, 0, 0, 4);
                box.addView(day);
                TextView total = label(header[1], 15, false);
                total.setPadding(0, 0, 0, 0);
                box.addView(total);
                return box;
            }
            TextView row = convert instanceof TextView ? (TextView) convert : new TextView(HistoryActivity.this);
            row.setText((String) item);
            row.setTextSize(16);
            row.setTextColor(Palette.SLATE);
            row.setPadding(pad, pad, pad, pad);
            return row;
        }
    }

    private TextView label(String words, int size, boolean bold) {
        TextView label = new TextView(this);
        label.setText(words);
        label.setTextSize(size);
        label.setTextColor(Palette.SLATE);
        if (bold) label.setTypeface(null, android.graphics.Typeface.BOLD);
        label.setPadding(0, 0, 0, 18);
        return label;
    }

    @Override
    protected void onDestroy() {
        payments.close();
        super.onDestroy();
    }
}
