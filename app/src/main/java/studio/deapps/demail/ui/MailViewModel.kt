package studio.deapps.demail.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import studio.deapps.demail.data.GmailApi
import studio.deapps.demail.data.GmailAuthException
import studio.deapps.demail.data.Mail

enum class AuthState { SignedOut, Authorizing, SignedIn }

class MailViewModel : ViewModel() {

    // ---------------------------------------------------------------- state
    val mails = mutableStateListOf<Mail>()

    var selectedIds by mutableStateOf(emptySet<String>())
        private set

    var authState by mutableStateOf(AuthState.SignedOut)
        private set

    var email by mutableStateOf<String?>(null)
        private set

    var refreshing by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    val selectionMode: Boolean get() = selectedIds.isNotEmpty()

    // ---------------------------------------------------------------- wiring (set by MainActivity)
    /** Asks the Activity for a (new) Gmail access token. */
    var requestToken: (() -> Unit)? = null

    /** Called when the user signs in / out so the Activity can remember it. */
    var onSignedInChanged: ((Boolean) -> Unit)? = null

    private var token: String? = null
    private var retry: (() -> Unit)? = null
    private var authRetried = false

    // ---------------------------------------------------------------- auth
    fun signIn() {
        error = null
        authState = AuthState.Authorizing
        retry = { refresh() }
        requestToken?.invoke()
    }

    fun onToken(accessToken: String?) {
        if (accessToken == null) {
            onAuthError("Google did not return an access token")
            return
        }
        token = accessToken
        if (authState != AuthState.SignedIn) {
            authState = AuthState.SignedIn
            onSignedInChanged?.invoke(true)
        }
        val r = retry
        retry = null
        r?.invoke()
    }

    fun onAuthError(message: String?) {
        retry = null
        refreshing = false
        if (authState == AuthState.SignedIn) {
            error = message ?: "Authorization failed"
        } else {
            authState = AuthState.SignedOut
            error = message ?: "Sign-in failed"
        }
    }

    fun signOut() {
        token = null
        retry = null
        mails.clear()
        selectedIds = emptySet()
        email = null
        error = null
        refreshing = false
        authState = AuthState.SignedOut
        onSignedInChanged?.invoke(false)
    }

    fun dismissError() {
        error = null
    }

    // ---------------------------------------------------------------- loading
    fun refresh() {
        refreshing = true
        launchApi(onError = { refreshing = false }) { t ->
            val list = GmailApi.inbox(t)
            if (email == null) email = GmailApi.profileEmail(t)
            mails.clear()
            mails.addAll(list)
            val present = list.map { it.id }.toSet()
            selectedIds = selectedIds.intersect(present)
            refreshing = false
        }
    }

    /**
     * Runs [block] with a valid token. On HTTP 401 asks the Activity for a fresh token
     * (once) and runs the block again.
     */
    private fun launchApi(onError: (() -> Unit)? = null, block: suspend (String) -> Unit) {
        viewModelScope.launch {
            val t = token
            if (t == null) {
                retry = { launchApi(onError, block) }
                requestToken?.invoke()
                return@launch
            }
            try {
                block(t)
                authRetried = false
            } catch (e: CancellationException) {
                throw e
            } catch (e: GmailAuthException) {
                token = null
                if (authRetried) {
                    authRetried = false
                    error = "Authorization expired. Please sign in again."
                    onError?.invoke()
                } else {
                    authRetried = true
                    retry = { launchApi(onError, block) }
                    requestToken?.invoke()
                }
            } catch (e: Exception) {
                error = e.message ?: "Network error"
                onError?.invoke()
            }
        }
    }

    // ---------------------------------------------------------------- selection
    fun startSelection(id: String) {
        selectedIds = selectedIds + id
    }

    fun toggleSelection(id: String) {
        selectedIds = if (id in selectedIds) selectedIds - id else selectedIds + id
    }

    fun selectAll() {
        selectedIds = mails.map { it.id }.toSet()
    }

    fun clearSelection() {
        selectedIds = emptySet()
    }

    // ---------------------------------------------------------------- actions
    fun archiveSelected() = modifySelected(add = emptyList(), remove = listOf("INBOX"), drop = true)

    fun spamSelected() = modifySelected(add = listOf("SPAM"), remove = listOf("INBOX"), drop = true)

    fun deleteSelected() = modifySelected(add = listOf("TRASH"), remove = listOf("INBOX"), drop = true)

    fun markSelectedRead(read: Boolean) = modifySelected(
        add = if (read) emptyList() else listOf("UNREAD"),
        remove = if (read) listOf("UNREAD") else emptyList(),
        drop = false,
        unread = !read,
    )

    fun openMail(id: String) {
        val i = mails.indexOfFirst { it.id == id }
        if (i < 0 || !mails[i].unread) return
        mails[i] = mails[i].copy(unread = false)
        launchApi(onError = { refresh() }) { t ->
            GmailApi.batchModify(t, listOf(id), emptyList(), listOf("UNREAD"))
        }
    }

    /** Updates the list immediately, then syncs with Gmail (and reloads if that fails). */
    private fun modifySelected(
        add: List<String>,
        remove: List<String>,
        drop: Boolean,
        unread: Boolean? = null,
    ) {
        val ids = selectedIds.toList()
        if (ids.isEmpty()) return
        val set = ids.toSet()

        if (drop) {
            mails.removeAll { it.id in set }
        } else if (unread != null) {
            for (i in mails.indices) {
                val m = mails[i]
                if (m.id in set) mails[i] = m.copy(unread = unread)
            }
        }
        clearSelection()

        launchApi(onError = { refresh() }) { t ->
            GmailApi.batchModify(t, ids, add, remove)
        }
    }
}
