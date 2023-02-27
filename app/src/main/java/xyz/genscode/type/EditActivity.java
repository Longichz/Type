package xyz.genscode.type;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class EditActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        Intent intent = getIntent();

        EditText etEdit = findViewById(R.id.etEditChange);
        View ivApply = findViewById(R.id.llEditApply);

        int max = intent.getIntExtra("maxLength", 32);
        etEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(max)});

        etEdit.setText("");
        if(intent.hasExtra("editValue")) etEdit.setText(intent.getStringExtra("editValue"));

        if(intent.hasExtra("header")){
            TextView tvEditTopIndicator = findViewById(R.id.tvEditTopIndicator);
            tvEditTopIndicator.setText(intent.getStringExtra("header"));
            etEdit.setHint(intent.getStringExtra("header"));
        }


        if(intent.hasExtra("allowNull") && intent.getBooleanExtra("allowNull", false)){
            ivApply.setVisibility(View.VISIBLE);
        }else{
            etEdit.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if(charSequence.length() >= 1){
                        ivApply.setVisibility(View.VISIBLE);
                    }else{
                        ivApply.setVisibility(View.GONE);
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
        }



        findViewById(R.id.llEditBack).setOnClickListener(view -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        ivApply.setOnClickListener(view -> {
            intent.putExtra("data", etEdit.getText().toString());
            setResult(RESULT_OK, intent);
            finish();
        });
    }
}