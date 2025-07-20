package com.example.p3

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var diceImage1: ImageView

    private lateinit var rollOneButton: Button

    private lateinit var resultText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize views
        diceImage1 = findViewById(R.id.dice_image1)
//        diceImage2 = findViewById(R.id.dice_image2)
        rollOneButton = findViewById(R.id.roll_one_button)
//        rollTwoButton = findViewById(R.id.roll_two_button)
        resultText = findViewById(R.id.result_text)

        // Set initial text
        resultText.text = "ROLL THE DEVICE TO START!"

        // Set click listeners for buttons
        rollOneButton.setOnClickListener {
            rollOneDice()
        }

//        rollTwoButton.setOnClickListener {
//            rollTwoDice()
//        }
    }

    /**
     * Roll one dice and update the UI
     */
    private fun rollOneDice() {
        // Start the rotation animation
        animateDiceRoll(diceImage1)

        val randomNumber = getRandomDiceNumber()

        // Update first dice image - with a slight delay so animation is visible
        val diceResource = getDiceDrawableResource(randomNumber)

        // Handler to update the image after animation starts
        Handler(Looper.getMainLooper()).postDelayed({
            diceImage1.setImageResource(diceResource)
            // Update result text
            resultText.text = "Result: $randomNumber"
        }, 900) // Delay showing result until near the end of animation
    }

    /**
     * Roll two dice and update the UI
     */
    private fun rollTwoDice() {

        animateDiceRoll(diceImage1)

        val randomNumber1 = getRandomDiceNumber()

        val totalValue = randomNumber1

        // Update first dice image
        val diceResource1 = getDiceDrawableResource(randomNumber1)
        diceImage1.setImageResource(diceResource1)



        // Update result text
        resultText.text = "Result: $randomNumber1 = $totalValue"
    }

    /**
     * Generate a random dice number between 1 and 6
     */
    private fun getRandomDiceNumber(): Int {
        return Random.nextInt(1, 7)
    }

    /**
     * Get the appropriate drawable resource ID based on the dice value
     */
    private fun getDiceDrawableResource(number: Int): Int {
        return when (number) {
            1 -> R.drawable.dice_face_1 // Update this to your Flaticon image name
            2 -> R.drawable.dice_face_2 // Update this to your Flaticon image name
            3 -> R.drawable.dice_face_3 // Update this to your Flaticon image name
            4 -> R.drawable.dice_face_4 // Update this to your Flaticon image name
            5 -> R.drawable.dice_face_5 // Update this to your Flaticon image name
            6 -> R.drawable.dice_face_6 // Update this to your Flaticon image name
            else -> R.drawable.dice_face_1 // Update this to your Flaticon image name
        }
    }

    /**
     * Animate dice roll with rotation effect
     */
    private fun animateDiceRoll(diceImage: ImageView) {
        val rotateAnimation = RotateAnimation(
            0f, 360f * 2, // Rotate n times of full spins
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )

        rotateAnimation.duration = 1000 // Animation duration in milliseconds
        rotateAnimation.interpolator = AccelerateDecelerateInterpolator()

        diceImage.startAnimation(rotateAnimation)
    }
}