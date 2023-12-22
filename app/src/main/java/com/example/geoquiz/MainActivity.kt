package com.example.geoquiz


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import android.widget.Toast.makeText
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import com.example.geoquiz.databinding.ActivityMainBinding


private const val TAG = "MainActivity"
class MainActivity : ComponentActivity() {
    private lateinit var binding: ActivityMainBinding

    private val quizViewModel: QuizViewModel by viewModels()

    private val cheatLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Handle the result
        if (result.resultCode == Activity.RESULT_OK) {
            quizViewModel.isCheater =
                result.data?.getBooleanExtra(EXTRA_ANSWER_SHOWN, false) ?: false
        }
    }

    private var correctAnswers = 0
    private var answered = 0
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate(Bundle?) called")
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(TAG, "Got a QuizViewModel: $quizViewModel")

        binding.trueButton.setOnClickListener {
            checkAnswer(true)
        }

        binding.falseButton.setOnClickListener {
            checkAnswer(false)
        }

        binding.nextButton.setOnClickListener {
            quizViewModel.moveToNext()
            updateQuestion()
        }
        var tokens = 3

        binding.cheatButton.setOnClickListener {
        // Start CheatActivity
            //Challenge: Limited Cheats
            if ( tokens <= 3 && tokens != 0) {
                tokens -= 1
                val remTokens = getString(R.string.rem_cheats, tokens)
                makeText(this, remTokens, LENGTH_SHORT).show()
                Log.d(TAG, "TOKENS $tokens")
                val answerIsTrue = quizViewModel.currentQuestionAnswer
                val intent = CheatActivity.newIntent(this@MainActivity, answerIsTrue)
                cheatLauncher.launch(intent)
            } else {
                binding.cheatButton.isEnabled = false
                makeText(this,R.string.out_cheats, LENGTH_SHORT).show()
            }

        }
        updateQuestion()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            blurCheatButton()
        }

        binding.previousButton.setOnClickListener {
            quizViewModel.moveToPrev()
            updateQuestion()
        }
        updateQuestion()

        binding.questionTextView.setOnClickListener {
            quizViewModel.moveToNext()
            updateQuestion()
        }
        quizViewModel.initCheatProtection()
        updateQuestion()
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart() called")
    }
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume() called")
    }
    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause() called")
    }
    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop() called")
    }
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy() called")
    }

    private fun manageAnswerButtons() {
        if (quizViewModel.answered) {
            binding.trueButton.isEnabled = false
            binding.falseButton.isEnabled = false
        } else {
            binding.trueButton.isEnabled = true
            binding.falseButton.isEnabled = true
        }
    }

    private fun updateQuestion() {
        //val questionTextResId = questionBank[currentIndex].textResId
        val questionTextResId = quizViewModel.currentQuestionText
        binding.questionTextView.setText(questionTextResId)
        manageAnswerButtons()
    }

    @SuppressLint("StringFormatInvalid")
    private fun checkAnswer(userAnswer: Boolean) {
        binding.falseButton.isEnabled = false
        binding.trueButton.isEnabled = false
        quizViewModel.answered = true
        val correctAnswer = quizViewModel.currentQuestionAnswer
        val messageResId = when {
            quizViewModel.cheatedQuestion[quizViewModel.currentIndex] -> R.string.judgment_toast
            quizViewModel.isCheater -> R.string.judgment_toast
            userAnswer == correctAnswer -> R.string.correct_toast
            else -> R.string.incorrect_toast
        }
        answered++
        if (isAllQuestionsAnswered()) {
            val score = getString(R.string.percent_grade, calculateScore().toInt())
            Toast.makeText(this, score, Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, messageResId, Toast.LENGTH_SHORT)
                .show()
        }
        correctAnswers++
    }
    @RequiresApi(Build.VERSION_CODES.S)
    private fun blurCheatButton() {
        val effect = RenderEffect.createBlurEffect(
            10.0f,
            10.0f,
            Shader.TileMode.CLAMP
        )
        binding.cheatButton.setRenderEffect(effect)
    }


    private fun isAllQuestionsAnswered(): Boolean {
        if (answered == quizViewModel.questionBank.size){
            return true
        }
        return false
    }

    private fun calculateScore():Double {
        return (correctAnswers.toDouble() / quizViewModel.questionBank.size) * 100
    }
}

