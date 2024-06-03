package com.teaminversion.envisionbuddy;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class playActivity extends AppCompatActivity {

    String word;
    String[] question_list = {"What is an example of a primary color?",
            "Which of the following is an example of a mammal?","Which of the following is an example of a renewable energy source?"
            ,"What is an example of a citrus fruit?",
            "Which of the following is an example of a polygon?"
    };
    String[] choose_list = {"Green","Red","Orange","Purple",
            "Snake","Fish","Dog","Frog",
            "Coal","Oil","Wind","Natural Gas",
            "Apple","Banana","Orange","Watermelon",
            "Circle","Triangle","Oval","Sphere"
    };
    String[] correct_list = {"Red","Dog","Wind","Orange","Triangle"};


    TextView cpt_question , text_question;
    Button btn_choose1 , btn_choose2 , btn_choose3 , btn_choose4 , btn_next;
    private ProgressBar progressBarQuestions;
    private ConstraintLayout constraintLayout;
    private LinearLayout linearLayout;


    int currentQuestion =  0  ;
    int scorePlayer =  0  ;
    boolean isclickBtn = false;
    String valueChoose = "";
    Button btn_click;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_play);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        word = "Generate 5 multiple-choice questions based on the word " + getIntent().getStringExtra("WORD_EXTRA") + ", including correct answers. Give the response in this format: 2. What is the top-selling car brand in the United States?\\n   A. Toyota\\n   B. Ford\\n   C. Honda\\n   D. Chevrolet\\n   Correct answer: A. Toyota\\n\\n" + ". Make sure to give all five questions.";

        new GenerateQuestionsTask().execute();

        cpt_question = findViewById(R.id.cpt_question);
        text_question = findViewById(R.id.text_question);

        btn_choose1 = findViewById(R.id.btn_choose1);
        btn_choose2 = findViewById(R.id.btn_choose2);
        btn_choose3 = findViewById(R.id.btn_choose3);
        btn_choose4 = findViewById(R.id.btn_choose4);
        btn_next = findViewById(R.id.btn_next);
        progressBarQuestions = findViewById(R.id.questionsProgressBar);
        constraintLayout = findViewById(R.id.constraintLayout);
        linearLayout = findViewById(R.id.linearLayout);

        findViewById(R.id.image_back).setOnClickListener(
                a-> finish()
        );
        //remplirData();
        btn_next.setOnClickListener(
                view -> {
                    if (isclickBtn){
                        isclickBtn = false;

                        if(!valueChoose.equals(correct_list[currentQuestion])){
                            Toast.makeText(playActivity.this , "Wrong..Correct Answer: " + correct_list[currentQuestion],Toast.LENGTH_LONG).show();
                            btn_click.setBackgroundResource(R.drawable.background_btn_error);

                        }else {
                            Toast.makeText(playActivity.this , "Correct!",Toast.LENGTH_LONG).show();
                            btn_click.setBackgroundResource(R.drawable.background_btn_correct);

                            scorePlayer++;
                        }
                        new Handler().postDelayed(() -> {
                            if(currentQuestion!=question_list.length-1){
                                currentQuestion = currentQuestion + 1;
                                remplirData();
                                valueChoose = "";
                                resetButtonBackgrounds();

                            }else {
                                Intent intent  = new Intent(playActivity.this , ResultActivity.class);
                                intent.putExtra("Result" , scorePlayer);
                                startActivity(intent);
                                finish();
                            }

                        },1000);

                    }else {
                        Toast.makeText(playActivity.this ,  "You must choose one",Toast.LENGTH_LONG).show();
                    }
                }
        );

        btn_choose1.setOnClickListener(this::ClickChoose);
        btn_choose2.setOnClickListener(this::ClickChoose);
        btn_choose3.setOnClickListener(this::ClickChoose);
        btn_choose4.setOnClickListener(this::ClickChoose);
    }

    private class GenerateQuestionsTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            return chatGPT(word); // Call chatGPT method here
        }

        @Override
        protected void onPostExecute(String chatGPTResponse) {
            super.onPostExecute(chatGPTResponse);
            Log.d("ChatGPT Response: ", chatGPTResponse);
            if (chatGPTResponse != null && !chatGPTResponse.isEmpty()) {
                String[] individual_questions = chatGPTResponse.split("\\d+\\.\\s+");

//                for(int i=0;i<individual_questions.length;i++)
//                    Log.d("QnA's", individual_questions[i]);
                //Log.d("QnA length", Integer.toString(individual_questions.length));

                if(individual_questions.length == 6){
                    progressBarQuestions.setVisibility(View.GONE);
                    constraintLayout.setAlpha(1f);
                    linearLayout.setAlpha(1f);
                    btn_next.setAlpha(1f);
                    question_list = new String[individual_questions.length - 1];
                    choose_list = new String[(individual_questions.length - 1)*4];
                    correct_list = new String[individual_questions.length - 1];

//                    for (int i = 0; i < questionAndAnswers.length; i++) {
//                        String[] parts = questionAndAnswers[i].split("\\n");
//                        question_list[i] = parts[0].substring(parts[0].indexOf(":") + 1).trim(); // Extract question
//                        choose_list[i * 4] = parts[1].substring(parts[1].indexOf(":") + 1).trim(); // Extract choices
//                        choose_list[i * 4 + 1] = parts[2].substring(parts[2].indexOf(":") + 1).trim();
//                        choose_list[i * 4 + 2] = parts[3].substring(parts[3].indexOf(":") + 1).trim();
//                        choose_list[i * 4 + 3] = parts[4].substring(parts[4].indexOf(":") + 1).trim();
//                        correct_list[i] = parts[5].substring(parts[5].indexOf(":") + 1).trim(); // Extract correct answer
//                    }

                    // Loop through each question and extract information
                    int j=0;
                    for (int i = 1; i < individual_questions.length; i++) {
                        // Extract question and options

                        String question = individual_questions[i].split("\\?")[0] + " ?";

                        //Log.d("array:", Arrays.toString(individual_questions[i].split("\\n\\s+Correct answer:\\s")));

                        String optionsPart = individual_questions[i].split("Correct answer:")[0];
                        //Log.d("Options:" +i, optionsPart);
                        String correct = individual_questions[i].split("Correct answer:")[1];
                        //Log.d("Correct: "+i, correct );

                        // Extract options
                        //Log.d("Options", Arrays.toString(optionsPart.split("[A-D]\\.")));
//                        Pattern pattern = Pattern.compile("[A-D]\\.\\s(.*)(\\n)*");
//                        Matcher matcher = pattern.matcher(optionsPart);
//                        //int j = 0;
//                        while (matcher.find()) {
//                            Log.d("Matcher group", matcher.group(1));
//                            choose_list[j++] = matcher.group(1);
//                        }
                        String[] options = optionsPart.split("[A-D]\\.");
                        for(int k = 1; k<options.length; k++){
                            choose_list[j++] = options[k].replace("\\n","").trim();
                        }
                        Log.d("Options"+i, Arrays.toString(choose_list));

                        // Extract correct answer
                        String correctAnswer = correct.substring(3).trim();
                        correctAnswer = correctAnswer.replace("\\n","");
                        Log.d("Correct Ans:", correctAnswer);

                        // Populate arrays
                        question_list[i - 1] = question;
                        correct_list[i - 1] = correctAnswer;
                    }

                    remplirData();

                } else{
                    progressBarQuestions.setVisibility(View.GONE);
                    constraintLayout.setAlpha(1f);
                    linearLayout.setAlpha(1f);
                    btn_next.setAlpha(1f);
                    Toast.makeText(playActivity.this, "Insufficient response from chat gpt", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Handle empty or insufficient response
                progressBarQuestions.setVisibility(View.GONE);
                constraintLayout.setAlpha(1f);
                linearLayout.setAlpha(1f);
                btn_next.setAlpha(1f);
                Toast.makeText(playActivity.this, "Empty response from ChatGPT", Toast.LENGTH_SHORT).show();
            }
        }
    }


//    private void generateQuestions() {
//        String chatGPTResponse = chatGPT(word); // Call chatGPT method here
//        Log.d("ChatGPT Response: ", chatGPTResponse);
//        if (!chatGPTResponse.isEmpty()) {
//            String[] questionAndAnswers = chatGPTResponse.split("\\n\\n");
//            if(questionAndAnswers.length == 5){
//                // Empty the arrays
//                question_list = new String[questionAndAnswers.length];
//                choose_list = new String[questionAndAnswers.length];
//                correct_list = new String[questionAndAnswers.length];
//
//                for (int i = 0; i < questionAndAnswers.length; i++) {
//                    String[] parts = questionAndAnswers[i].split("\\n");
//                    question_list[i] = parts[0].substring(parts[0].indexOf(":") + 1).trim(); // Extract question
//                    choose_list[i] = parts[1]; // Choices start from the second line
//                    correct_list[i] = parts[parts.length - 1].substring(parts[parts.length - 1].indexOf(":") + 1).trim(); // Extract correct answer
//                }
//
//                // Print parsed data
//                for (int i = 0; i < questionAndAnswers.length; i++) {
//                    System.out.println("Question " + (i + 1) + ": " + question_list[i]);
//                    System.out.println("Choices: " + choose_list[i]);
//                    System.out.println("Correct Answer: " + correct_list[i]);
//                    System.out.println();
//                }
//
//            } else{
//                Toast.makeText(this, "Insufficient response from chat gpt", Toast.LENGTH_SHORT).show();
//            }
//        } else {
//            // Handle empty or insufficient response
//            Toast.makeText(this, "Empty response from ChatGPT", Toast.LENGTH_SHORT).show();
//        }
//    }

    void remplirData(){
        cpt_question.setText((currentQuestion+1) + "/" + question_list.length);
        text_question.setText(question_list[currentQuestion]);

        btn_choose1.setText(choose_list[4 * currentQuestion]);
        btn_choose2.setText(choose_list[4 * currentQuestion+1]);
        btn_choose3.setText(choose_list[4 * currentQuestion+2]);
        btn_choose4.setText(choose_list[4 * currentQuestion+3]);

    }

    public void ClickChoose(View view) {
        btn_click = (Button)view;

        if (isclickBtn) {
            resetButtonBackgrounds();
        }
        chooseBtn();


    }
    void chooseBtn(){

        btn_click.setBackgroundColor(Color.parseColor("#565CCE"));
        isclickBtn = true;
        valueChoose = btn_click.getText().toString();
    }

    void resetButtonBackgrounds() {
        btn_choose1.setBackgroundResource(R.drawable.background_btn_choose);
        btn_choose2.setBackgroundResource(R.drawable.background_btn_choose);
        btn_choose3.setBackgroundResource(R.drawable.background_btn_choose);
        btn_choose4.setBackgroundResource(R.drawable.background_btn_choose);
    }

    public static String chatGPT(String message) {
        String url = "https://api.openai.com/v1/chat/completions";
        String apiKey = "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"; // API key goes here
        String model = "gpt-3.5-turbo"; // current model of chatgpt api

        try {
            // Create the HTTP POST request
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Authorization", "Bearer " + apiKey);
            con.setRequestProperty("Content-Type", "application/json");

            // Build the request body
            String body = "{\"model\": \"" + model + "\", \"messages\": [{\"role\": \"user\", \"content\": \"" + message + "\"}]}";
            con.setDoOutput(true);
            OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
            writer.write(body);
            writer.flush();
            writer.close();

            // Get the response
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // returns the extracted contents of the response.
            return extractContentFromResponse(response.toString());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static String extractContentFromResponse(String response) {
        int startMarker = response.indexOf("content")+11; // Marker for where the content starts.
        int endMarker = response.indexOf("\"", startMarker); // Marker for where the content ends.
        return response.substring(startMarker, endMarker); // Returns the substring containing only the response.
    }
}