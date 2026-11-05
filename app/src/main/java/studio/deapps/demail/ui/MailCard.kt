package studio.deapps.demail.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import studio.deapps.demail.data.Mail
import studio.deapps.demail.ui.theme.CardColor
import studio.deapps.demail.ui.theme.DeType
import studio.deapps.demail.ui.theme.Ink
import studio.deapps.demail.ui.theme.Ink50

/** How far the card content moves when selection mode opens. */
private val ContentShift = 44.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MailCard(
    mail: Mail,
    selectionMode: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shift by animateDpAsState(
        targetValue = if (selectionMode) ContentShift else 0.dp,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow),
        label = "contentShift",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardColor)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 18.dp + shift, top = 16.dp, bottom = 16.dp, end = 18.dp),
        ) {
            // Row 1 – (unread dot) sender
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (mail.unread) {
                    Box(
                        Modifier
                            .size(9.dp)
                            .background(Ink, CircleShape),
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text = mail.sender,
                    style = DeType.Sender,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(Modifier.height(8.dp))

            // Row 2 – subject ........ time
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = mail.subject,
                    style = DeType.Subject,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = mail.time,
                    style = DeType.Body,
                    maxLines = 1,
                )
            }

            Spacer(Modifier.height(4.dp))

            // Rows 3-4 – body preview
            Text(
                text = mail.body,
                style = DeType.Body,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }

        // Selection circle – centered vertically, left side
        AnimatedVisibility(
            visible = selectionMode,
            enter = fadeIn() + scaleIn(initialScale = 0.4f),
            exit = fadeOut() + scaleOut(targetScale = 0.4f),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp),
        ) {
            SelectionCircle(checked = selected)
        }
    }
}

@Composable
private fun SelectionCircle(checked: Boolean) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .then(
                if (checked) Modifier.background(Ink, CircleShape)
                else Modifier.border(1.5.dp, Ink50, CircleShape),
            ),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedVisibility(
            visible = checked,
            enter = fadeIn() + scaleIn(initialScale = 0.5f),
            exit = fadeOut() + scaleOut(targetScale = 0.5f),
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}
