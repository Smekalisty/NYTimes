package com.nytimes.activities

import android.content.Intent
import android.net.Uri
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.nytimes.R
import com.nytimes.entities.pojo.Media
import com.nytimes.entities.pojo.Post
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DetailActivityTest {
    //Do not judge strictly. These are my first tests for android

    @get:Rule
    val activityTestRule = object : ActivityTestRule<DetailActivity>(DetailActivity::class.java) {
        override fun getActivityIntent(): Intent {
            val metadata1 = com.nytimes.entities.pojo.Metadata("image-link1", "small")
            val metadata2 = com.nytimes.entities.pojo.Metadata("image-link2", "large")
            val metadata = listOf(metadata1, metadata2)
            val media = Media("image", metadata)
            val post = Post(1, "http://nyt.com/post/1", "Summit Failed", "Big Threats - Big Egos", listOf(media))

            return Intent().putExtra(DetailActivity.extraPost, post)
        }
    }

    @Test
    fun checkTexts() {
        onView(withId(R.id.id)).check(matches(withText(1.toString())))
        onView(withId(R.id.title)).check(matches(withText("Summit Failed")))
        onView(withId(R.id.description)).check(matches(withText("Big Threats - Big Egos")))
    }

    @Test
    fun openUrl() {
        Intents.init()
        onView(withId(R.id.openUrl)).perform(click())
        intended(hasData(Uri.parse("http://nyt.com/post/1")))
        Intents.release()
    }
}