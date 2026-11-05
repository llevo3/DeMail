package studio.deapps.demail.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.MarkEmailRead
import androidx.compose.material.icons.outlined.MarkEmailUnread
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Report
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import studio.deapps.demail.data.Mail
import studio.deapps.demail.ui.theme.DeType
import studio.deapps.demail.ui.theme.Ink
import studio.deapps.demail.ui.theme.Ink50
import studio.deapps.demail.ui.theme.Outfit
import studio.deapps.demail.ui.theme.ScreenBackground

/** Entry point that wires the ViewModel to the stateless screen. */
@Composable
fun InboxRoute(vm: MailViewModel = viewModel()) {
    InboxScreen(
        mails = vm.mails,
        selectedIds = vm.selectedIds,
        onMailClick = { id ->
            if (vm.selectionMode) vm.toggleSelection(id) else vm.openMail(id)
        },
        onMailLongClick = { id ->
            if (!vm.selectionMode) vm.startSelection(id) else vm.toggleSelection(id)
        },
        onClearSelection = vm::clearSelection,
        onSelectAll = vm::selectAll,
        onMarkRead = vm::markSelectedRead,
        onArchive = vm::archiveSelected,
        onSpam = vm::spamSelected,
        onDelete = vm::deleteSelected,
        refreshing = vm.refreshing,
        email = vm.email,
        error = vm.error,
        onRefresh = vm::refresh,
        onSignOut = vm::signOut,
        onDismissError = vm::dismissError,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(
    mails: List<Mail>,
    selectedIds: Set<String>,
    onMailClick: (String) -> Unit,
    onMailLongClick: (String) -> Unit,
    onClearSelection: () -> Unit,
    onSelectAll: () -> Unit,
    onMarkRead: (Boolean) -> Unit,
    onArchive: () -> Unit,
    onSpam: () -> Unit,
    onDelete: () -> Unit,
    refreshing: Boolean = false,
    email: String? = null,
    error: String? = null,
    onRefresh: () -> Unit = {},
    onSignOut: () -> Unit = {},
    onDismissError: () -> Unit = {},
) {
    val selectionMode = selectedIds.isNotEmpty()
    val haptics = LocalHapticFeedback.current

    BackHandler(enabled = selectionMode) { onClearSelection() }

    Box(
        Modifier
            .fillMaxSize()
            .background(ScreenBackground),
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            InboxHeader(
                selectionMode = selectionMode,
                count = selectedIds.size,
                anyUnreadSelected = mails.any { it.id in selectedIds && it.unread },
                email = email,
                onClose = onClearSelection,
                onSelectAll = onSelectAll,
                onMarkRead = onMarkRead,
                onArchive = onArchive,
                onSpam = onSpam,
                onDelete = onDelete,
                onSignOut = onSignOut,
            )

            PullToRefreshBox(
                isRefreshing = refreshing,
                onRefresh = onRefresh,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (error != null) {
                        item(key = "error") {
                            Text(
                                text = error,
                                style = DeType.Body.copy(color = Color.White),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Ink)
                                    .clickable(onClick = onDismissError)
                                    .padding(16.dp),
                            )
                        }
                    }

                    if (mails.isEmpty() && !refreshing && error == null) {
                        item(key = "empty") {
                            Text(
                                text = "Your inbox is empty",
                                style = DeType.Body,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 48.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            )
                        }
                    }

                    items(mails, key = { it.id }) { mail ->
                        MailCard(
                            mail = mail,
                            selectionMode = selectionMode,
                            selected = mail.id in selectedIds,
                            onClick = { onMailClick(mail.id) },
                            onLongClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                onMailLongClick(mail.id)
                            },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InboxHeader(
    selectionMode: Boolean,
    count: Int,
    anyUnreadSelected: Boolean,
    email: String?,
    onClose: () -> Unit,
    onSelectAll: () -> Unit,
    onMarkRead: (Boolean) -> Unit,
    onArchive: () -> Unit,
    onSpam: () -> Unit,
    onDelete: () -> Unit,
    onSignOut: () -> Unit,
) {
    AnimatedContent(
        targetState = selectionMode,
        transitionSpec = {
            (fadeIn() + slideInVertically { -it / 4 }) togetherWith
                (fadeOut() + slideOutVertically { it / 4 })
        },
        label = "header",
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
    ) { selecting ->
        if (!selecting) {
            var menuOpen by remember { mutableStateOf(false) }
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 20.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "Inbox", style = DeType.Header, modifier = Modifier.weight(1f))
                Box {
                    ToolbarIcon(Icons.Outlined.MoreVert, "Account") { menuOpen = true }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        if (email != null) {
                            DropdownMenuItem(
                                text = { Text(email, fontFamily = Outfit, color = Ink50) },
                                onClick = {},
                                enabled = false,
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Sign out", fontFamily = Outfit) },
                            onClick = {
                                menuOpen = false
                                onSignOut()
                            },
                        )
                    }
                }
            }
        } else {
            SelectionToolbar(
                count = count,
                anyUnreadSelected = anyUnreadSelected,
                onClose = onClose,
                onSelectAll = onSelectAll,
                onMarkRead = onMarkRead,
                onArchive = onArchive,
                onSpam = onSpam,
                onDelete = onDelete,
            )
        }
    }
}

@Composable
private fun SelectionToolbar(
    count: Int,
    anyUnreadSelected: Boolean,
    onClose: () -> Unit,
    onSelectAll: () -> Unit,
    onMarkRead: (Boolean) -> Unit,
    onArchive: () -> Unit,
    onSpam: () -> Unit,
    onDelete: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ToolbarIcon(Icons.Outlined.Close, "Close", onClose)
        Spacer(Modifier.width(4.dp))
        Text(text = count.toString(), style = DeType.Count)
        Spacer(Modifier.weight(1f))

        ToolbarIcon(Icons.Outlined.Archive, "Archive", onArchive)
        if (anyUnreadSelected) {
            ToolbarIcon(Icons.Outlined.MarkEmailRead, "Mark as read") { onMarkRead(true) }
        } else {
            ToolbarIcon(Icons.Outlined.MarkEmailUnread, "Mark as unread") { onMarkRead(false) }
        }
        ToolbarIcon(Icons.Outlined.Report, "Report spam", onSpam)
        ToolbarIcon(Icons.Outlined.Delete, "Delete", onDelete)

        Box {
            ToolbarIcon(Icons.Outlined.MoreVert, "More") { menuOpen = true }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(
                    text = { Text("Select all", fontFamily = Outfit) },
                    onClick = {
                        menuOpen = false
                        onSelectAll()
                    },
                )
            }
        }
    }
}

@Composable
private fun ToolbarIcon(icon: ImageVector, description: String, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(imageVector = icon, contentDescription = description, tint = Ink)
    }
}
