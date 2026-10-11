package com.sara.giphygif.presentation

import androidx.annotation.StringRes
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth
import com.sara.giphygif.R
import com.sara.giphygif.data.network.FakeGiphyApi
import com.sara.giphygif.utils.TestTags
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import javax.inject.Inject

/**
 * End-to-end UI tests: the real activity, ViewModel and repository run against a fake Giphy API
 * and an in-memory database (see TestAppModule).
 */
@HiltAndroidTest
class GiphyAppTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createEmptyComposeRule()

    @Inject
    lateinit var fakeApi: FakeGiphyApi

    private var scenario: ActivityScenario<MainActivity>? = null

    private val trending1 = TestTags.trendingItem("${FakeGiphyApi.TRENDING}-1")

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @After
    fun tearDown() {
        scenario?.close()
    }

    @Test
    fun trendingGifsAreShownOnLaunch() {
        launchApp()

        waitFor(hasTestTag(trending1))
        composeRule.onNodeWithTag(trending1).assertIsDisplayed()
        Truth.assertThat(fakeApi.requestedUrls.first()).contains("/v1/gifs/trending")
    }

    @Test
    fun apiFailureShowsErrorMessage() {
        fakeApi.error = IOException("Network down")
        launchApp()

        waitFor(hasText("Network down", substring = true))
        composeRule.onNodeWithTag(trending1).assertDoesNotExist()
    }

    @Test
    fun searchShowsResultsAndClearingRestoresTrending() {
        launchApp()
        waitFor(hasTestTag(trending1))

        composeRule.onNodeWithTag(TestTags.SEARCH_FIELD).performTextInput("cats")

        val search1 = TestTags.trendingItem("${FakeGiphyApi.SEARCH}-1")
        waitFor(hasTestTag(search1))
        composeRule.onNodeWithTag(trending1).assertDoesNotExist()
        Truth.assertThat(fakeApi.requestedUrls.last()).contains("q=cats")

        composeRule.onNodeWithContentDescription("clear").performClick()

        waitFor(hasTestTag(trending1))
        composeRule.onNodeWithTag(search1).assertDoesNotExist()
    }

    @Test
    fun favouritesTabIsEmptyInitially() {
        launchApp()

        openTab("Favourite")

        waitFor(hasText(string(R.string.no_favourite_present)))
    }

    @Test
    fun addingAndRemovingFavouriteUpdatesFavouritesTab() {
        launchApp()
        waitFor(hasTestTag(trending1))

        composeRule.onNode(
            hasContentDescription(string(R.string.add_to_favourites)) and hasAnyAncestor(hasTestTag(trending1))
        ).performClick()

        openTab("Favourite")
        val favourite1 = TestTags.favouriteItem("${FakeGiphyApi.TRENDING}-1")
        waitFor(hasTestTag(favourite1))

        composeRule.onNode(
            hasContentDescription(string(R.string.remove_from_favourites)) and hasAnyAncestor(hasTestTag(favourite1))
        ).performClick()

        waitFor(hasText(string(R.string.no_favourite_present)))
        composeRule.onNodeWithTag(favourite1).assertDoesNotExist()
    }

    @Test
    fun favouriteIsMarkedInTrendingAfterReturningToTab() {
        launchApp()
        waitFor(hasTestTag(trending1))

        composeRule.onNode(
            hasContentDescription(string(R.string.add_to_favourites)) and hasAnyAncestor(hasTestTag(trending1))
        ).performClick()

        openTab("Favourite")
        waitFor(hasTestTag(TestTags.favouriteItem("${FakeGiphyApi.TRENDING}-1")))

        // Returning to Trending refetches the list and must mark saved gifs as favourites
        openTab("Trending")
        waitFor(
            hasContentDescription(string(R.string.remove_from_favourites)) and hasAnyAncestor(hasTestTag(trending1))
        )
    }

    private fun launchApp() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    private fun openTab(name: String) {
        composeRule.onNodeWithText(name).performClick()
    }

    // Network and database work runs on Dispatchers.IO, which Compose's idling doesn't track
    private fun waitFor(matcher: SemanticsMatcher, timeoutMillis: Long = 5_000) {
        composeRule.waitUntil(timeoutMillis) {
            composeRule.onAllNodes(matcher).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun string(@StringRes id: Int): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(id)

}
