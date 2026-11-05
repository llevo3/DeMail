package studio.deapps.demail

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import studio.deapps.demail.data.GMAIL_SCOPE
import studio.deapps.demail.ui.AuthState
import studio.deapps.demail.ui.InboxRoute
import studio.deapps.demail.ui.LoginScreen
import studio.deapps.demail.ui.MailViewModel
import studio.deapps.demail.ui.theme.DeMailTheme

class MainActivity : ComponentActivity() {

    private val vm: MailViewModel by viewModels()
    private val prefs by lazy { getSharedPreferences("demail", MODE_PRIVATE) }

    /** Receives the result of Google's consent / account-picker screen. */
    private val authLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        if (result.resultCode != RESULT_OK || result.data == null) {
            vm.onAuthError("Sign-in cancelled")
            return@registerForActivityResult
        }
        try {
            val auth = Identity.getAuthorizationClient(this)
                .getAuthorizationResultFromIntent(result.data)
            vm.onToken(auth.accessToken)
        } catch (e: Exception) {
            vm.onAuthError(e.message ?: "Sign-in failed")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT),
        )

        vm.requestToken = ::requestAuthorization
        vm.onSignedInChanged = { prefs.edit().putBoolean(KEY_SIGNED_IN, it).apply() }

        // Returning user: get a fresh token silently (no screen if consent was already given).
        if (savedInstanceState == null && prefs.getBoolean(KEY_SIGNED_IN, false)) {
            vm.signIn()
        }

        setContent {
            DeMailTheme {
                if (vm.authState == AuthState.SignedIn) {
                    InboxRoute(vm)
                } else {
                    LoginScreen(
                        loading = vm.authState == AuthState.Authorizing,
                        error = vm.error,
                        onSignIn = vm::signIn,
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        if (isFinishing) {
            vm.requestToken = null
            vm.onSignedInChanged = null
        }
        super.onDestroy()
    }

    private fun requestAuthorization() {
        val request = AuthorizationRequest.builder()
            .setRequestedScopes(listOf(Scope(GMAIL_SCOPE)))
            .build()

        Identity.getAuthorizationClient(this)
            .authorize(request)
            .addOnSuccessListener { result ->
                val pending = result.pendingIntent
                if (result.hasResolution() && pending != null) {
                    // First time (or consent revoked): show Google's account / consent screen.
                    authLauncher.launch(IntentSenderRequest.Builder(pending.intentSender).build())
                } else {
                    vm.onToken(result.accessToken)
                }
            }
            .addOnFailureListener { e ->
                vm.onAuthError(e.message ?: "Authorization failed")
            }
    }

    private companion object {
        const val KEY_SIGNED_IN = "signed_in"
    }
}
