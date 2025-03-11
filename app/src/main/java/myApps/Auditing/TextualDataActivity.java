package myApps.Auditing;

import android.app.Activity;
import android.os.Bundle;

import android.view.View;
import android.widget.EditText;

public class TextualDataActivity extends Activity {
  static EditText IN;
  static String data = null;
  static Runnable function = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.layout_textual);
    IN = findViewById(R.id.textual_input_box);
    if (function == null){
      IN.setText("Err: you do not pass function");
      function = new Runnable() {
        @Override
        public void run() {}
      };
    }
    else if (data != null)
      IN.setText(data);
    findViewById(R.id.return_textual_data_button).setOnClickListener(
          new View.OnClickListener() {
            @Override
            public void onClick(View v){
              data = TextualDataActivity.IN.getText().toString();
              IN.setText("");
              function.run();
              function = null;
              data = null;
              finish();
            }
          }
        );
    findViewById(R.id.cancel_textual_data_button).setOnClickListener(
          new View.OnClickListener() {
            @Override
            public void onClick(View v){
              IN.setText("");
              function = null;
              data = null;
              finish();
            }
          }
        );
    }
}
