package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.ChargeUpViewModel
import com.example.ui.Screen
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("3 Min Charge-Up", appName)
  }

  @Test
  fun testViewModelNavigation() {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = ChargeUpViewModel(application)
    
    assertEquals(Screen.ONBOARDING, viewModel.currentScreen.value)
    
    viewModel.navigateTo(Screen.HISTORY)
    assertEquals(Screen.HISTORY, viewModel.currentScreen.value)
    
    viewModel.navigateTo(Screen.ONBOARDING)
    assertEquals(Screen.ONBOARDING, viewModel.currentScreen.value)
  }

  @Test
  fun testGeminiClientOfflineFallback() = runTest {
    val script = com.example.api.GeminiClient.getOfflineBundledScript(
      "Calm", "Lazy", "Motivation", "Japanese", "Mountains"
    )
    println("Offline Script: $script")
    org.json.JSONObject(script) // Should parse successfully
  }
}

