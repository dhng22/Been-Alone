package com.example.beenalone;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;

import android.util.Log;
import android.view.View;


import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.EditText;

import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.material.navigation.NavigationView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoUnit;
import java.util.StringTokenizer;


public class MainActivity extends AppCompatActivity {
    ImageButton btnMenu;
    DrawerLayout layoutDrawer;
    NavigationView viewNavigation;
    String quote = "";
    String artist = "";
    String GET_QUOTE_URL = "https://www.generatormix.com/random-love-quotes";
    CheckBox btnLock, cbQuote;
    SharedPreferences.Editor editor;
    View.OnClickListener clickChangeMode;
    int timeLong;
    EditText edtDatePick;
    LocalDate chosenDate;
    DateTimeFormatter format;
    TextView txtNumber, txtDay, txtBeenFor, txtHeader, txtReminder, txtLocked, txtUnlocked, txtEnabled, txtDisabled, txtArtist, txtQuote;
    String strChosenDate;
    SharedPreferences sharedPreferences;
    String MODE_DAYS = "days";
    String MODE_YEARS = "years";
    Animation fadeIn, fadeOut, popOut, blink, slideOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mapping();
        init();
        edtDatePick.setOnClickListener(view -> {
            pickDate();
            moveViewUp(view);
        });
        changeViewMode();
        initAnimCheck();


    }

    protected void checkQuoteActivated() {
        if (sharedPreferences.getBoolean("quoteActivated", false)) {
            cbQuote.setChecked(true);
        }
    }

    protected void enableQuote() {
        cbQuote.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                //enabled slide out
                txtEnabled.setAlpha(1);
                txtEnabled.startAnimation(slideOut);
                slideOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        cbQuote.setEnabled(false);
                        editor.putBoolean("quoteActivated", true);
                        editor.commit();
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        txtEnabled.setAlpha(0);
                        cbQuote.setEnabled(true);
                        txtQuote.animate().alpha(1).setDuration(500).start();
                        txtArtist.animate().alpha(1).setStartDelay(500).setDuration(400).start();

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            } else {
                txtDisabled.setAlpha(1);
                txtDisabled.startAnimation(slideOut);
                slideOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        cbQuote.setEnabled(false);
                        editor.putBoolean("quoteActivated", false);
                        editor.commit();
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        txtDisabled.setAlpha(0);
                        cbQuote.setEnabled(true);
                        txtArtist.animate().alpha(0).setStartDelay(0).setDuration(200).start();
                        txtQuote.animate().alpha(0).setDuration(200).start();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            }
        });
    }

    protected void changeLock() {
        btnLock.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                txtLocked.setAlpha(1);
                txtLocked.startAnimation(popOut);
                popOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        edtDatePick.setEnabled(false);
                        btnLock.setEnabled(false);
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        txtLocked.clearAnimation();
                        txtLocked.setAlpha(0);
                        editor.putBoolean("isLocked", true);
                        editor.commit();
                        btnLock.setEnabled(true);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
            } else {
                txtUnlocked.setAlpha(1);
                txtUnlocked.startAnimation(popOut);
                popOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        edtDatePick.setEnabled(true);
                        btnLock.setEnabled(false);
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        txtUnlocked.clearAnimation();
                        txtUnlocked.setAlpha(0);
                        editor.putBoolean("isLocked", false);
                        editor.commit();
                        btnLock.setEnabled(true);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            }
        });
    }

    protected void pickDate() {
        DatePickerDialog pickerDialog = new DatePickerDialog(this, (datePicker, i, i1, i2) -> {
            strChosenDate = i2 + "/" + (i1 + 1) + "/" + i;
            chosenDate = LocalDate.parse(strChosenDate, format);
            timeLong = (int) ChronoUnit.DAYS.between(chosenDate, LocalDate.now());

            //set screen information
            alternateDisplay();

            //set for shared preference
            editor.putString("date", strChosenDate);
            editor.putBoolean("isUsed", true);
            editor.putString("day", timeLong > 1 ? "days" : "day");
            editor.commit();

            //animate edt view
            moveViewDown(edtDatePick);

        }, chosenDate.getYear(), chosenDate.getMonth().getValue() - 1, chosenDate.getDayOfMonth());
        pickerDialog.setCanceledOnTouchOutside(false);
        pickerDialog.setOnDismissListener(dialogInterface -> moveViewDown(edtDatePick));

        pickerDialog.show();

    }

    public void alternateDisplay() {
        if (sharedPreferences.getString("mode", MODE_DAYS).equals(MODE_DAYS)) {
            modeDays();
        } else {
            modeYears();
        }
    }

    public void changeViewMode() {
        txtNumber.setOnClickListener(clickChangeMode);
        txtDay.setOnClickListener(view -> txtNumber.performClick());
    }

    protected void changeModeCheck() {
        if (!sharedPreferences.getBoolean("modeKnown", false)) {
            CountDownTimer timer = new CountDownTimer(10000, 1000) {
                @Override
                public void onTick(long l) {

                }

                @Override
                public void onFinish() {
                    txtDay.startAnimation(blink);
                    blink.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            txtNumber.startAnimation(blink);
                            txtReminder.animate().alpha(1).setDuration(500).start();
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }
                    });
                    txtNumber.setOnClickListener(view -> {
                        txtReminder.animate().alpha(0).setDuration(350).start();
                        txtNumber.clearAnimation();
                        txtDay.clearAnimation();

                        txtNumber.setOnClickListener(clickChangeMode);
                        txtNumber.performClick();
                        editor.putBoolean("modeKnown", true);
                        editor.commit();
                    });
                }
            };

            timer.start();
        }
    }

    public void initAnimCheck() {
        if (sharedPreferences.getBoolean("isUsed", false)) {
            CountDownTimer timer = new CountDownTimer(1500, 1000) {
                @Override
                public void onTick(long l) {
                }

                @Override
                public void onFinish() {
                    moveViewDown(edtDatePick);
                }
            };
            timer.start();
        }
    }

    public void moveViewUp(View view) {
        view.animate().translationY(0).scaleX(1f).scaleY(1f).setDuration(400).start();
        txtHeader.animate().alpha(1).setDuration(500).start();

        //btn lock
        btnLock.setEnabled(false);
        btnLock.animate().alpha(0).setDuration(100).start();


        //cb quote
        cbQuote.setEnabled(false);
        cbQuote.animate().alpha(0).setDuration(100).start();

    }

    public void moveViewDown(View view) {
        Animation anim = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_wait);
        txtNumber.startAnimation(anim);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                view.animate().translationY(202).scaleX(1.25f).scaleY(1.25f).setDuration(700).start();
                txtHeader.animate().alpha(0).setDuration(200).start();
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                changeModeCheck();

                btnLock.setEnabled(true);
                btnLock.animate().translationY(198).setDuration(700).start();
                btnLock.animate().alpha(1).setDuration(200).start();
                changeLock();

                //enable checkbox quote
                cbQuote.setEnabled(true);
                cbQuote.animate().alpha(1).setStartDelay(400).setDuration(200).start();
                enableQuote();
                checkQuoteActivated();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });


    }

    public void modeDays() {
        txtBeenFor.setVisibility(View.VISIBLE);
        txtDay.setText(timeLong > 1 ? "days" : "day");
        txtNumber.setText(String.valueOf(timeLong));
        edtDatePick.setText(strChosenDate);
        editor.putString("mode", MODE_DAYS);
    }

    public void modeYears() {
        txtBeenFor.setVisibility(View.VISIBLE);
        edtDatePick.setText(strChosenDate);
        int years = Period.between(chosenDate, LocalDate.now()).getYears();
        int months = Period.between(chosenDate.minusYears(years), LocalDate.now()).getMonths();
        int days = Period.between(chosenDate.minusYears(years).minusMonths(months), LocalDate.now()).getDays();
        String strYear = years > 1 ? getString(R.string.moreYear, years) : getString(R.string.oneYear, years);
        String strMonth = months > 1 ? getString(R.string.moreMonth, months) : getString(R.string.oneMonth, months);
        String strDay = days > 1 ? getString(R.string.moreDay, days) : getString(R.string.oneDay, days);
        txtNumber.setText(String.valueOf(strYear));
        txtDay.setText(String.valueOf(strMonth + " " + strDay));
        editor.putString("mode", MODE_YEARS);
    }

    protected void mapping() {
        //initialize view and formatter
        txtEnabled = findViewById(R.id.txtQuoteEnabled);
        txtDisabled = findViewById(R.id.txtQuoteDisabled);
        txtQuote = findViewById(R.id.txtQuote);
        txtArtist = findViewById(R.id.txtArtist);
        cbQuote = findViewById(R.id.cb_quote);
        txtLocked = findViewById(R.id.txtLocked);
        txtUnlocked = findViewById(R.id.txtUnlocked);
        btnLock = findViewById(R.id.btnLock);
        txtReminder = findViewById(R.id.txtReminder);
        txtHeader = findViewById(R.id.txtHeader);
        txtNumber = findViewById(R.id.txtNumber);
        txtDay = findViewById(R.id.txtDay);
        txtBeenFor = findViewById(R.id.txtBeenFor);
        txtBeenFor.setVisibility(View.INVISIBLE);
        edtDatePick = findViewById(R.id.edtDatePick);
        chosenDate = LocalDate.now();
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder()
                .appendPattern("[dd/MM/yyyy]")
                .appendPattern("[d/M/yyyy]")
                .appendPattern("[dd/M/yyyy]")
                .appendPattern("[d/MM/yyyy]");
        format = builder.toFormatter();
        sharedPreferences = getSharedPreferences("dateData", MODE_PRIVATE);

        fadeIn = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_fadein);
        fadeOut = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_fadeout);
        blink = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_blink);
        popOut = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_popup_fadeout);
        slideOut = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_slideout);

        editor = sharedPreferences.edit();
        editor.putString("mode", MODE_DAYS);

        layoutDrawer = findViewById(R.id.layoutDrawer);
        viewNavigation = findViewById(R.id.viewNavigation);
        btnMenu = findViewById(R.id.btnMenu);
    }

    public void init() {
        if (sharedPreferences.getBoolean("isUsed", false)) {
            //set text for view if the user has used
            strChosenDate = sharedPreferences.getString("date", "");

            //dynamically calculate the day
            chosenDate = LocalDate.parse(strChosenDate, format);
            timeLong = (int) ChronoUnit.DAYS.between(chosenDate, LocalDate.now());
            alternateDisplay();

        } else {
            chosenDate = LocalDate.now();
        }
        clickChangeMode = view -> {

            view.startAnimation(fadeOut);
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    txtDay.startAnimation(fadeOut);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (sharedPreferences.getString("mode", MODE_DAYS).equals(MODE_DAYS)) {
                        modeYears();
                    } else {
                        modeDays();
                    }
                    view.startAnimation(fadeIn);
                    txtDay.startAnimation(fadeIn);
                    editor.commit();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
        };

        if (sharedPreferences.getBoolean("isLocked", false)) {
            btnLock.performClick();
            edtDatePick.setEnabled(false);
        }

        //init quote
        new GetQuote().execute();

        //init navigation menu
        btnMenu.setOnClickListener(view -> NavigationMenu());

        viewNavigation.setNavigationItemSelectedListener(item -> {
            //Insert navigation menu action here

            item.setChecked(true);
            Toast.makeText(MainActivity.this, item.getTitle(), Toast.LENGTH_SHORT).show();
            layoutDrawer.closeDrawers();
            return true;
        });
    }

    private class GetQuote extends AsyncTask<Void, Void, String> {
        String tempQuote = null;

        @Override
        protected String doInBackground(Void... voids) {
            try {
                Document pageDoc = Jsoup.connect(GET_QUOTE_URL).get();


                tempQuote = pageDoc.select("blockquote").first().text();

                StringTokenizer tokenizer = new StringTokenizer(tempQuote, "-");
                tempQuote = tokenizer.nextToken().trim();
                while (tokenizer.countTokens() > 1) {
                    tempQuote = tempQuote.concat("-" + (tokenizer.nextToken().trim()));
                }
                artist = "- " + tokenizer.nextToken().trim() + " -";
            } catch (IOException e) {
                Log.d("AAA", e.getMessage());
                tempQuote = "";
                artist = "";
            }

            return tempQuote;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s == null) {
                s = "";
            }
            if (s.length() > 250) {
                new GetQuote().execute();
            } else if (s.equals("")) {
                quote = "\" We're born alone, we live alone, we die alone. Only through our love and friendship can we create the illusion for the moment that we're not alone. \"";
                artist = "- Orson Welles -";
            } else {
                quote = "\" " + s + " \"";
                txtArtist.setText(artist);
                txtQuote.setText(quote);
            }
        }
    }

    private void NavigationMenu() {
        layoutDrawer.openDrawer(GravityCompat.START);
    }
}