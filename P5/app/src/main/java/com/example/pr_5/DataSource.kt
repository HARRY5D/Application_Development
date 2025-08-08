package com.example.pr_5

import android.content.Context

class DataSource {
    fun loadAffirmations(): List<Affirmation> {
        return listOf(
            Affirmation(R.string.affirmation1, R.drawable.image_1),
            Affirmation(R.string.affirmation2, R.drawable.image_2),
            Affirmation(R.string.affirmation3, R.drawable.image_3),
            Affirmation(R.string.affirmation4, R.drawable.image_4),
            Affirmation(R.string.affirmation5, R.drawable.image_5)
        )
    }
}
