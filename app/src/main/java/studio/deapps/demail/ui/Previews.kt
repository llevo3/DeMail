package studio.deapps.demail.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import studio.deapps.demail.data.Mail
import studio.deapps.demail.data.SampleData
import studio.deapps.demail.ui.theme.DeMailTheme
import studio.deapps.demail.ui.theme.ScreenBackground

private val unreadMail = SampleData.mails[0]
private val readMail = SampleData.mails[2]

/** Full inbox, normal state. */
@Preview(name = "Inbox", showBackground = true, showSystemUi = true)
@Composable
private fun InboxPreview() {
    DeMailTheme {
        InboxScreen(
            mails = SampleData.mails,
            selectedIds = emptySet(),
            onMailClick = {}, onMailLongClick = {}, onClearSelection = {}, onSelectAll = {},
            onMarkRead = {}, onArchive = {}, onSpam = {}, onDelete = {},
        )
    }
}

/** Full inbox after a long tap: action bar in the header, circles on cards. */
@Preview(name = "Inbox – selection", showBackground = true, showSystemUi = true)
@Composable
private fun InboxSelectionPreview() {
    DeMailTheme {
        InboxScreen(
            mails = SampleData.mails,
            selectedIds = setOf("2", "4"),
            onMailClick = {}, onMailLongClick = {}, onClearSelection = {}, onSelectAll = {},
            onMarkRead = {}, onArchive = {}, onSpam = {}, onDelete = {},
        )
    }
}

/** Sign-in screen. */
@Preview(name = "Login", showBackground = true, showSystemUi = true)
@Composable
private fun LoginPreview() {
    DeMailTheme {
        LoginScreen(loading = false, error = null, onSignIn = {})
    }
}

/** Individual card states side by side. */
@Preview(name = "Cards", showBackground = true, widthDp = 380)
@Composable
private fun CardStatesPreview() {
    DeMailTheme {
        Column(
            modifier = Modifier
                .background(ScreenBackground)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            card(unreadMail, selectionMode = false, selected = false)
            card(readMail, selectionMode = false, selected = false)
            card(unreadMail, selectionMode = true, selected = false)
            card(readMail, selectionMode = true, selected = true)
        }
    }
}

@Composable
private fun card(mail: Mail, selectionMode: Boolean, selected: Boolean) {
    MailCard(
        mail = mail,
        selectionMode = selectionMode,
        selected = selected,
        onClick = {},
        onLongClick = {},
    )
}
