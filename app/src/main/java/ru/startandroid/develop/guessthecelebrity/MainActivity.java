package ru.startandroid.develop.guessthecelebrity;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    DownloadContent task;
    DownloadImage task1;
    String result;
    ArrayList<String> celebNames;
    ArrayList<String> celebUrls;
    Pattern p;
    int randomCelebInt;
    int correctAnswer;
    String[] answers;
    Random rand;
    Matcher m;
    ImageView imageView;
    Button button1;
    Button button2;
    Button button3;
    Button button4;
    Bitmap bitmap;

    public void generateAnswers() {
        randomCelebInt = rand.nextInt(celebUrls.size());
        correctAnswer = rand.nextInt(4);

        task1 = new DownloadImage();

        try {
            bitmap = task1.execute(celebUrls.get(randomCelebInt)).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        imageView.setImageBitmap(bitmap);

        for (int i = 0; i < 4; i++) {
            if (i == correctAnswer) {
                answers[i] = celebNames.get(randomCelebInt);
            } else {
                String tempAnswer = celebNames.get(rand.nextInt(celebNames.size()));
                while (tempAnswer.equals(celebNames.get(randomCelebInt))) {
                    tempAnswer = celebNames.get(rand.nextInt(celebNames.size()));
                }
                answers[i] = tempAnswer;
            }
        }

        button1.setText(answers[0]);
        button2.setText(answers[1]);
        button3.setText(answers[2]);
        button4.setText(answers[3]);

    }

    public void celebChosen(View v) {
        if(v.getTag().toString().equals(Integer.toString(correctAnswer))) {
            Toast.makeText(getApplicationContext(), "Correct!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Wrong! The correct answer is " + celebNames.get(randomCelebInt), Toast.LENGTH_LONG).show();
        }
        generateAnswers();
    }

    public class DownloadImage extends AsyncTask<String, Void, Bitmap> {

        URL url;
        HttpURLConnection connection;
        InputStream in;
        Bitmap bitmap;

        @Override
        protected Bitmap doInBackground(String... urls) {

            try {

                url = new URL(urls[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                in = connection.getInputStream();
                bitmap = BitmapFactory.decodeStream(in);
                return bitmap;

            } catch (Exception e) {
                return null;
            }
        }
    }

    public class DownloadContent extends AsyncTask<String, Void, String> {

        URL url;
        HttpURLConnection connection;
        InputStream in;
        InputStreamReader reader;
        BufferedReader br;
        String result = "";

        @Override
        protected String doInBackground(String... urls) {

            try {
                url = new URL(urls[0]);
                connection = (HttpURLConnection) url.openConnection();
                in = connection.getInputStream();
                reader = new InputStreamReader(in);
                br = new BufferedReader(reader);

                String tmpString;
                while ((tmpString = br.readLine()) != null) {
                    result += tmpString;
                }
                return result;
            } catch (Exception e) {
                return null;
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        celebNames = new ArrayList<String>();
        celebUrls = new ArrayList<String>();
        rand = new Random();
        answers = new String[4];
        imageView = (ImageView) findViewById(R.id.imageView);
        button1 = (Button) findViewById(R.id.button);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);
        button4 = (Button) findViewById(R.id.button4);

        task = new DownloadContent();

        try {
            result = task.execute("https://www.imdb.com/list/ls052283250/").get();
        } catch (Exception e) {

        }

        String[] firstSplit = result.split("updated - 13 Dec 2014");
        String firstResult = firstSplit[1];
        String[] secondSplit = firstResult.split("en a megastar in the Tamil");
        String lastResult = secondSplit[0];


        p = Pattern.compile("src=\"(.*?)\"");
        m = p.matcher(lastResult);

        while (m.find()) {
            celebUrls.add(m.group(1));
        }

        p = Pattern.compile("<img alt=\"(.*?)\"");
        m = p.matcher(lastResult);

        while (m.find()) {
            celebNames.add(m.group(1));
        }

        generateAnswers();
    }


}